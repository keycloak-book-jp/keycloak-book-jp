package sample.clientapp.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import sample.clientapp.ClientSession;
import sample.clientapp.TokenResponse;
import sample.clientapp.config.ClientAppConfiguration;
import sample.clientapp.config.OauthConfiguration;
import sample.clientapp.jwt.AccessToken;
import sample.clientapp.jwt.IdToken;
import sample.clientapp.jwt.JsonWebToken;
import sample.clientapp.jwt.RefreshToken;
import sample.clientapp.util.JsonUtil;
import sample.clientapp.util.OauthUtil;

@Service
public class ClientAppService {

    private static final Logger logger = LoggerFactory.getLogger(ClientAppService.class);

    @Autowired
    ClientAppConfiguration clientConfig;

    @Autowired
    OauthConfiguration oauthConfig;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ClientSession clientSession;

    public String getAuthorizationUrl(String scope) {
        UriComponentsBuilder authorizationUrl = UriComponentsBuilder.fromUriString(clientConfig.getAuthorizationEndpoint());
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        String redirectUrl = URLEncoder.encode(generateRedirectUri(), Charset.defaultCharset());
        params.add("redirect_uri", redirectUrl);
        params.add("response_type", "code");
        params.add("client_id", clientConfig.getClientId());

        if (scope != null && !scope.isEmpty()) {
            params.add("scope", URLEncoder.encode(scope, Charset.defaultCharset()));
        }

        if (oauthConfig.isState()) {
            String state = UUID.randomUUID().toString();
            clientSession.setState(state);
            params.add("state", state);
        }

        if (oauthConfig.isNonce()) {
            String nonce = UUID.randomUUID().toString();
            clientSession.setNonce(nonce);
            params.add("nonce", nonce);
        }

        if (oauthConfig.isPkce()) {
            String codeVerifier = OauthUtil.generateCodeVerifier();
            String codeChallenge = OauthUtil.generateCodeChallenge(codeVerifier);
            params.add("code_challenge_method", "S256");
            params.add("code_challenge", codeChallenge);
            clientSession.setCodeVerifier(codeVerifier);
        }

        if (oauthConfig.isFormPost()) {
            params.add("response_mode", "form_post");
        }

        return authorizationUrl.queryParams(params).build(true).toUriString();
    }

    public TokenResponse requestToken(String authorizationCode) {
        StringBuilder tokenRequestUrl = new StringBuilder();
        tokenRequestUrl.append(clientConfig.getTokenEndpoint());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientConfig.getClientId(), clientConfig.getClientSecret());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", generateRedirectUri());

        if (oauthConfig.isPkce()) {

            params.add("code_verifier", clientSession.getCodeVerifier());
        }

        RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST, URI.create(tokenRequestUrl.toString()));
        TokenResponse token = null;
        try {
            printRequest("Token Request", req);

            ResponseEntity<TokenResponse> res = restTemplate.exchange(req, TokenResponse.class);
            token = res.getBody();
            printResponse("Token Response", res);

        } catch (HttpStatusCodeException e) {
            printClientError("Token Response", e);
            return TokenResponse.withError(e.getMessage(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return TokenResponse.withError(e.getMessage(), null);
        }

        return token;
    }

    public TokenResponse processAuthorizationCodeGrant(String code, String state) {
        // check state before token request
        if (oauthConfig.isState()) {
            if (state == null || !state.equals(clientSession.getState())) {
                // state check failure. Write error handling here.
                logger.error("state check NG");
                return TokenResponse.withError("state check NG", null);
            } else {
                logger.debug("state check OK");
                clientSession.setState(null);
            }
        }

        TokenResponse token = requestToken(code);
        if (token.getError() != null) {
            return token;
        }

        // check nonce after ID token is obtained
        if (oauthConfig.isNonce() && token.getIdToken() != null) {
            IdToken idToken = JsonWebToken.parse(token.getIdToken(), IdToken.class);
            if (idToken.getNonce() == null || !idToken.getNonce().equals(clientSession.getNonce())) {
                // nonce check failure. Write error handling here.
                logger.error("nonce check NG\n");
                return TokenResponse.withError("nonce check NG", null);
            } else {
                logger.debug("nonce check OK\n");
                clientSession.setNonce(null);
            }
        }

        return token;
    }

    public TokenResponse refreshToken(RefreshToken refreshToken) {
        StringBuilder tokenRequestUrl = new StringBuilder();
        tokenRequestUrl.append(clientConfig.getTokenEndpoint());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientConfig.getClientId(), clientConfig.getClientSecret());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("grant_type", "refresh_token");
        if (refreshToken != null) {
            params.add("refresh_token", refreshToken.getTokenString());
        }
        RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST, URI.create(tokenRequestUrl.toString()));
        TokenResponse token = null;
        printRequest("Refresh Request", req);

        try {
            ResponseEntity<TokenResponse> res = restTemplate.exchange(req, TokenResponse.class);
            token = res.getBody();
            printResponse("Refresh Response", res);
        } catch (HttpStatusCodeException e) {
            printClientError("Refresh Response", e);
            return TokenResponse.withError(e.getMessage(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return TokenResponse.withError(e.getMessage(), null);
        }

        return token;
    }

    public void revokeToken(RefreshToken refreshToken) {
        StringBuilder revokeUrl = new StringBuilder();
        revokeUrl.append(clientConfig.getRevokeEndpoint());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientConfig.getClientId(), clientConfig.getClientSecret());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        if (refreshToken != null) {
            params.add("token", refreshToken.getTokenString());
            params.add("token_type_hint", "refresh_token");
        }
        RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST, URI.create(revokeUrl.toString()));

        printRequest("Revoke Request", req);

        try {
            restTemplate.exchange(req, Object.class);
        } catch (HttpStatusCodeException e) {
            printClientError("Revoke Response", e);

        } catch (ResourceAccessException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String callApi(String url, AccessToken accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.setBearerAuth(accessToken.getTokenString());
        }

        RequestEntity<?> req = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        printRequest("Call API", req);
        String response = null;
        try {
            ResponseEntity<String> res = restTemplate.exchange(req, String.class);
            response = res.getBody();
            printResponse("Call API", res);
        } catch (HttpStatusCodeException e) {
            printClientError("Call API", e);
            response = e.getStatusCode().toString();
        } catch (ResourceAccessException e) {
            response = e.getMessage();
        }

        return response;
    }

    private String generateRedirectUri() {
        String redirectUri = ServletUriComponentsBuilder.fromCurrentRequest().replacePath("/gettoken").replaceQuery(null)
                .toUriString();
        return redirectUri;
    }

    private void printRequest(String msg, RequestEntity<?> req) {
        Map<String, Object> message = new HashMap<>();
        message.put("method", req.getMethod().toString());
        message.put("url", req.getUrl().toString());
        message.put("headers", req.getHeaders());
        if (req.hasBody()) {
            message.put("body", req.getBody());
        }
        logger.debug("ReqeustType=\"" + msg + "\" RequestInfo=" + JsonUtil.marshal(message, false));
        return;
    }

    private void printResponse(String responseType, ResponseEntity<?> resp) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", resp.getStatusCode().toString());
        message.put("headers", resp.getHeaders());
        message.put("body", resp.getBody());
        logger.debug("ResponseType=\"" + responseType + "\" ResponseInfo=" + JsonUtil.marshal(message, false));
        return;
    }

    private void printClientError(String errorType, HttpStatusCodeException e) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", e.getStatusCode().toString());
        message.put("headers", e.getResponseHeaders());
        message.put("body", e.getResponseBodyAsString());
        logger.error("ErrorType=\"" + errorType + "\" ResponseInfo=" + JsonUtil.marshal(message, false));

    }

}

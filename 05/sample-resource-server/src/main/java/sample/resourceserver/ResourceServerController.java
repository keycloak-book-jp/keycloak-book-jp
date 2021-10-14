package sample.resourceserver;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import sample.oauthutil.AccessToken;
import sample.oauthutil.IntrospectionResponse;
import sample.oauthutil.OauthUtil;

@RestController
public class ResourceServerController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServerController.class);

    @Autowired
    ResourceServerConfiguration serverConfig;

    @Autowired
    RestTemplate restTemplate;

    private void printRequest(String requestType, RequestEntity<?> req) {
        Map<String, Object> message = new HashMap<>();
        message.put("method", req.getMethod());
        message.put("url", req.getUrl());
        message.put("headers", req.getHeaders());
        if (req.hasBody()) {
            message.put("body", req.getBody());
        }
        logger.debug("RequestType=\"" + requestType + "\" RequestInfo=" + writeJsonString(message, false));
    }

    private void printResponse(String responseType, ResponseEntity<?> resp) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", resp.getStatusCode());
        message.put("headers", resp.getHeaders());
        message.put("body", resp.getBody());
        logger.debug("ResponseType=\"" + responseType + "\" ResponseInfo=" + writeJsonString(message, false));
    }

    private boolean requestTokenIntrospection(String accessToken) {
        boolean result = false;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization",
                "Basic " + OauthUtil.encodeToBasicClientCredential(serverConfig.getClientId(), serverConfig.getClientSecret()));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);

        RequestEntity<?> req = new RequestEntity<>(params, headers, HttpMethod.POST, URI.create(serverConfig.getAuthserverUrl() + serverConfig.getIntrospectionEndpoint()));
        printRequest("Introspection Request", req);

        try {
            ResponseEntity<IntrospectionResponse> res = restTemplate.exchange(req, IntrospectionResponse.class);
            printResponse("Introspection Response", res);
            IntrospectionResponse resBody = res.getBody();
            result = resBody != null && Objects.equals(resBody.getActive(), "true");
        } catch (HttpClientErrorException e) {
            logger.error("response code=\"" + e.getStatusCode() + "\" body=" + e.getResponseBodyAsString());
        }

        return result;
    }

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getEcho() {
        return new ResponseEntity<>(Collections.singletonMap("message", "echo!"), HttpStatus.OK);
    }

    @RequestMapping(value = "/demointrospection", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getDemoIntrospection(
            @RequestHeader("Authorization") String authorizationString) {
        String accessToken = (authorizationString.split(" ", 0))[1];

        if (requestTokenIntrospection(accessToken)) {
            return new ResponseEntity<>(Collections.singletonMap("message", "called demointrospection"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Collections.singletonMap("message", "error!"), HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/readdata", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getReadData(@RequestHeader("Authorization") String authorizationString) {
        String accessToken = (authorizationString.split(" ", 0))[1];

        if (requestTokenIntrospection(accessToken)) {
            AccessToken token = OauthUtil.readJsonContent(OauthUtil.decodeFromBase64Url(accessToken), AccessToken.class);
            logger.debug("Scope of Token: \"" + token.getScope() + "\"");
            if (token.getScopeList().contains("readdata")) {

                return new ResponseEntity<>(Collections.singletonMap("message",
                        String.format("%s's Protected Resource.", token.getPreferredUsername())), HttpStatus.OK);
            } else {
                logger.error("readdata scope is not included.");
                return new ResponseEntity<>(Collections.singletonMap("message", "error!"), HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(Collections.singletonMap("message", "error!"), HttpStatus.UNAUTHORIZED);
        }
    }

    public String writeJsonString(Object obj, boolean indent) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, indent);
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("unable to deserialize", e);
        }
        return "";
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplateBuilder RestTemplateBuilder = new RestTemplateBuilder();
        return RestTemplateBuilder.build();
    }
}
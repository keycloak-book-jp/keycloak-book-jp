package sample.clientapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import sample.clientapp.config.ClientAppConfiguration;
import sample.clientapp.config.OauthConfiguration;
import sample.clientapp.jwt.AccessToken;
import sample.clientapp.jwt.RefreshToken;
import sample.clientapp.service.ClientAppService;

@Controller
public class ClientAppController {

    private static final Logger logger = LoggerFactory.getLogger(ClientAppController.class);

    @Autowired
    ClientAppConfiguration clientConfig;

    @Autowired
    OauthConfiguration oauthConfig;

    @Autowired
    ClientAppService service;

    @Autowired
    ClientSession clientSession;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public String auth(@RequestParam("scope") String scope) {
        clientSession.setScope(scope);
        String authUrl = service.getAuthorizationUrl(scope);
        logger.debug("Type=\"Authorization Request\" Status=\"302\" Location=\"" + authUrl + "\"");
        return String.format("redirect:%s", authUrl);
    }

    @RequestMapping(value = "/gettoken", method = RequestMethod.GET)
    public String getToken(@RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            @RequestParam(name = "state", required = false) String state, Model model) {

        if (oauthConfig.isFormPost()) {
            return "gettoken";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("errorDescription", errorDescription);
            return "gettoken";
        }

        TokenResponse tokenResponse = service.processAuthorizationCodeGrant(code, state);
        if (tokenResponse.getError() != null) {
            model.addAttribute("error", tokenResponse.getError());
            model.addAttribute("errorDescription", tokenResponse.getErrorDescription());
        }
        clientSession.setTokensFromTokenResponse(tokenResponse);
        return "gettoken";

    }

    @RequestMapping(value = "/gettoken", method = RequestMethod.POST)
    public String getTokenFormPost(@RequestParam(value = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription, Model model) {

        if (!oauthConfig.isFormPost()) {
            return "gettoken";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("errorDescription", errorDescription);
            return "gettoken";
        }

        TokenResponse tokenResponse = service.processAuthorizationCodeGrant(code, state);
        if (tokenResponse.getError() != null) {
            model.addAttribute("error", tokenResponse.getError());
            model.addAttribute("errorDescription", tokenResponse.getErrorDescription());
        }
        clientSession.setTokensFromTokenResponse(tokenResponse);

        return "gettoken";
    }

    @RequestMapping(value = "/refresh")
    public String refreshToken(Model model) {
        RefreshToken token = clientSession.getRefreshToken();
        TokenResponse tokenResponse = service.refreshToken(token);
        if (tokenResponse.getError() != null) {
            model.addAttribute("error", tokenResponse.getError());
            model.addAttribute("errorDescription", tokenResponse.getErrorDescription());
        }
        clientSession.setTokensFromTokenResponse(tokenResponse);
        return "gettoken";
    }

    @RequestMapping(value = "/revoke")
    public String logout(Model model) {
        RefreshToken refreshToken = clientSession.getRefreshToken();
        if (refreshToken == null) {
            return "forward:/";
        }

        service.revokeToken(refreshToken);

        // session.setAttribute("accessToken", null);
        // session.setAttribute("refreshToken", null);

        return "forward:/";
    }

    @RequestMapping("/callecho")
    public String callEcho(Model model) {
        AccessToken accessToken = clientSession.getAccessToken();
        String uri = clientConfig.getApiserverUrl() + "/echo";
        String response = service.callApi(uri, accessToken);
        model.addAttribute("apiResponse", response);
        return "forward:/";
    }

    @RequestMapping("/calldemointrospection")
    public String callReadApi(Model model) {
        AccessToken accessToken = clientSession.getAccessToken();
        String uri = clientConfig.getApiserverUrl() + "/demointrospection";
        String response = service.callApi(uri, accessToken);
        model.addAttribute("apiResponse", response);
        return "forward:/";
    }

    @RequestMapping("/callreadapi")
    public String callWriteApi(Model model) {
        AccessToken accessToken = clientSession.getAccessToken();
        String uri = clientConfig.getApiserverUrl() + "/readdata";
        String response = service.callApi(uri, accessToken);
        model.addAttribute("apiResponse", response);
        return "forward:/";
    }
}
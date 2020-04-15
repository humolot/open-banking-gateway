package de.adorsys.opba.tppauthapi.controller;

import de.adorsys.opba.db.domain.entity.psu.Psu;
import de.adorsys.opba.protocol.facade.config.auth.FacadeAuthConfig;
import de.adorsys.opba.tppauthapi.model.generated.LoginResponse;
import de.adorsys.opba.tppauthapi.model.generated.PsuAuthBody;
import de.adorsys.opba.tppauthapi.resource.generated.PsuAuthApi;
import de.adorsys.opba.tppauthapi.service.PsuAuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

import static de.adorsys.opba.restapi.shared.HttpHeaders.AUTHORIZATION_SESSION_ID;
import static de.adorsys.opba.restapi.shared.HttpHeaders.X_REQUEST_ID;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*") //FIXME move CORS at gateway/load balancer level
public class PsuAuthController implements PsuAuthApi {

    public static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final PsuAuthService psuAuthService;
    private final FacadeAuthConfig authConfig;
    private final TppAuthResponseCookie.TppAuthResponseCookieBuilder tppAuthResponseCookieBuilder;

    @Override
    @SneakyThrows
    public ResponseEntity<LoginResponse> login(PsuAuthBody psuAuthBody, UUID xRequestID) {
        Psu psu = psuAuthService.tryAuthenticateUser(psuAuthBody.getId(), psuAuthBody.getPassword());

        String jwtToken = psuAuthService.generateToken(psu.getLogin());

        String cookieString = tppAuthResponseCookieBuilder
                .responseCookieBuilder(ResponseCookie.from(AUTHORIZATION_SESSION_ID, jwtToken))
                .build().getCookieString();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setXsrfToken(ENCODER.encodeToString(jwtToken.getBytes()));
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .header(X_REQUEST_ID, xRequestID.toString())
                .header(SET_COOKIE, cookieString)
                .body(loginResponse);
    }

    @Override
    public ResponseEntity<Void> registration(PsuAuthBody psuAuthDto, UUID xRequestID) {
        psuAuthService.createPsuIfNotExist(psuAuthDto.getId(), psuAuthDto.getPassword());

        HttpHeaders responseHeaders = new HttpHeaders();
        // FIXME - this is incorrect as there should be user binding after registration, but currently keeping as is
        responseHeaders.add(HttpHeaders.LOCATION, authConfig.getRedirect().getLoginPage());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }
}
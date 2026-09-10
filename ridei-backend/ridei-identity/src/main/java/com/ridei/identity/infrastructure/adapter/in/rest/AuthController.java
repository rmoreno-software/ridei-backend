package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.ridei.identity.application.GoogleAuthResult;
import com.ridei.identity.application.LoginCommand;
import com.ridei.identity.application.LoginResult;
import com.ridei.identity.application.LoginWithGoogleCommand;
import com.ridei.identity.application.RequestTemporaryPasswordCommand;
import com.ridei.identity.application.ResendVerificationEmailCommand;
import com.ridei.identity.application.ResetPasswordCommand;
import com.ridei.identity.application.VerifyEmailCommand;
import com.ridei.identity.domain.exception.InvalidOrExpiredVerificationTokenException;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.port.in.LoginUseCase;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.in.RequestTemporaryPasswordUseCase;
import com.ridei.identity.domain.port.in.ResendVerificationEmailUseCase;
import com.ridei.identity.domain.port.in.ResetPasswordUseCase;
import com.ridei.identity.domain.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.in.VerifyEmailUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final LoginWithGoogleUseCase loginWithGoogleUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final LoginUseCase loginUseCase;
    private final RequestTemporaryPasswordUseCase requestTemporaryPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;
    private final ITemplateEngine templateEngine;

    public AuthController(
        LoginWithGoogleUseCase loginWithGoogleUseCase,
        ValidateTokenUseCase validateTokenUseCase,
        LoginUseCase loginUseCase,
        RequestTemporaryPasswordUseCase requestTemporaryPasswordUseCase,
        ResetPasswordUseCase resetPasswordUseCase,
        VerifyEmailUseCase verifyEmailUseCase,
        ResendVerificationEmailUseCase resendVerificationEmailUseCase,
        ITemplateEngine templateEngine
    ) {

        this.loginWithGoogleUseCase = loginWithGoogleUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
        this.loginUseCase = loginUseCase;
        this.requestTemporaryPasswordUseCase = requestTemporaryPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.resendVerificationEmailUseCase = resendVerificationEmailUseCase;
        this.templateEngine = templateEngine;
    }

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDTO> loginWithGoogle(
        @RequestBody @Valid GoogleAuthRequestDTO dto
    ) {
        GoogleAuthResult result = loginWithGoogleUseCase.login(new LoginWithGoogleCommand(dto.getIdToken()));
        return ResponseEntity.ok(new GoogleAuthResponseDTO(
            result.accessToken(),
            result.refreshToken(),
            result.email(),
            result.needsOnboarding()
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        boolean valid = validateTokenUseCase.validate(token);

        return valid
            ? ResponseEntity.ok().build()
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody @Valid LoginRequestDTO dto
    ) {
        LoginResult result = loginUseCase.login(
            new LoginCommand(
                new Email(dto.getEmail()),
                dto.getPassword()
            )
        );

        return ResponseEntity.ok(new LoginResponseDTO(
            result.userId().value().toString(),
            result.email(),
            result.accessToken(),
            result.refreshToken(),
            result.needsOnboarding()
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
        @RequestBody @Valid ForgotPasswordRequestDTO dto
    ) {
        requestTemporaryPasswordUseCase.request(new RequestTemporaryPasswordCommand(dto.getEmail()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO dto) {
        resetPasswordUseCase.reset(new ResetPasswordCommand(
            new Email(dto.getEmail()), 
            dto.getTemporaryPassword(),
            dto.getNewPassword()
        ));
        return ResponseEntity.noContent().build();
    }

    // GET solo comprueba el token, sin efectos secundarios — así un escáner de seguridad de
    // correo (Safe Links, antivirus corporativos) puede visitarlo sin invalidarlo antes de tiempo.
    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showVerifyEmailConfirmation(@RequestParam String token) {
        if (!verifyEmailUseCase.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(templateEngine.process("verification/error", new Context()));
        }
        Context context = new Context();
        context.setVariable("token", token);
        return ResponseEntity.ok(templateEngine.process("verification/confirm", context));
    }

    @PostMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirmVerifyEmail(@RequestParam String token) {
        try {
            verifyEmailUseCase.verify(new VerifyEmailCommand(token));
            return ResponseEntity.ok(templateEngine.process("verification/success", new Context()));
        } catch (InvalidOrExpiredVerificationTokenException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(templateEngine.process("verification/error", new Context()));
        }
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<Void> resendVerificationEmail(@RequestBody @Valid ResendVerificationEmailRequestDTO dto) {
        resendVerificationEmailUseCase.resend(new ResendVerificationEmailCommand(new Email(dto.getEmail())));
        return ResponseEntity.accepted().build();
    }

}

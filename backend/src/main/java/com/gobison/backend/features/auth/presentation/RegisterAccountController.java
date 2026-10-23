package com.gobison.backend.features.auth.presentation;

import com.gobison.backend.features.auth.application.RegisterAccountService;
import com.gobison.backend.features.auth.presentation.dto.RegisterAccountRequest;
import com.gobison.backend.features.auth.presentation.dto.RegisterAccountResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterAccountController {
    private final RegisterAccountService registerAccountService;

    public RegisterAccountController(RegisterAccountService registerAccountService) {
        this.registerAccountService = registerAccountService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterAccountResponse> registerAccount(
            @Valid @RequestBody RegisterAccountRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        var account = registerAccountService.registerAccount(
                request.firstName(), request.lastName(), request.email(), request.password(),
                servletRequest, servletResponse
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterAccountResponse(
                        account.userId(),
                        account.firstName(),
                        account.lastName(),
                        account.email(),
                        account.createdAt()
                ));
    }
}
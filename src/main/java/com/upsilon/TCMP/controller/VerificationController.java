package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.service.IAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * @deprecated This controller has been deprecated as its functionality has been consolidated into {@link AuthController}.
 * Use {@link AuthController} for all authentication and verification related operations.
 */
@Deprecated
@Controller
public class VerificationController {

    private final IAuthenticationService authService;
    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    public VerificationController(IAuthenticationService authService) {
        this.authService = authService;
    }
}
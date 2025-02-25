package com.upsilon.TCMP.controller;

import com.upsilon.TCMP.dto.UserRegistrationDTO;
import com.upsilon.TCMP.service.IAuthenticationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/login")
    public String showLogin(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          @RequestParam(value = "message", required = false) String message,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", message != null ? message : "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("userRegistrationDTO")) {
            model.addAttribute("userRegistrationDTO", new UserRegistrationDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userRegistrationDTO") UserRegistrationDTO registrationDTO,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            logger.error("Validation errors in registration form");
            return "auth/register";
        }

        try {
            logger.info("Attempting to register user with email: {}", registrationDTO.getEmail());
            authService.register(registrationDTO);
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error during registration", e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            logger.debug("Processing email verification for token: {}", token);
            authService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("success", 
                "Your email has been successfully verified! You can now login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Invalid verification token: {}", token);
            redirectAttributes.addFlashAttribute("error", 
                "Invalid verification token. Please request a new verification email.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            logger.error("Expired verification token: {}", token);
            redirectAttributes.addFlashAttribute("error", 
                "This verification link has expired. Please request a new one.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error during email verification", e);
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred during email verification. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/verify/resend")
    public String resendVerification(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            logger.debug("Resending verification email to: {}", email);
            authService.sendVerificationEmail(email);
            redirectAttributes.addFlashAttribute("success", 
                "A new verification email has been sent. Please check your inbox.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error resending verification email", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to resend verification email. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                      RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting to initiate password reset for: {}", email);
            authService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("success", 
                "Password reset instructions have been sent to your email.");
        } catch (Exception e) {
            logger.error("Error initiating password reset", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                     @RequestParam("password") String password,
                                     @RequestParam("confirmPassword") String confirmPassword,
                                     RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting to reset password");
            authService.resetPassword(token, password, confirmPassword);
            redirectAttributes.addFlashAttribute("success", 
                "Password has been reset successfully. You can now login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }
    }
}
package com.upsilon.TCMP.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                String requestPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString();
                if (requestPath.startsWith("/assets/") || requestPath.startsWith("/static/") ||
                    requestPath.startsWith("/css/") || requestPath.startsWith("/js/") ||
                    requestPath.startsWith("/images/")) {
                    model.addAttribute("error", "Resource Not Found");
                    model.addAttribute("message",
                        "The requested resource '" + requestPath + "' could not be found. " +
                        "This may be due to cached references to old files. Try clearing your browser cache.");
                } else {
                    model.addAttribute("error", "Page Not Found");
                    model.addAttribute("message", "The requested page could not be found.");
                }
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Internal Server Error");
                model.addAttribute("message", "Something went wrong on our end.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Access Denied");
                model.addAttribute("message", "You don't have permission to access this resource.");
            } else {
                model.addAttribute("error", "Error " + statusCode);
                model.addAttribute("message", message != null ? message : "An unexpected error occurred.");
            }
        } else {
            model.addAttribute("status", "Error");
            model.addAttribute("error", "Unknown Error");
            model.addAttribute("message", "An unexpected error occurred.");
        }

        // Add stack trace in development mode only
        if (error != null && isDevelopment()) {
            model.addAttribute("trace", ((Exception) error).getMessage());
        }

        return "error";
    }

    private boolean isDevelopment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile == null || profile.equals("dev") || profile.equals("development");
    }
}
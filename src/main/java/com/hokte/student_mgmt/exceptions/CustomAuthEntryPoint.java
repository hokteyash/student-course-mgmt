package com.hokte.student_mgmt.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Read error set in the JwtFilter
        String errorType = (String) request.getAttribute("jwt_error");
        String message;

        if (errorType == null) {
            message = "Unauthorized request";
        } else {
            switch (errorType) {
                case "missing" -> message = "Missing Authorization header";
                case "expired" -> message = "JWT token has expired";
                case "malformed" -> message = "Malformed JWT token";
                case "invalid_signature" -> message = "Invalid JWT signature";
                case "invalid_token" -> message = "Invalid JWT token";
                default -> message = "Authentication failed";
            }
        }

        String json = """
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "%s"
                }
                """.formatted(message);

        response.getWriter().write(json);
    }
}
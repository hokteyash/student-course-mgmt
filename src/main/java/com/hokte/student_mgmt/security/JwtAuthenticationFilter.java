//package com.hokte.student_mgmt.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private ApplicationContext context;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String path = request.getServletPath();
//        if(path.startsWith("/api/auth/")){
//            filterChain.doFilter(request,response);
//            return;
//        }
//
//        String header = request.getHeader("Authorization");
//        String username = null;
//        String token = null;
//
//        if(header==null || !header.startsWith("Bearer ")){
//            filterChain.doFilter(request,response);
//            return;
//        }
//
//        token = header.substring(7);
//
//        try {
//
//            if(!jwtUtil.validateToken(token)){
//                filterChain.doFilter(request,response);
//                return;
//            }
//
//            username = jwtUtil.getUsernameFromToken(token);
//            UserDetails userDetails = context.getBean(UserDetailsService.class).loadUserByUsername(username);
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        } catch (Exception e) {
//            // let AuthenticationEntryPoint handle 401 JSON response
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request,response);
//
//    }
//}


// new code which is perfect handles expire, missing token

package com.hokte.student_mgmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ❌ Skip authentication for login/register URLs
        if (request.getServletPath().startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // ❌ No header → let EntryPoint return JSON error
        if (header == null || !header.startsWith("Bearer ")) {
            request.setAttribute("jwt_error", "missing");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String username = null;

        // ⭐⭐⭐ TOKEN PARSING + ERROR HANDLING ⭐⭐⭐
        try {
            Jws<Claims> claims = jwtUtil.parseJws(token);
            username = claims.getBody().getSubject();

        } catch (ExpiredJwtException e) {
            request.setAttribute("jwt_error", "expired");
        } catch (MalformedJwtException e) {
            request.setAttribute("jwt_error", "malformed");
        } catch (SignatureException e) {
            request.setAttribute("jwt_error", "invalid_signature");
        } catch (Exception e) {
            request.setAttribute("jwt_error", "invalid_token");
        }

        // ❌ Token invalid → do not authenticate
        if (request.getAttribute("jwt_error") != null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // ⭐⭐⭐ TOKEN IS VALID → AUTHENTICATE USER ⭐⭐⭐
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetailsService userDetailsService = context.getBean(UserDetailsService.class);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}


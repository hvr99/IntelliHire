package com.jobrec.application.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.List;
@Component public class InternalApiKeyFilter extends OncePerRequestFilter {
    @Value("${internal.api-key}") private String key;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) { if (!key.equals(request.getHeader("X-Internal-Key"))) { response.sendError(401, "Invalid or missing X-Internal-Key"); return; } SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL")))); }
        chain.doFilter(request, response);
    }
}

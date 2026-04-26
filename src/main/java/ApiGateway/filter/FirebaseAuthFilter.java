package ApiGateway.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Token no proporcionado");
            return;
        }

        String token = authHeader
            .substring(BEARER_PREFIX.length()).trim();

        try {
            FirebaseToken decoded = FirebaseAuth
                .getInstance()
                .verifyIdToken(token);

            request.setAttribute("uid", decoded.getUid());
            request.setAttribute("email", decoded.getEmail());
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            writeUnauthorized(response, "Token inválido o expirado");
        }
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            "{\"error\":\"unauthorized\",\"message\":\""
            + message + "\"}");
    }
}
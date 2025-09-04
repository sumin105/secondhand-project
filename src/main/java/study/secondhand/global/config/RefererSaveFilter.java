package study.secondhand.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RefererSaveFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.contains("/login")) {
                HttpSession session = request.getSession();
                session.setAttribute("PREVIOUS_PAGE", referer);
            }
        }
        filterChain.doFilter(request, response);
    }
}

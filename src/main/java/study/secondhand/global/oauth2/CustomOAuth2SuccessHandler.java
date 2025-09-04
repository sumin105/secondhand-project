package study.secondhand.global.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        HttpSession session = request.getSession(false);
        String targetUrl = "/";
        String previousPageFromSession = null;

        if (session != null) {
            Object previousPage = session.getAttribute("PREVIOUS_PAGE");
            if (previousPage != null) {
                previousPageFromSession = previousPage.toString();
                targetUrl = previousPageFromSession;
                session.removeAttribute("PREVIOUS_PAGE");
            }
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

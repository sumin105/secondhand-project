package study.secondhand.global.oauth2;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomAuthentication extends AbstractAuthenticationToken {

    private final UserDetails principal; // 사용자 정보

    public CustomAuthentication(UserDetails principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        setAuthenticated(true); // 인증된 상태로 설정
    }

    @Override
    public Object getCredentials() {
        return null; // 자격 증명은 필요없으므로 null 반환
    }

    @Override
    public Object getPrincipal() {
        return principal; // 사용자 정보 반환
    }
}

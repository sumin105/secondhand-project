package study.secondhand.global.oauth2;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public class OAuthAttributes {

    private final String oauthId;
    private final String name;
    private final String email;

    private OAuthAttributes(String oauthId, String name, String email) {
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
    }

    public static OAuthAttributes of(String provider, Map<String, Object> attributes) {
        if (provider.equals("kakao")) {
            return ofKakao(attributes);
        } else if (provider.equals("naver")) {
            return ofNaver(attributes);
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String id = Objects.toString(attributes.get("id"), "");
        String name = (String) profile.get("nickname");
        String email = (String) kakaoAccount.get("email");

        return new OAuthAttributes(id, name, email);
    }

    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String id = (String) response.get("id");
        String name = (String) response.get("name");
        String email = (String) response.get("email");

        return new OAuthAttributes(id, name, email);
    }

    @Override
    public String toString() {
        return "OAuthAttributes{" +
                "oauthId='" + oauthId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthAttributes that = (OAuthAttributes) o;
        return Objects.equals(oauthId, that.oauthId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oauthId, name, email);
    }
}

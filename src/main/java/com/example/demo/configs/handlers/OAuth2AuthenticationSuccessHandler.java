package com.example.demo.configs.handlers;

import com.example.demo.configs.AppProperties;
import com.example.demo.configs.jwt.JwtService;
import com.example.demo.configs.security.UserPrincipal;
import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.commons.enums.AuthProvider;
import com.example.demo.entities.Account;
import com.example.demo.repositories.queries.AccountQueryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final AccountQueryRepository accountQueryRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        Object principal = authentication.getPrincipal();
        String email;
        String name = null;

        // Kiểm tra login bằng Google OAuth2
        if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if (principal instanceof UserPrincipal userPrincipal) {
            // Login bằng form truyền thống
            email = userPrincipal.getEmail();
            name = userPrincipal.getName();
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }

        // Tìm account theo email, nếu không có thì tạo mới
        Account account = accountQueryRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account newAccount = Account.builder()
                            .email(email)
                            .provider(AuthProvider.google) // provider mặc định là Google
                            .role(AccountRole.CUSTOMER)       // role mặc định
                            .status(AccountStatus.ACTIVE) // status mặc định
                            .lastLoginAt(LocalDateTime.now())
                            .build();
                    return accountQueryRepository.save(newAccount);
                });

        // Cập nhật last login
        account.setLastLoginAt(LocalDateTime.now());
        accountQueryRepository.save(account);

        UserPrincipal userPrincipal = UserPrincipal.create(account);
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal); // Generate refresh token

        return UriComponentsBuilder.fromUriString(appProperties.getOauth2().getRedirectUri())
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken) // Add refresh token
                .build(true) // encode tokens
                .toUriString();
    }
}
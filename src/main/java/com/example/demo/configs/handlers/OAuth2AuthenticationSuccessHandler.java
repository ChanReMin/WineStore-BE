package com.example.demo.configs.handlers;

import com.example.demo.configs.AppProperties;
import com.example.demo.configs.jwt.JwtService;
import com.example.demo.configs.security.UserPrincipal;
import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.commons.enums.AuthProvider;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.UserCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final AccountCommandRepository accountCommandRepository;
    private final AccountQueryRepository accountQueryRepository ;
    private final UserCommandRepository userCommandRepository ;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("=== OAuth2 Authentication Success Handler Started ===");
        log.info("Principal: {}", authentication.getPrincipal().getClass().getSimpleName());

        try {
            String targetUrl = determineTargetUrl(request, response, authentication);
            log.info("Target URL generated: {}", targetUrl);

            if (response.isCommitted()) {
                log.warn("Response has already been committed. Unable to redirect to {}", targetUrl);
                return;
            }

            clearAuthenticationAttributes(request);
            log.debug("Authentication attributes cleared");

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            log.info("=== OAuth2 Authentication Success Handler Completed ===");
        } catch (Exception e) {
            log.error("Error in OAuth2 authentication success handler", e);
            throw e;
        }
    }

    @Transactional
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        log.info("=== Determining Target URL ===");

        Object principal = authentication.getPrincipal();
        String email;
        final String name;


        if (principal instanceof OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            log.info("✓ OAuth2 login detected");
            log.info("  Email: {}", email);
            log.info("  Name: {}", name);
            log.info("  Attributes: {}", oAuth2User.getAttributes().keySet());
        } else if (principal instanceof UserPrincipal userPrincipal) {

            email = userPrincipal.getEmail();
            name = userPrincipal.getName();
            log.info("✓ Form login detected");
            log.info("  Email: {}", email);
            log.info("  Name: {}", name);
        } else {
            log.error("✗ Unknown principal type: {}", principal.getClass());
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }


        log.debug("Searching for account with email: {}", email);
        Account account = accountQueryRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Account not found, creating new account...");
                    return createNewAccount(email, name);
                });

        log.info("Account ID: {}", account.getId());


        account.setLastLoginAt(LocalDateTime.now());
        accountCommandRepository.save(account);
        log.info("Updated last login at: {}", account.getLastLoginAt());


        log.debug("Creating UserPrincipal from account...");
        UserPrincipal userPrincipal = UserPrincipal.create(account);

        // Generate tokens
        log.debug("Generating access token...");
        String accessToken = jwtService.generateToken(userPrincipal);
        log.debug("Access token generated (length: {})", accessToken.length());

        log.debug("Generating refresh token...");
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        log.debug("Refresh token generated (length: {})", refreshToken.length());


        String redirectUri = appProperties.getOauth2().getRedirectUri();
        log.info("Redirect URI: {}", redirectUri);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build(true)
                .toUriString();

        log.info("Target URL ready for redirect");
        return targetUrl;
    }


    private Account createNewAccount(String email, String name) {
        try {
            log.info("Creating new account and user for email: {}", email);


            String[] nameParts = parseFullName(name);
            String firstName = nameParts[0];
            String lastName = nameParts[1];


            Account newAccount = Account.builder()
                    .email(email)
                    .provider(AuthProvider.google)
                    .role(AccountRole.CUSTOMER)
                    .status(AccountStatus.ACTIVE)
                    .lastLoginAt(LocalDateTime.now())
                    .build();


            Account savedAccount = accountCommandRepository.save(newAccount);
            log.info("Account saved successfully with ID: {}, Email: {}", savedAccount.getId(), email);


            User newUser = User.builder()
                    .account(savedAccount)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();

            // Save User
            User savedUser = userCommandRepository.save(newUser);
            log.info("User saved successfully with ID: {}, Account ID: {}, Name: {} {}",
                    savedUser.getId(), savedAccount.getId(), firstName, lastName);

            return savedAccount;

        } catch (Exception e) {
            log.error("Error creating new account and user for email: {}", email, e);
            throw new RuntimeException("Failed to create account and user: " + e.getMessage(), e);
        }
    }


    private String[] parseFullName(String fullName) {
        log.debug("=== Parsing Full Name ===");
        log.debug("Input: '{}'", fullName);


        if (fullName == null || fullName.trim().isEmpty()) {
            log.warn("Full name is empty or null, using default values");
            return new String[]{"", ""};
        }


        String[] nameParts = fullName.trim().split("\\s+");
        log.debug("Split parts count: {}", nameParts.length);


        if (nameParts.length == 0) {
            log.warn("No name parts found, using default values");
            return new String[]{"", ""};
        }


        String firstName = nameParts[0];


        String lastName = nameParts.length > 1
                ? String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length))
                : "";

        log.debug("✓ Name parsed successfully");
        log.debug("  FirstName: '{}' (length: {})", firstName, firstName.length());
        log.debug("  LastName: '{}' (length: {})", lastName, lastName.length());

        return new String[]{firstName, lastName};
    }
}
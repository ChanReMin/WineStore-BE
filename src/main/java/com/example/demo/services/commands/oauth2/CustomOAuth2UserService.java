package com.example.demo.services.commands.oauth2;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.commons.enums.AuthProvider;
import com.example.demo.configs.security.UserPrincipal;
import com.example.demo.dtos.oauth2.OAuth2UserInfo;
import com.example.demo.dtos.oauth2.OAuth2UserInfoFactory;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.exceptions.OAuth2AuthenticationProcessingException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final AccountQueryRepository accountQueryRepository;
    private final AccountCommandRepository accountCommandRepository;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<Account> accountOptional = accountQueryRepository.findByEmail(oAuth2UserInfo.getEmail());
        Account account;
        if(accountOptional.isPresent()) {
            account = accountOptional.get();
            if(!account.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        account.getProvider() + " account. Please use your " + account.getProvider() +
                        " account to login.");
            }
            account = updateExistingUser(account, oAuth2UserInfo);
        } else {
            account = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(account, oAuth2User.getAttributes());
    }
    @Transactional(transactionManager = "writeTransactionManager")
    protected Account registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Account account = new Account();
        User user = new User();

        account.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        account.setProviderId(oAuth2UserInfo.getId());
        account.setEmail(oAuth2UserInfo.getEmail());
        account.setRole(AccountRole.CUSTOMER);
        account.setStatus(AccountStatus.ACTIVE);
        
        user.setFirstName(oAuth2UserInfo.getName());
        user.setAvatar(oAuth2UserInfo.getImageUrl());
        user.setAccount(account);
        account.setUser(user);
        
        return accountCommandRepository.saveAndFlush(account);
    }
    @Transactional(transactionManager = "writeTransactionManager")
    protected Account updateExistingUser(Account existingAccount, OAuth2UserInfo oAuth2UserInfo) {
        existingAccount.getUser().setFirstName(oAuth2UserInfo.getName());
        existingAccount.getUser().setAvatar(oAuth2UserInfo.getImageUrl());
        return accountCommandRepository.saveAndFlush(existingAccount);
    }

}
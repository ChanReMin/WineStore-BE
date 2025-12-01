package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.dtos.responses.profile.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.entities.UserAddress;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.UserQueryRepository;
import com.example.demo.services.queries.ProfileQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private final AccountQueryRepository accountQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public ProfileResponse getProfile(Long accountId) {
        log.debug("Getting profile for user: {}", accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        // Get loyalty points = total completed orders
        Integer loyaltyPoints = userQueryRepository.countOrdersByUserId(user.getId());

        return ProfileResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .username(account.getEmail()) // Using email as username
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender() != null ? user.getGender().ordinal() : null)
                .role(account.getRole().name())
                .loyaltyPoints(loyaltyPoints != null ? loyaltyPoints : 0)
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public AddressListResponse getAddresses(Long accountId) {
        log.debug("Getting addresses for user: {}", accountId);

        Account account = accountQueryRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        List<UserAddress> addresses = userQueryRepository.findAddressesByUserId(user.getId());

        if (addresses == null || addresses.isEmpty()) {
            return AddressListResponse.builder()
                    .addresses(Collections.emptyList())
                    .build();
        }

        List<AddressListResponse.AddressDetail> addressDetails = addresses.stream()
                .map(this::mapToAddressDetail)
                .collect(Collectors.toList());

        return AddressListResponse.builder()
                .addresses(addressDetails)
                .build();
    }

    private AddressListResponse.AddressDetail mapToAddressDetail(UserAddress address) {
        return AddressListResponse.AddressDetail.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine(address.getAddressLine())
                .ward(null) // Not in current entity, add if needed
                .district(address.getState())
                .city(address.getCity())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .addressType("home") // Default value, add field if needed
                .createdAt(address.getCreatedAt())
                .build();
    }
}

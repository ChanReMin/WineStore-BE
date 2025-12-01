package com.example.demo.services.queries;

import com.example.demo.dtos.responses.profile.*;

public interface ProfileQueryService {
    ProfileResponse getProfile(Long userId);
    AddressListResponse getAddresses(Long userId);
}

package com.example.demo.configs;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.example.demo.configs.security.UserPrincipal; // New import


@Component
public class SecurityUtils {

    /**
     * Lấy email của user hiện tại từ Security Context
     * Email được lưu trong Principal (UserDetails.getUsername())
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Principal có thể là UserDetails hoặc String (anonymous user)
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // Email
        } else if (principal instanceof String) {
            return (String) principal; // Email
        }

        return null;
    }

    /**
     * Kiểm tra xem user hiện tại có phải là chủ sở hữu của resource không
     */
    public boolean isOwner(String ownerEmail) {
        String currentEmail = getCurrentUserEmail();
        return currentEmail != null && currentEmail.equals(ownerEmail);
    }

    /**
     * Kiểm tra xem user hiện tại có authenticated không
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String &&
                "anonymousUser".equals(authentication.getPrincipal()));
    }

    /**
     * Lấy UUID của user hiện tại từ Security Context
     */
    public static Long getCurrentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        return null;
    }

}
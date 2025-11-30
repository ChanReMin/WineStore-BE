package com.example.demo.services.commands;

import com.example.demo.dtos.commands.user.*;
import com.example.demo.dtos.responses.user.ChangeUserStatusResponse;
import com.example.demo.dtos.responses.user.RestoreUserResponse;
import com.example.demo.dtos.responses.user.UpdateAvatarResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserCommandService {
    void updateUser(Long userId, UpdateUserRequest request);

    ChangeUserStatusResponse changeUserStatus(Long userId, ChangeUserStatusRequest request);
    void deleteUser(Long userId, boolean permanent);
    RestoreUserResponse restoreUser(Long userId);
    String sendNotificationToUser(Long userId, SendNotificationRequest request);
    BulkActionResponse bulkActions(BulkActionRequest request);
    UpdateAvatarResponse updateAvatar(Long userId, UpdateAvatarRequest avatarFile);
}

package com.example.demo.services.commands;

import com.example.demo.dtos.commands.user.*;
import com.example.demo.dtos.responses.user.*;

public interface UserCommandService {
    ChangeUserStatusResponse changeUserStatus(Long userId, ChangeUserStatusRequest request);
    ChangeUserRoleResponse changeUserRole(Long userId, ChangeUserRoleRequest request);
    void deleteUser(Long userId, boolean permanent);
    RestoreUserResponse restoreUser(Long userId);
    BulkActionResponse bulkActions(BulkActionRequest request);
    UpdateAvatarResponse updateAvatar(Long userId, UpdateAvatarRequest avatarFile);
}

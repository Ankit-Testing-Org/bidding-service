package com.evatech.bidplatform.user.service;


import com.evatech.bidplatform.user.dto.UserLookupType;
import com.evatech.bidplatform.user.dto.request.RegisterRequest;
import com.evatech.bidplatform.user.dto.request.UpdateUserRequest;
import com.evatech.bidplatform.user.entity.User;
import lombok.NonNull;


public interface UserService {
    User register(@NonNull RegisterRequest request);
    void deleteUser(@NonNull String username);
    void sendVerificationToken(@NonNull User user);
    User fetchUser(@NonNull UserLookupType type, @NonNull String value);
    User updateDetails(@NonNull User user, @NonNull UpdateUserRequest updateUserRequest);

}


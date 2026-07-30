package com.evatech.bidplatform.user.mapper;

import com.evatech.bidplatform.user.dto.response.UserResponse;
import com.evatech.bidplatform.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}


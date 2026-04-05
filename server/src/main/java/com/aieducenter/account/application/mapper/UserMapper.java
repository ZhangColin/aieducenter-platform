package com.aieducenter.account.application.mapper;

import com.aieducenter.account.application.dto.response.UserResponse;
import com.aieducenter.account.domain.aggregate.User;
import com.cartisan.web.mapper.DomainMapper;
import org.mapstruct.Mapper;

/**
 * User 聚合根与 UserResponse DTO 的转换器。
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends DomainMapper<User, UserResponse> {
    @Override
    UserResponse convert(User user);

    // convertList() 和 convertSet() 由 DomainMapper 基类提供
}

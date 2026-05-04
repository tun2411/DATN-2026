package com.example.bedatn.converter;

import com.example.bedatn.dto.request.UserRequest;
import com.example.bedatn.dto.response.RoleResponse;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.documents.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserConverter {

    @Autowired
    private ModelMapper modelMapper;

    public UserResponse toUserResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserResponse result = modelMapper.map(entity, UserResponse.class);
        // ModelMapper có thể ghi đè giá trị mặc định thành null.
        if (result.getRoles() == null) {
            result.setRoles(new ArrayList<RoleResponse>());
        }
        return result;
    }

    public UserEntity toEntity(UserRequest dto) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, UserEntity.class);
    }
}

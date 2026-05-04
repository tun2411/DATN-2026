package com.example.bedatn.converter;

import com.example.bedatn.dto.response.RoleResponse;
import com.example.bedatn.documents.RoleEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter {
	
	@Autowired
	private ModelMapper modelMapper;
	
	public RoleResponse convertToDto(RoleEntity entity) {
		RoleResponse result = modelMapper.map(entity, RoleResponse.class);
        return result;
    }

    public RoleEntity convertToEntity(RoleResponse dto) {
    	RoleEntity result = modelMapper.map(dto, RoleEntity.class);
        return result;
    }
}

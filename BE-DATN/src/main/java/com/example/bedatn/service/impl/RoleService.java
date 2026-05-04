package com.example.bedatn.service.impl;

import com.example.bedatn.converter.RoleConverter;
import com.example.bedatn.dto.response.RoleResponse;
import com.example.bedatn.documents.RoleEntity;
import com.example.bedatn.repository.RoleRepository;
import com.example.bedatn.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService implements IRoleService {
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private RoleConverter roleConverter;

	public List<RoleResponse> findAll() {
		List<RoleEntity> roleEntities = roleRepository.findAll();
		List<RoleResponse> list = new ArrayList<>();
		roleEntities.forEach(item -> {
			RoleResponse rr = roleConverter.convertToDto(item);
			list.add(rr);
		});
		return list;
	}

	@Override
	public Map<String, String> getRoles() {
		Map<String,String> roleTerm = new HashMap<>();
		List<RoleEntity> roleEntity = roleRepository.findAll();
		for(RoleEntity item : roleEntity){
			RoleResponse rr = roleConverter.convertToDto(item);
			roleTerm.put(rr.getCode(), rr.getName());
		}
		return roleTerm;
	}
}

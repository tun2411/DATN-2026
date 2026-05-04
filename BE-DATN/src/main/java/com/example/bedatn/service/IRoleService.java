package com.example.bedatn.service;

import com.example.bedatn.dto.response.RoleResponse;

import java.util.List;
import java.util.Map;

public interface IRoleService {
	List<RoleResponse> findAll();
	Map<String,String> getRoles();
}

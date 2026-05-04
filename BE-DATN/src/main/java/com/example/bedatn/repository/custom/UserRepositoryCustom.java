package com.example.bedatn.repository.custom;

import com.example.bedatn.documents.UserEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {
	List<UserEntity> findByRole(String roleCode);
	List<UserEntity> getAllUsers(Pageable pageable);
	int countTotalItem();
}

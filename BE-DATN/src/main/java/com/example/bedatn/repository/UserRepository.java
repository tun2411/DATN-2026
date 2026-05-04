package com.example.bedatn.repository;

import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.repository.custom.UserRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<UserEntity, Long> , UserRepositoryCustom {
    UserEntity findOneByUserNameAndStatus(String name, int status);
    UserEntity findOneByEmailAndStatus(String email, int status);
    Page<UserEntity> findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseAndStatusNot(String userName, String fullName, int status,
                                                                                                  Pageable pageable);
    List<UserEntity> findByStatusAndRoleCode(Integer status, String roleCode);
    Page<UserEntity> findByStatusNot(int status, Pageable pageable);
    long countByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseAndStatusNot(String userName, String fullName, int status);
    long countByStatusNot(int status);
    UserEntity findOneByUserName(String userName);
    List<UserEntity> findByIdIn(List<Long> ids);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);

    /** Số điện thoại đã dùng bởi tài khoản đang hoạt động (status = 1) */
    boolean existsByPhoneAndStatus(String phone, Integer status);
}

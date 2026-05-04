package com.example.bedatn.service.impl;

import com.example.bedatn.constant.SystemConstant;
import com.example.bedatn.converter.UserConverter;
import com.example.bedatn.dto.request.PasswordChangeRequest;
import com.example.bedatn.dto.request.UserRequest;
import com.example.bedatn.dto.response.RoleResponse;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.documents.*;
import com.example.bedatn.exception.MyException;
import com.example.bedatn.repository.*;
import com.example.bedatn.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private AssignmentBuildingRepository assignmentBuildingRepository;

    @Autowired
    private AssignmentCustomerRepository assignmentCustomerRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public boolean existsByUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /** Tài khoản đang hoạt động đã dùng số điện thoại này (đã chuẩn hóa). */
    public boolean existsActiveUserWithPhone(String normalizedPhone) {
        if (normalizedPhone == null || normalizedPhone.isEmpty()) {
            return false;
        }
        return userRepository.existsByPhoneAndStatus(normalizedPhone, 1);
    }

    private RoleEntity requireRole(String code) {
        RoleEntity roleEntity = roleRepository.findOneByCode(code);
        if (roleEntity == null) {
            throw new IllegalStateException("Role '" + code + "' chưa tồn tại trong hệ thống");
        }
        return roleEntity;
    }

    private UserEntity findByLoginAndStatus(String login, int status) {
        if (login == null || login.trim().isEmpty()) {
            return null;
        }
        String normalized = login.trim();
        UserEntity byUserName = userRepository.findOneByUserNameAndStatus(normalized, status);
        if (byUserName != null) {
            return byUserName;
        }
        return userRepository.findOneByEmailAndStatus(normalized, status);
    }

    public void registerUser(UserRequest userRequest) {
        UserEntity user = userConverter.toEntity(userRequest);
        if (user.getId() == null) {
            user.setId(System.currentTimeMillis());
        }
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(new Date());
        }
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRoleCode("USER");
        userRepository.save(user);

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setId(System.currentTimeMillis());
        RoleEntity roleEntity = requireRole("USER");
        userRoleEntity.setUserId(user.getId());
        userRoleEntity.setRoleId(roleEntity.getId());
        userRoleRepository.save(userRoleEntity);
    }

    @Override
    public UserResponse authenticate(String login, String password) {
        UserEntity userEntity = findByLoginAndStatus(login, 1);
        if (userEntity == null) {
            return null;
        }
        if (password == null || !passwordEncoder.matches(password, userEntity.getPassword())) {
            return null;
        }
        UserResponse dto = userConverter.toUserResponse(userEntity);
        dto.setRoleCode(userEntity.getRoleCode());
        if (userEntity.getRoleCode() != null) {
            RoleEntity roleEntity = roleRepository.findOneByCode(userEntity.getRoleCode());
            if (roleEntity != null) {
                RoleResponse roleResponse = new RoleResponse();
                roleResponse.setCode(roleEntity.getCode());
                roleResponse.setName(roleEntity.getName());
                dto.getRoles().add(roleResponse);
            }
        }
        return dto;
    }

    @Override
    public UserResponse findOneByUserNameAndStatus(String name, int status) {
        UserEntity entity = userRepository.findOneByUserNameAndStatus(name, status);
        UserResponse dto = userConverter.toUserResponse(entity);
        if (dto != null && entity != null && entity.getRoleCode() != null) {
            RoleEntity roleEntity = roleRepository.findOneByCode(entity.getRoleCode());
            if (roleEntity != null) {
                RoleResponse roleResponse = new RoleResponse();
                roleResponse.setCode(roleEntity.getCode());
                roleResponse.setName(roleEntity.getName());
                dto.getRoles().add(roleResponse);
            }
        }
        return dto;
    }

    @Override
    public List<UserResponse> getUsers(String searchValue, Pageable pageable) {
        Page<UserEntity> users;
        if (StringUtils.isNotBlank(searchValue)) {
            users = userRepository.findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseAndStatusNot(searchValue, searchValue, 0, pageable);
        } else {
            users = userRepository.findByStatusNot(0, pageable);
        }
        List<UserEntity> newsEntities = users.getContent();
        List<UserResponse> result = new ArrayList<>();
        for (UserEntity userEntity : newsEntities) {
            UserResponse userResponse = userConverter.toUserResponse(userEntity);
            userResponse.setRoleCode(userEntity.getRoleCode());
            result.add(userResponse);
        }
        return result;
    }

    @Override
    public List<UserResponse> getAllUsers(Pageable pageable) {
        List<UserEntity> userEntities = userRepository.getAllUsers(pageable);
        List<UserResponse> results = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            UserResponse userResponse = userConverter.toUserResponse(userEntity);
            userResponse.setRoleCode(userEntity.getRoleCode());
            results.add(userResponse);
        }
        return results;
    }

    @Override
    public List<UserEntity> getUsersByBuildingId(Long id) {
        List<AssignmentBuildingEntity> assignments = assignmentBuildingRepository.findByBuildingId(id);
        return assignments.stream()
                .map(AssignmentBuildingEntity::getStaffId)
                .map(userId -> userRepository.findById(userId).orElse(null))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<UserEntity> getUsersByCustomerId(Long customerId) {
        List<AssignmentCustomerEntity> assignments = assignmentCustomerRepository.findByCustomerId(customerId);
        return assignments.stream()
                .map(AssignmentCustomerEntity::getStaffId)
                .map(userId -> userRepository.findById(userId).orElse(null))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, String> getStaffs() {
        Map<Long, String> listStaffs = new HashMap<>();
        List<UserEntity> staffs = userRepository.findByStatusAndRoleCode(1, "STAFF");
        for (UserEntity staff : staffs) {
            listStaffs.put(staff.getId(), staff.getFullName());
        }
        return listStaffs;
    }

    @Override
    public int countTotalItems() {
        return userRepository.countTotalItem();
    }

    @Override
    public int getTotalItems(String searchValue) {
        int totalItem;
        if (StringUtils.isNotBlank(searchValue)) {
            totalItem = (int) userRepository.countByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCaseAndStatusNot(searchValue, searchValue, 0);
        } else {
            totalItem = (int) userRepository.countByStatusNot(0);
        }
        return totalItem;
    }

    @Override
    public UserResponse findOneByUserName(String userName) {
        UserEntity userEntity = userRepository.findOneByUserName(userName);
        UserResponse userResponse = userConverter.toUserResponse(userEntity);
        if (userEntity != null) {
            userResponse.setRoleCode(userEntity.getRoleCode());
        }
        return userResponse;
    }

    @Override
    public UserResponse findUserById(long id) {
        UserEntity entity = userRepository.findById(id).get();
        UserResponse dto = userConverter.toUserResponse(entity);
        dto.setRoleCode(entity.getRoleCode());
        return dto;
    }

    @Override
    @Transactional
    public UserResponse insert(UserRequest newUser) {
        UserEntity userEntity = userConverter.toEntity(newUser);
        if (userEntity.getId() == null) {
            userEntity.setId(System.currentTimeMillis());
        }
        if (userEntity.getCreatedDate() == null) {
            userEntity.setCreatedDate(new Date());
        }
        userEntity.setRoleCode(newUser.getRoleCode());
        userEntity.setStatus(1);
        userEntity.setPassword(passwordEncoder.encode(SystemConstant.PASSWORD_DEFAULT));
        UserEntity savedUser = userRepository.save(userEntity);
        RoleEntity role = requireRole(newUser.getRoleCode());
        if (role != null) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setId(System.currentTimeMillis());
            userRole.setUserId(savedUser.getId());
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        }
        return userConverter.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest updateUser) {
        UserEntity oldUser = userRepository.findById(id).get();
        UserEntity userEntity = userConverter.toEntity(updateUser);
        userEntity.setId(oldUser.getId());
        userEntity.setCreatedDate(oldUser.getCreatedDate());
        userEntity.setCreatedBy(oldUser.getCreatedBy());
        userEntity.setUserName(oldUser.getUserName());
        userEntity.setStatus(oldUser.getStatus());
        userEntity.setRoleCode(updateUser.getRoleCode());
        userEntity.setPassword(oldUser.getPassword());
        UserEntity saved = userRepository.save(userEntity);
        RoleEntity role = requireRole(updateUser.getRoleCode());
        userRoleRepository.deleteByUserId(saved.getId());
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(System.currentTimeMillis());
        userRole.setUserId(saved.getId());
        userRole.setRoleId(role.getId());
        userRoleRepository.save(userRole);
        return userConverter.toUserResponse(saved);
    }

    @Override
    @Transactional
    public void updatePassword(long id, PasswordChangeRequest passwordChangeRequest) throws MyException {
        UserEntity user = userRepository.findById(id).get();
        if (passwordEncoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())
                && passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new MyException(SystemConstant.CHANGE_PASSWORD_FAIL);
        }
    }

    @Override
    @Transactional
    public UserResponse resetPassword(long id) {
        UserEntity userEntity = userRepository.findById(id).get();
        userEntity.setPassword(passwordEncoder.encode(SystemConstant.PASSWORD_DEFAULT));
        return userConverter.toUserResponse(userRepository.save(userEntity));
    }

    @Override
    @Transactional
    public UserResponse updateProfileOfUser(String username, UserRequest updateUser) {
        UserEntity oldUser = userRepository.findOneByUserName(username);
        oldUser.setFullName(updateUser.getFullName());
        return userConverter.toUserResponse(userRepository.save(oldUser));
    }

    @Override
    @Transactional
    public void delete(long[] ids) {
        for (Long item : ids) {
            UserEntity userEntity = userRepository.findById(item).get();
            userEntity.setStatus(0);
            userRepository.save(userEntity);
        }
    }
}

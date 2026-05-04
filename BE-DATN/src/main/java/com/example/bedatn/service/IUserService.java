package com.example.bedatn.service;

import com.example.bedatn.dto.request.PasswordChangeRequest;
import com.example.bedatn.dto.request.UserRequest;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.documents.UserEntity;
import com.example.bedatn.exception.MyException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IUserService {
    UserResponse findOneByUserNameAndStatus(String name, int status);
    List<UserResponse> getUsers(String searchValue, Pageable pageable);
    int getTotalItems(String searchValue);
    UserResponse findOneByUserName(String userName);
    UserResponse findUserById(long id);
    UserResponse insert(UserRequest userRequest);
    UserResponse update(Long id, UserRequest userRequest);
    void updatePassword(long id, PasswordChangeRequest passwordChangeRequest) throws MyException;
    UserResponse resetPassword(long id);
    UserResponse updateProfileOfUser(String username, UserRequest userRequest);
    void delete(long[] ids);
    List<UserResponse> getAllUsers(Pageable pageable);
    List<UserEntity> getUsersByBuildingId(Long buildingId);
    List<UserEntity> getUsersByCustomerId(Long customerId);
    Map<Long, String> getStaffs();
    int countTotalItems();
    UserResponse authenticate(String login, String password);
}

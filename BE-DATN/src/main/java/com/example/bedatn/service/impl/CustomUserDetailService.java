package com.example.bedatn.service.impl;

import com.example.bedatn.dto.response.RoleResponse;
import com.example.bedatn.dto.response.UserResponse;
import com.example.bedatn.security.MyUserDetail;
import com.example.bedatn.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        UserResponse user = userService.findOneByUserNameAndStatus(name, 1);
        if (user == null) {
            throw new UsernameNotFoundException("Username not found");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (RoleResponse role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }
        MyUserDetail myUserDetail = new MyUserDetail(name, user.getPassword(), true, true, true, true, authorities);
        BeanUtils.copyProperties(user, myUserDetail);
        return myUserDetail;
    }
}

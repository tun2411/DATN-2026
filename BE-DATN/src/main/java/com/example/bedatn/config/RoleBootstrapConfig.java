package com.example.bedatn.config;

import com.example.bedatn.documents.RoleEntity;
import com.example.bedatn.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoleBootstrapConfig implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleBootstrapConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> defaultRoles = Map.of(
                "USER", "User",
                "STAFF", "Staff",
                "MANAGER", "Manager"
        );
        for (Map.Entry<String, String> role : defaultRoles.entrySet()) {
            if (roleRepository.findOneByCode(role.getKey()) == null) {
                RoleEntity entity = new RoleEntity();
                entity.setId(System.currentTimeMillis() + role.getKey().hashCode());
                entity.setCode(role.getKey());
                entity.setName(role.getValue());
                roleRepository.save(entity);
            }
        }
    }
}

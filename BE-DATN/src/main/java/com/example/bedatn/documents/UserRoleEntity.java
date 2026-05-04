package com.example.bedatn.documents;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_role")
@Getter
@Setter
public class UserRoleEntity extends BaseEntity{
	private Long userId;

	private Long roleId;
}

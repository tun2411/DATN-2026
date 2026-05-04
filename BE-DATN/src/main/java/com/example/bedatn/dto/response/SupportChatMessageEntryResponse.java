package com.example.bedatn.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SupportChatMessageEntryResponse {
    private String role;
    private String body;
    private Date createdAt;
}

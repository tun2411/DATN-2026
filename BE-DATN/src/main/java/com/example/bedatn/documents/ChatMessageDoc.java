package com.example.bedatn.documents;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ChatMessageDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /** USER | BOT | STAFF */
    private String role;
    private String body;
    private Date createdAt;
}

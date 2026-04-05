package com.hackassist.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {
    private String uid;
    private String name;
    private String email;
    private String photoUrl;
    private Boolean authenticated;
}

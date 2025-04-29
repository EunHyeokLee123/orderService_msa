package com.playdata.userservice.user.dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginReqDTO {

    private String email;
    private String password;

}

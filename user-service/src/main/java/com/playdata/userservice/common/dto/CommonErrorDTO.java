package com.playdata.userservice.common.dto;


import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommonErrorDTO {

    private int statusCode;
    private String statusMessage;

    public CommonErrorDTO(HttpStatus httpStatus, String statusMessage) {
        this.statusCode = httpStatus.value();
        this.statusMessage = statusMessage;
    }



}

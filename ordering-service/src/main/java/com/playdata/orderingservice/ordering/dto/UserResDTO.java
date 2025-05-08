package com.playdata.orderingservice.ordering.dto;

import com.playdata.orderingservice.common.entity.Address;
import com.playdata.orderingservice.common.auth.Role;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResDTO {

    private String email;
    private Long id;
    private String name;
    private Role role;
    private Address address;

    private boolean dummyFlag;
}



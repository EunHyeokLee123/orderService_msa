package com.playdata.userservice.user.dto;


import com.playdata.userservice.common.entity.Address;
import com.playdata.userservice.user.entity.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

// 데이터 단순 운반용 객체, DTO
// 프론트 단으로 전달할, 혹은 전달받는 데이터는 DTO를 따로 선언하여 포장하는 것을 권장
// Entity는 DB와 밀접하게 연관되어 있음
// -> 화면단에서 전달된 데이터가 Entity를 초기화하기에 부족할 수 있고,
// 화면단에 노출되면 안되는 데이터가 존재할 수도 있는 가능성이 있기에.
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSaveReqDTO {

    private String name;

    // 에러 발생 시 내보낼 메시지도 설정 가능
    @NotEmpty(message = "이메일은 필수입니다.")
    private String email;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    @Size(min = 8,message = "비밀번호는 최소 8자 이상입니다.")
    private String password;

    private Address address;

    // dto가 가진 정보를 토대로 User Entity를 생성해서 리턴하는 메소드
    public User toEntity(PasswordEncoder encoder) {
        return User.builder()
                .name(this.name)
                .email(this.email)
                .password(encoder.encode(this.password))
                .address(this.address)
                .build();
    }

}

package com.playdata.userservice.user.service;


import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.user.dto.UserLoginReqDTO;
import com.playdata.userservice.user.dto.UserResDTO;
import com.playdata.userservice.user.dto.UserSaveReqDTO;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Service로 Bean 등록(Component와 동일하지만, 의미를 명시)
@Service
@RequiredArgsConstructor
public class UserService {

    // service는 repository에 의존하고 있음 -> repository의 기능을 써야 함.
    // repository 객체를 자동으로 주입받자. (JPA가 만들어서 컨테이너에 등록해 놓음.)
    private final UserRepository userRepository;

    // 비밀번호를 암호화해서 DB에 저장하기 위해서 사용하는 객체
    private final PasswordEncoder encoder;

    // controller가 이 메소드를 호출할 것임.
    // 전달받은 DTO를 매개변수로 넘길 것임.
    public User userCreate(UserSaveReqDTO dto) {
        Optional<User> foundEmail =
                userRepository.findByEmail(dto.getEmail());
        // 이미 존재하는 이메일인 경우 -> 회원가입 불가
        if (foundEmail.isPresent()) {
            // 이미 존재하는 이메일이라는 에러를 발생 -> controller가 이 에러를 처리
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 이메일 중복 안됨 -> 회원가입 진행
        // dto를 entity로 변환하는 로직
        User user = dto.toEntity(encoder);
        return userRepository.save(user);

    }

    public User login(UserLoginReqDTO dto) {
        // 이메일로 user 조회하기
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User nt Found!")
        );

        // 비밀번호 확인하기 (암호화 되어 있으니 encoder에게 부탁)
        if(!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // myPage에서 회원정보를 불러오는 메소드
    public UserResDTO myInfo() {
        // Security 컨테이너에 있는 User 정보를 가져오자
        TokenUserInfo userInfo = 
                // 필터에서 세팅한 시큐리티 인증 정보를 불러오는 메소드
                (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(userInfo.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User nt Found!")
        );

        return user.fromEntity();

    }

    public List<UserResDTO> userList(Pageable pageable) {
        // Pageable 객체를 직접 생성할 필요가 없음
        // Controller가 보내줌.

        Page<User> all = userRepository.findAll(pageable);

        // 실질적인 데이터
        List<UserResDTO> content = all.getContent().stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());

        return content;
    }

    public void saveRefreshToken(String refreshToken, String email) {



    }

    public User findById(String id) {

    return userRepository.findById(Long.parseLong(id)).orElseThrow(
            () -> new EntityNotFoundException("User nt Found!")
    );
    }
}

package com.playdata.userservice.user.service;


import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.common.dto.KakaoUserDto;
import com.playdata.userservice.user.dto.UserLoginReqDTO;
import com.playdata.userservice.user.dto.UserResDto;
import com.playdata.userservice.user.dto.UserSaveReqDTO;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Service로 Bean 등록(Component와 동일하지만, 의미를 명시)
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    // service는 repository에 의존하고 있음 -> repository의 기능을 써야 함.
    // repository 객체를 자동으로 주입받자. (JPA가 만들어서 컨테이너에 등록해 놓음.)
    private final UserRepository userRepository;

    private final MailSenderService mailSenderService;

    // 비밀번호를 암호화해서 DB에 저장하기 위해서 사용하는 객체
    private final PasswordEncoder encoder;

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key 상수
    private static final String VERIFICATION_CODE_KEY = "email_verify:code:";
    private static final String VERIFICATION_ATTEMPT_KEY = "email_verify:attempt:";
    private static final String VERIFICATION_BLOCK_KEY = "email_verify:block:";

    @Value("${oauth2.kakao.client_id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.redirect_uri}")
    private String kakaoRedirectUri;

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
    public UserResDto myInfo() {
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

    public List<UserResDto> userList(Pageable pageable) {
        // Pageable 객체를 직접 생성할 필요가 없음
        // Controller가 보내줌.

        Page<User> all = userRepository.findAll(pageable);

        // 실질적인 데이터
        List<UserResDto> content = all.getContent().stream()
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

    public UserResDto findByEmail(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EntityNotFoundException("User not Found!"));

        log.info(user.toString());

        return user.fromEntity();
    }

    public String mailCheck(String email) {
        // 차단 상태 확인
        if(isBlocked(email)){
            throw new IllegalArgumentException("blocked email");
        }
        Optional<User> foundEmail =
                userRepository.findByEmail(email);
        // 이미 존재하는 이메일인 경우 -> 회원가입 불가
        if (foundEmail.isPresent()) {
            // 이미 존재하는 이메일이라는 에러를 발생 -> controller가 이 에러를 처리
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String authNum;
        // 이메일 전송만을 담당하는 객체를 이용해서 이메일 로직 작성.
        try {
            authNum = mailSenderService.joinMail(email);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 과정 중 문제 발생");
        }

        // 인증 코드를 redis에 저장하자
        String key = VERIFICATION_CODE_KEY + email;
        redisTemplate.opsForValue().set(key, authNum, Duration.ofMinutes(1));

        return authNum;
    }

    public Map<String, String> verifyEmail(Map<String, String> map) {

        String email = map.get("email");
        String code = map.get("code");

        // 차단 상태 확인
        // 차단된 이메일인 경우
        if(isBlocked(email)) {
            throw new IllegalArgumentException("blocked email: " + email);
        }

        // redis에 저장된 인증 코드 조회
        String key = VERIFICATION_CODE_KEY + email;
        Object foundCode = redisTemplate.opsForValue().get(key);
        // 인증 코드 유효시간이 만료된 경우
        if(foundCode == null) {
            throw new IllegalArgumentException("Authcode expired.");
        }

        // 인증 시도 횟수 증가
        int attemptCount = incrementAttemptCount(email);

        // 조회한 코드와 사용자가 입력한 코드가 일치한 지 검증
        if(!foundCode.toString().equals(code)) {
            // 인증 코드를 틀린 경우
            if(attemptCount >= 3){
                // 최대 시도 횟수 초과 시 해당 이메일 인증 차단
                blockUser(email);
                throw new IllegalArgumentException("email blocked.");
            }
            int remainingAttempt = 3 - attemptCount;
            throw new IllegalArgumentException(String.format("authCode wrong!, %d", remainingAttempt));
        }

        log.info("이메일 인증 성공!, email: {}", email);

        // 인증 완료 했기 때문에, redis에 있는 인증 관련 데이터를 삭제하자.
        redisTemplate.delete(key);

        return map;
    }

    private boolean isBlocked(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        return redisTemplate.hasKey(key);
    }

    private void blockUser(String email) {

        String key = VERIFICATION_BLOCK_KEY + email;
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofMinutes(30));

    }

    private int incrementAttemptCount(String email) {

        String key = VERIFICATION_ATTEMPT_KEY + email;
        Object obj = redisTemplate.opsForValue().get(key);

        int count = (obj != null) ? Integer.parseInt(obj.toString()) + 1 : 1;
        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(1));

        return count;
    }


    // 인가 코드로 kakao access 토큰 받기
    public String getKakaoAccessToken(String code) {

        // 요청 URL
        String requestUri = "https://kauth.kakao.com/oauth/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        // 헤더 정보 세팅
        headers.add("Content-Type",
                "application/x-www-form-urlencoded;charset=utf-8");
        // 바디 세팅
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("client_id", kakaoClientId);
        map.add("redirect_uri", kakaoRedirectUri);

        // 헤더정보와 바디 정보를 하나로 합치자
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // 토큰 발급 요청을 카카오 서버로 보내보자
                /*
            - RestTemplate객체가 REST API 통신을 위한 API인데 (자바스크립트 fetch역할)
            - 서버에 통신을 보내면서 응답을 받을 수 있는 메서드가 exchange
            param1: 요청 URL
            param2: 요청 방식 (get, post, put, patch, delete...)
            param3: 요청 헤더와 요청 바디 정보 - HttpEntity로 포장해서 줘야 함
            param4: 응답결과(JSON)를 어떤 타입으로 받아낼 것인지 (ex: DTO로 받을건지 Map으로 받을건지)
         */
        ResponseEntity<Map> responseEntity
                = restTemplate.exchange(requestUri, HttpMethod.POST, request, Map.class);

        Map<String, Object> responseJson = (Map<String, Object>) responseEntity.getBody();

        log.info("응답 JSON 데이터는 : {}", responseJson);

        // Access Token 추출
        String accessToken = responseJson.get("access_token").toString();

        return accessToken;
    }

    // Access Token을 통해 카카오 서버에서 사용자 정보 가져오기
    public KakaoUserDto getKakaoUserInfo(String accessToken) {

        String requestUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> entity = template.exchange(requestUri,
                HttpMethod.POST, new HttpEntity<>(headers), String.class);

        log.info(entity.getBody());

        ResponseEntity<KakaoUserDto> responseEntity = template.exchange(requestUri,
                HttpMethod.POST, new HttpEntity<>(headers), KakaoUserDto.class);

        log.info(responseEntity.getBody().toString());

        return responseEntity.getBody();
    }

    public UserResDto findOrCreateKakaoUser(KakaoUserDto dto) {
        // 카카오 ID로 기존 사용자 찾기
        Optional<User> existingUser =
                userRepository.findBySocialProviderAndSocialId("KAKAO" ,dto.getId().toString());

        if(existingUser.isPresent()) {
            User foundUser = existingUser.get();
            return foundUser.fromEntity();
        }
        else{  // 처음 카카오 로그인한 사람 -> 새 사용자로 생성을 해줘야 함.
            User newUser = User.builder()
                    .email(dto.getAccount().getEmail())
                    .socialId(dto.getId().toString())
                    .name(dto.getProperties().getNickName())
                    .profileImage(dto.getProperties().getProfileImage())
                    .socialProvider("KAKAO")
                    // 소셜 로그인은 비밀번호 없음
                    .password(null)
                    .address(null)
                    .build();
            User saved = userRepository.save(newUser);
            return saved.fromEntity();
        }

    }
}

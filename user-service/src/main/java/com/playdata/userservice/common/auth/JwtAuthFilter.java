package com.playdata.userservice.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 클라이언트가 전송한 토큰을 검사하는 필터
// spring security에 등록해서 사용할 것임.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 우리가 만드는 이 필터가 요청 한번 당 자동으로 동작할 수 있게끔
        // OncePerRequestFilter를 상속받음.
        // 메소드 내에는 필터가 해야 할 로직을 작성하면 됨.

        // 요청과 함께 전달된 jwt를 얻어와야 함.
        // jwt는 클라이언트 단에서 요청 헤더에 담겨져서 전달됨.
        // Authorization: Bear avasdasd(토큰값) 형식으로 전달하는 것이 관례임.

        // 요청과 함께 전달된 토큰을 요청 헤더에서 꺼내기
        String token = parseBearerToken(request);
        // token에 null이 들어갈 수 있으니 null 체크
        try {
            if (token != null) {
                // token이 null이 아니면 이 토큰의 유효성을 검사

                TokenUserInfo userInfo
                        = jwtTokenProvider.validateAndGetTokenUserInfo(token);
                // spring security에게 전달할 인가 정보 리스트를 생성. (권한 정보)
                // 권한이 여러 개 존재할 경우 리스트로 권한 체크에 사용할 필드를 add.
                // (권한 여러개면 여러번 add 가능)
                // 나중에 컨트롤러의 요청 메서드마다 권한을 파악하게 하기 위해 미리 저장을 해 놓는 것.
                List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
                // _는 security의 입력 규칙임. ex) ROLE_USER, ROLE_ADMIN
                // 나중에 ROLE을 꺼내올 때 security가 ROLE_를 붙여서 검색을 함.
                authorityList.add(new SimpleGrantedAuthority("ROLE_"
                        + userInfo.getRole().toString()));
                // 인터페이스 형태로 짧게 선언
                // 인증 완료 처리
                // 위에서 준비한 여러가지 사용자 정보, 인가정보 리스트를 하나의 객체로 포장
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userInfo, // controller 등에서 활용할 USER 정보
                        "",       // 인증된 사용자의 비밀번호: 보통 null 혹은 빈 문자열로 선언
                        authorityList  // 인가 정보 (권한 확인용)
                );

                // Security 컨테이너에 인증 정보 객체를 등록
                // 인증 정보를 전역적으로 어느 컨테이너, 혹은 서비스에서나 활용할 수 있도록 미리 저장
                SecurityContextHolder.getContext().setAuthentication(auth);


            }

            // Filter를 통과하는 메소드 (doFilter를 호출하지 않으면 필터 통과가 안됨.)
            // 토큰의 소유 여부와 관계없이 필터를 통과해야 하긴 함.
            filterChain.doFilter(request, response);

        } catch (Exception e) {
                // token 검증 과정에서 문제가 발생한다면 catch문이 실행될 것임.
                e.printStackTrace();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + "invalid_token" + "\"}");

            }
    }

    private String parseBearerToken(HttpServletRequest request) {
        // Authorization: Bear avasdasd...(토큰값) 형식으로 전달하는 것이 관례임.
        String bearerToken
                = request.getHeader("Authorization");// 얻고자 하는 키값을 매개변수로 넣음.

        // Bearer가 붙어있는 문자열에서 Bearer를 뗴자.
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bearer를 뗀 값을 리턴
        }
        return null;
    }
}

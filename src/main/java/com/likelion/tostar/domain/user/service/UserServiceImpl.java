package com.likelion.tostar.domain.user.service;

import com.likelion.tostar.domain.user.dto.UserInfoDTO;
import com.likelion.tostar.domain.user.dto.LoginRequestDTO;
import com.likelion.tostar.domain.user.entity.User;
import com.likelion.tostar.domain.user.repository.UserRepository;
import com.likelion.tostar.global.enums.statuscode.ErrorStatus;
import com.likelion.tostar.global.exception.GeneralException;
import com.likelion.tostar.global.jwt.util.JwtUtil;
import com.likelion.tostar.global.response.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 로그인
     */
    @Transactional
    public ResponseEntity<?> login(LoginRequestDTO dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_CORRECT);
        }

        String accessToken = jwtUtil.createJwt(user.getEmail(), user.getRole());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // JWT 발급 성공시 Header에 삽입하여 반환

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.onSuccess("Bearer " + accessToken));
    }

    /**
     * 회원가입
     */
    public ResponseEntity<?> join(UserInfoDTO userInfoDTO) {

        // 동일 username 사용자 생성 방지
        if (userRepository.existsUserByEmail(userInfoDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.onFailure(ErrorStatus._MEMBER_IS_EXISTS, "회원가입에 실패하였습니다."));
        }

        // 새로운 회원 생성 - OAuth2 를 통한 회원가입을 수행할 경우 비밀번호는 저장하지 않아야함
        User user = User.builder()
                .email(userInfoDTO.getEmail())
                .password(passwordEncoder.encode(userInfoDTO.getPassword())) // 암호화 후 저장
                .role("ROLE_USER") // 사용자 권한 설정 접두사 ROLE 작성 필요
                .build();
        userRepository.save(user);

        String accessToken = jwtUtil.createJwt(user.getEmail(), user.getRole());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.onSuccess("Bearer " + accessToken));
    }
}

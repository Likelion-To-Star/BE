package com.likelion.tostar.domain.user.service;

import com.likelion.tostar.domain.relationship.entity.Relationship;
import com.likelion.tostar.domain.relationship.repository.RelationshipRepository;
import com.likelion.tostar.domain.user.converter.UserConverter;
import com.likelion.tostar.domain.user.dto.*;
import com.likelion.tostar.domain.user.entity.User;
import com.likelion.tostar.domain.user.repository.UserRepository;
import com.likelion.tostar.global.enums.statuscode.ErrorStatus;
import com.likelion.tostar.global.exception.GeneralException;
import com.likelion.tostar.global.jwt.util.JwtUtil;
import com.likelion.tostar.global.response.ApiResponse;
import com.likelion.tostar.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserConverter userConverter;
    private final S3Service s3Service;
    private final RelationshipRepository relationshipRepository;


    /**
     * 로그인
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(LoginRequestDTO dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 비밀 번호 검증
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_CORRECT);
        }

        String accessToken = "Bearer " + jwtUtil.createJwt(user.getEmail(), user.getRole());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);

        // 반환 DTO 생성
        LoginResponseDTO loginResponseDTO = userConverter.toLoginResponseDTO(user, accessToken);

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.onSuccess(loginResponseDTO));
    }

    /**
     * 회원 가입
     */
    @Override
    public ResponseEntity<?> join(MultipartFile image, UserJoinDTO userJoinDTO) throws IOException {

        // 동일 username 사용자 생성 방지
        if (userRepository.existsUserByEmail(userJoinDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.onFailure(ErrorStatus._USER_IS_EXISTS, "회원가입에 실패하였습니다."));
        }

        User user = userConverter.toUser(userJoinDTO);

        userRepository.save(user);

        user.changeProfileImage(s3Service.uploadFile(image));

        return getJwtResponseEntity(user);
    }

    /**
     * 회원 개인 정보 열람
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> info(String email) {
        // 해당 회원이 실제로 존재 하는지 확인
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 회원 정보 반환
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(userConverter.toUserInfoDTO(user)));
    }

    /**
     * 회원 개인 정보 수정
     */
    @Override
    public ResponseEntity<?> edit(MultipartFile image, UserInfoDTO userInfoDTO, String email) throws IOException {
        // 해당 회원이 실제로 존재 하는지 확인
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 기존 프로필 이미지 삭제
        s3Service.deleteFileByURL(user.getProfileImage());

        user.changeUserInfo(userInfoDTO); // 회원 정보 수정

        if (!image.isEmpty()) {
            // 새로운 프로필 이미지 업로드
            user.changeProfileImage(s3Service.uploadFile(image)); // 사용자 정보 갱신
        }

        return ResponseEntity.ok(ApiResponse.onSuccess("회원정보가 수정되었습니다."));
    }

    /**
    회원 검색
     */
    @Override
    public ResponseEntity<?> searchUser(Long userId, String petName, int page, int size) {
        // 해당 회원이 실제로 존재 하는지 확인
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 400 : 검색할 애완동물 이름 누락
        if (petName.isBlank()) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.onFailure(ErrorStatus._BAD_REQUEST, "잘못된 요청입니다. 검색할 애완동물 이름을 입력해주세요."));
        }

        // 페이지로 받기
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findByPetNameContaining(petName, pageRequest);

        // 오류 확인 로그
//        System.out.println("총 개수: " +  users.getTotalElements());
//        System.out.println("페이지 내용: " + users.getContent());

        // data 가공
        List<UserSearchDto> data = new ArrayList<>();
        for (User user : users) {
            UserSearchDto dto = UserSearchDto.builder()
                    .id(user.getId())
                    .petName(user.getPetName())
                    .profileImage(user.getProfileImage())
                    .category(user.getCategory())
                    .birthday(user.getBirthday().toString())
                    .starDay(user.getStarDay().toString())
                    .build();
            data.add(dto);
        }

        // 200 : 검색 성공
        return ResponseEntity.status(200)
                .body(ApiResponse.onSuccess(data));
    }

    /**
    * 친구 추가
     */
    @Override
    public ResponseEntity<?> addFriend(String email, AddFriendDto addFriendDto) {
        // 해당 회원이 실제로 존재 하는지 확인
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 404 : 친구 id에 해당하는 user가 없는 경우
        User friend = userRepository.findById(addFriendDto.getFriendId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FRIEND_NOT_FOUND));

        // relationship 테이블 탐색을 위한 id 순서대로 정렬
        User firstUser = (user.getId() < friend.getId()) ? user : friend; // 더 작은 id를 가진 회원
        User secondUser = (user.getId() < friend.getId()) ? friend : user; // 더 큰 id를 가진 회원

        // 409 : 자기 자신과 친구를 맺으려는 경우
        if (user.equals(friend)) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.onFailure(ErrorStatus._SELF_FRIEND_REQUEST_NOT_ALLOWED, null));
        }

        // 409 : 이미 친구인 경우
        Relationship foundRelationship = relationshipRepository.findByUsers(firstUser, secondUser)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FRIEND_ALREADY_EXISTS));

        // save
        Relationship relationship = Relationship.builder()
                .user1(firstUser)
                .user2(secondUser)
                .build();
        relationshipRepository.save(relationship);

        // 200 : 친구 추가 성공
        return ResponseEntity.status(200)
                .body(ApiResponse.onSuccess("친구추가에 성공했습니다."));
    }

    // 회원 가입 & 로그인 성공시 JWT 생성 후 반환
    public ResponseEntity<?> getJwtResponseEntity(User user) {
        String accessToken = jwtUtil.createJwt(user.getEmail(), user.getRole());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.onSuccess("Bearer " + accessToken));
    }
}

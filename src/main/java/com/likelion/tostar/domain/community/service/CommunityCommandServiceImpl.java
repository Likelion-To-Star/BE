package com.likelion.tostar.domain.community.service;

import com.likelion.tostar.domain.community.converter.CommunityConverter;
import com.likelion.tostar.domain.community.dto.CommunityFormDTO;
import com.likelion.tostar.domain.community.entity.Community;
import com.likelion.tostar.domain.community.repository.CommunityRepository;
import com.likelion.tostar.domain.user.entity.User;
import com.likelion.tostar.domain.user.repository.UserRepository;
import com.likelion.tostar.global.enums.statuscode.ErrorStatus;
import com.likelion.tostar.global.exception.GeneralException;
import com.likelion.tostar.global.response.ApiResponse;
import com.likelion.tostar.global.s3.service.S3Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityCommandServiceImpl implements CommunityCommandService {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityConverter communityConverter;
    private final S3Service s3Service;

    @Override
    public ResponseEntity<?> createCommunity(
            MultipartFile image, CommunityFormDTO communityFormDTO, String email) throws IOException {
        // 1. 회원 정보 조회
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 커뮤니티 생성
        Community community = communityConverter.toCommunity(image,communityFormDTO);
        user.createCommunity(community);

        // 3. 커뮤니티 저장(+멤버 저장)
        communityRepository.save(community);

        return ResponseEntity.ok(ApiResponse.onSuccess("커뮤니티가 생성되었습니다."));
    }

    @Override
    public ResponseEntity<?> editCommunity(
            Long communityId, MultipartFile image,
            CommunityFormDTO communityFormDTO, String email) throws IOException {
        // 1. 회원 정보 조회
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 커뮤니티 존재 확인
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._COMMUNITY_NOT_FOUND));

        // 3. 회원이 커뮤니티 주인인지 확인
        if (!community.getOwner().equals(user)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 4. 커뮤니티 값 수정
        // 4.1. 기존 이미지 삭제
        s3Service.deleteFileByURL(community.getProfileImage());
        // 4.2. 커뮤니티 정보 변경
        community.changeCommunityInfo(communityFormDTO);
        // 4.3. 새로운 이미지 저장
        if (!image.isEmpty()) {
            community.changeProfileImage(s3Service.uploadFile(image));
        }

        return ResponseEntity.ok(ApiResponse.onSuccess("커뮤니티가 수정되었습니다."));
    }

    @Override
    public ResponseEntity<?> deleteCommunity(Long communityId, String email) {
        // 1. 회원 정보 조회
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 커뮤니티 존재 확인
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._COMMUNITY_NOT_FOUND));

        // 3. 회원이 커뮤니티 주인인지 확인
        if (!community.getOwner().equals(user)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }

        // 4. 커뮤니티 이미지 삭제
        s3Service.deleteFileByURL(community.getProfileImage());
        communityRepository.delete(community);

        return ResponseEntity.ok(ApiResponse.onSuccess("커뮤니티가 삭제되었습니다."));
    }

    /**
     * 커뮤니티 가입
     */
    @Override
    public ResponseEntity<?> joinCommunity(Long communityId, String email) {
        return null;
    }
}

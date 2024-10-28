package com.likelion.tostar.domain.user.service;

import com.likelion.tostar.domain.user.dto.AddFriendDto;
import com.likelion.tostar.domain.user.dto.LoginRequestDTO;
import com.likelion.tostar.domain.user.dto.UserInfoDTO;
import com.likelion.tostar.domain.user.dto.UserJoinDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    ResponseEntity<?> login(LoginRequestDTO dto);
    ResponseEntity<?> join(MultipartFile image, UserJoinDTO userJoinDTO) throws IOException;
    ResponseEntity<?> info(String email);
    ResponseEntity<?> edit(MultipartFile image, UserInfoDTO userInfoDTO, String email) throws IOException;
    ResponseEntity<?> searchUser(Long userId, String petName, int page, int size);
    ResponseEntity<?> addFriend(String email, AddFriendDto addFriendDto);
    ResponseEntity<?> searchFriend(Long userId);
}

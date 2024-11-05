package com.likelion.tostar.domain.chat.converter;

import com.likelion.tostar.domain.chat.dto.CommunityChatResponseDTO;
import com.likelion.tostar.domain.chat.entity.enums.MessageType;
import com.likelion.tostar.domain.user.entity.User;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConverter {
    /**
     * 구독 발송용 메시지 생성
     */
    public CommunityChatResponseDTO toCommunityChatResponseDTO(
            String message, MessageType messageType, User sender) {
        return CommunityChatResponseDTO.builder()
                .messageType(messageType)
                .email(sender.getEmail())
                .content(message)
                .build();
    }
}

package kr.sparta.livechat.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자용 채팅 상세 내역의 개별 메시지 정보를 담는 DTO
 * 메시지의 고유 식별자, 내용, 작성자 정보 및 전송시간을 포함합니다.
 * AdminChatMessageResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 22.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminChatMessageResponse {
	private Long messageId;
	private String content;
	private String type;
	private Long writerId;
	private String writerName;
	private LocalDateTime sentAt;
}

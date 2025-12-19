package kr.sparta.livechat.dto.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.domain.role.RoleInRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 채팅방 생성 결과를 반환하는 응답 DTO 클래스입니다.
 * <p>
 * 채팅방 생성이 완료 되었을 때, 생성된 채팅방의 기본 정보, 참여자 목록, 최초 메시지 정보를 함께 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Getter
@AllArgsConstructor
public class CreateChatRoomResponse {

	private Long chatRoomId;
	private ChatRoomStatus status;
	private String productName;
	private List<ParticipantResponse> participants;
	private FirstMessageResponse firstMessage;

	/**
	 * 채팅방 참여자 정보를 담는 응답 DTO입니다.
	 */
	@Getter
	@AllArgsConstructor
	public static class ParticipantResponse {
		private Long userId;
		private String userName;
		private RoleInRoom roleInRoom;
		private String profileImage;
	}

	/**
	 * 최초 메시지 정보를 담는 응답 DTO입니다.
	 */
	@Getter
	@AllArgsConstructor
	public static class FirstMessageResponse {
		private Long messageId;
		private String content;
		private MessageType type;
		private LocalDateTime sentAt;
		private Long writerId;
		private String writerName;
	}
}

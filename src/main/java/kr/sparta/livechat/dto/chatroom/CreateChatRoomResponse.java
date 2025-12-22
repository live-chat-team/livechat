package kr.sparta.livechat.dto.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.ChatRoomParticipant;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.entity.User;
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
	 * 채팅방 생성 응답 DTO를 생성합니다.
	 *
	 * @param chatRoomId   생성된 채팅방 식별자
	 * @param status       채팅방 상태
	 * @param productName  상담 대상 상품명
	 * @param participants 참여자 목록
	 * @param firstMessage 최초 메시지 정보
	 * @return 채팅방 생성 응답 DTO
	 */
	public static CreateChatRoomResponse of(
		Long chatRoomId,
		ChatRoomStatus status,
		String productName,
		List<ParticipantResponse> participants,
		FirstMessageResponse firstMessage
	) {
		return new CreateChatRoomResponse(chatRoomId, status, productName, participants, firstMessage);
	}

	/**
	 * 채팅방 엔티티로부터 채팅방 생성 응답 DTO를 생성합니다.
	 *
	 * @param room         생성된 채팅방
	 * @param participants 참여자 목록
	 * @param firstMessage 최초 메시지 정보
	 * @return 채팅방 생성 응답 DTO
	 */
	public static CreateChatRoomResponse of(
		ChatRoom room,
		List<ParticipantResponse> participants,
		FirstMessageResponse firstMessage
	) {
		return CreateChatRoomResponse.of(
			room.getId(),
			room.getStatus(),
			room.getProduct().getName(),
			participants,
			firstMessage
		);
	}

	/**
	 * 채팅방 참여자 정보를 담는 응답 DTO입니다.
	 */
	@Getter
	@AllArgsConstructor
	public static class ParticipantResponse {
		private Long userId;
		private String userName;
		private RoleInRoom roleInRoom;

		/**
		 * 참여자 엔티티로부터 참여자 응답 DTO를 생성합니다.
		 *
		 * @param participant 채팅방 참여자
		 * @return 참여자 응답 DTO
		 */
		public static ParticipantResponse of(ChatRoomParticipant participant) {
			User user = participant.getUser();
			return new ParticipantResponse(
				user.getId(),
				user.getName(),
				participant.getRoleInRoom()
			);
		}
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

		/**
		 * 메시지 엔티티로부터 최초 메시지 응답 DTO를 생성합니다.
		 *
		 * @param message 최초 메시지
		 * @return 최초 메시지 응답 DTO
		 */
		public static FirstMessageResponse of(Message message) {
			User writer = message.getWriter();
			return new FirstMessageResponse(
				message.getId(),
				message.getContent(),
				message.getType(),
				message.getSentAt(),
				writer.getId(),
				writer.getName()
			);
		}
	}
}

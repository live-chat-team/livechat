package kr.sparta.livechat.service;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import kr.sparta.livechat.domain.role.MessageType;
import kr.sparta.livechat.dto.socket.ChatEventResponse;
import kr.sparta.livechat.dto.socket.MessageResponse;
import kr.sparta.livechat.dto.socket.MessageSendRequest;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.WsCustomException;
import kr.sparta.livechat.global.exception.WsErrorCode;

import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final SocketService socketService;

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * 메시지 전송 유스케이스
	 * - roomId 유효성 검증(4004)
	 * - 참여자 여부 검증(4002)
	 * - type/content 형식 오류(4003)
	 * - Message 엔티티 생성 및 저장
	 * - /sub/chat/room/{roomId} 로 MESSAGE 이벤트 브로드캐스트
	 */
	public void sendMessage(Long writerId, MessageSendRequest request) {

		Long roomId = request.getRoomId();

		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new WsCustomException(WsErrorCode.CHAT_ROOM_NOT_FOUND));

		if (room.getStatus() == ChatRoomStatus.CLOSED) {
			throw new WsCustomException(WsErrorCode.FORBIDDEN);
		}

		if (!socketService.isParticipant(roomId, writerId)) {
			throw new WsCustomException(WsErrorCode.FORBIDDEN);
		}

		if (!StringUtils.hasText(request.getContent())) {
			throw new WsCustomException(WsErrorCode.INVALID_MESSAGE);
		}

		MessageType type;

		try {
			type = MessageType.valueOf(request.getType());
		} catch (Exception e) {
			throw new WsCustomException(WsErrorCode.INVALID_MESSAGE);
		}

		User writer = userRepository.findById(writerId)
			.orElseThrow(() -> new WsCustomException(WsErrorCode.AUTH_FAILED));

		Message saved = messageRepository.save(
			Message.of(room, writer, request.getContent(), type)
		);

		MessageResponse response = MessageResponse.builder()
			.id(saved.getId())
			.roomId(saved.getRoom().getId())
			.writerId(saved.getWriter().getId())
			.type(saved.getType().name())
			.content(saved.getContent())
			.sentAt(saved.getSentAt())
			.readCount(1)
			.build();

		messagingTemplate.convertAndSend(
			"/sub/chat/room/" + roomId,
			ChatEventResponse.<MessageResponse>builder()
				.event("MESSAGE")
				.message(response)
				.build()
		);
	}
}

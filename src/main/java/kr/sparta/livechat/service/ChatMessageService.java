package kr.sparta.livechat.service;

import java.time.LocalDateTime;
import java.util.List;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.entity.MessageRead;
import kr.sparta.livechat.domain.role.MessageType;

import kr.sparta.livechat.dto.socket.ChatEventResponse;
import kr.sparta.livechat.dto.socket.MessageResponse;
import kr.sparta.livechat.dto.socket.MessageSendRequest;
import kr.sparta.livechat.dto.socket.ReadEventResponse;
import kr.sparta.livechat.dto.socket.ReadMessageRequest;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.WsCustomException;
import kr.sparta.livechat.global.exception.WsErrorCode;

import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageReadRepository;
import kr.sparta.livechat.repository.MessageRepository;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 채팅 메시지 전송 및 브로드캐스트 로직을 담당하는 서비스 클래스입니다.
 *
 * 클라이언트가 {@code /pub/chat/message}로 전송한 메시지를 검증한 뒤
 * DB에 저장하고 {@code /sub/chat/room/{roomId}} 구독자들에게
 * 메세지 이벤트를 브로드캐스트합니다.
 *
 * @author 오정빈
 * @since 2025. 12. 22.
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final SocketService socketService;

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final MessageReadRepository messageReadRepository;

	private final SimpMessagingTemplate messagingTemplate;

	/**
	 * 채팅 메시지를 전송하고 구독자들에게 브로드캐스트합니다.
	 *
	 * {@code 4002}: 전송자가 해당 채팅방 참여자가 아님
	 * {@code 4003}: 형식 오류
	 * {@code 4004}: {@code roomId}에 해당하는 채팅방이 존재하지 않음
	 */
	public void sendMessage(Long writerId, MessageSendRequest request) {

		Long roomId = request.getRoomId();

		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new WsCustomException(WsErrorCode.CHAT_ROOM_NOT_FOUND));

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

	/**
	 * 메시지 읽음 처리를 수행하고 READ 이벤트를 브로드캐스트합니다.
	 *
	 * 사용자가 읽은 마지막 메시지 ID를 받아 검증하고
	 * 해당 채팅방의 모든 참여자에게 READ 이벤트를 브로드캐스트합니다.
	 *
	 * 읽음 처리된 메세지는 DB에 저장합니다.
	 *
	 * CHAT_ROOM_NOT_FOUND: 채팅방이 존재하지 않음
	 * FORBIDDEN: 채팅방 참여자가 아님
	 * NVALID_MESSAGE: lastReadMessageId가 해당 채팅방의 메시지가 아님
	 */
	public void readMessage(Long readerId, ReadMessageRequest request) {
		Long roomId = request.getRoomId();
		Long lastReadMessageId = request.getLastReadMessageId();

		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new WsCustomException(WsErrorCode.CHAT_ROOM_NOT_FOUND));

		if (!socketService.isParticipant(roomId, readerId)) {
			throw new WsCustomException(WsErrorCode.FORBIDDEN);
		}

		if (!messageRepository.existsById(lastReadMessageId)) {
			throw new WsCustomException(WsErrorCode.CHAT_ROOM_NOT_FOUND);
		}

		if (!messageRepository.existsByIdAndRoomId(lastReadMessageId, roomId)) {
			throw new WsCustomException(WsErrorCode.INVALID_MESSAGE);
		}

		User reader = userRepository.findById(readerId)
			.orElseThrow(() -> new WsCustomException(WsErrorCode.AUTH_FAILED));

		List<Message> messagesToMarkRead =
			messageRepository.findByRoom_IdAndIdLessThanEqualOrderByIdAsc(roomId, lastReadMessageId);

		for (Message message : messagesToMarkRead) {
			if (!messageReadRepository.existsByMessageIdAndUserId(message.getId(), readerId)) {
				MessageRead read = MessageRead.of(message, reader);
				messageReadRepository.save(read);
			}
		}

		ReadEventResponse eventResponse = ReadEventResponse.builder()
			.event("READ")
			.roomId(roomId)
			.readerId(readerId)
			.lastReadMessageId(lastReadMessageId)
			.readAt(LocalDateTime.now())
			.build();

		messagingTemplate.convertAndSend(
			"/sub/chat/room/" + roomId,
			eventResponse
		);
	}
}

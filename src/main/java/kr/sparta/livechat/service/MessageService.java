package kr.sparta.livechat.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.dto.message.ChatMessageListItem;
import kr.sparta.livechat.dto.message.GetChatMessageListResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomParticipantRepository;
import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 메시지 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 메시지 목록 조회는 무한 스크롤을 위해 커서(cursor) 기반으로 동작하며,
 * DB 조회는 최신순(DESC)으로 가져오되 화면에서는 시간순(ASC)이 자연스럽기 때문에 응답 매핑 단계에서 ASC로 재정렬합니다.
 * 커서는 마지막으로 조회한 메시지의 ID를 사용하며, 이를 통해 안정적이고 효율적인 페이징을 구현합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

	private static final int DEFAULT_SIZE = 50;

	private final MessageRepository messageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomParticipantRepository chatRoomParticipantRepository;

	/**
	 * 채팅방 메시지 목록을 커서 기반으로 조회합니다.
	 * <p>
	 * 최초 진입 시에는 {@code cursor}를 {@code null}로 전달하여 최신 메시지부터 조회하며,
	 * 이후 스크롤 시에는 이전 응답의 {@code nextCursor} 값을 사용하여 과거 메시지를 조회합니다.
	 * </p>
	 *
	 * @param chatRoomId    채팅방 ID
	 * @param cursor        마지막으로 조회한 메시지 ID (null이면 최신부터 조회)
	 * @param size          한 번에 조회할 메시지 개수 (null이면 {@value #DEFAULT_SIZE})
	 * @param currentUserId 로그인한 사용자 ID
	 * @return 메시지 목록 조회 응답 DTO
	 */
	@Transactional(readOnly = true)
	public GetChatMessageListResponse getMessageList(
		Long chatRoomId,
		Long cursor,
		Integer size,
		Long currentUserId
	) {
		validateChatRoomExists(chatRoomId);
		validateParticipant(chatRoomId, currentUserId);
		validateCursor(cursor);
		validateSize(size);

		int resolvedSize = resolveSize(size);

		Pageable pageable = PageRequest.of(
			0,
			resolvedSize,
			Sort.by(Sort.Direction.DESC, "sentAt")
				.and(Sort.by(Sort.Direction.DESC, "id"))
		);

		Slice<Message> slice = (cursor == null)
			? messageRepository.findByRoom_Id(chatRoomId, pageable)
			: messageRepository.findByRoom_IdAndIdLessThan(chatRoomId, cursor, pageable);

		List<Message> content = slice.getContent();

		List<ChatMessageListItem> items = content.stream()
			.sorted(Comparator.comparing(Message::getSentAt).thenComparing(Message::getId))
			.map(ChatMessageListItem::from)
			.toList();

		Long nextCursor = calculateNextCursor(content, slice.hasNext());

		return GetChatMessageListResponse.builder()
			.chatRoomId(chatRoomId)
			.size(resolvedSize)
			.hasNext(slice.hasNext())
			.nextCursor(nextCursor)
			.messageList(items)
			.build();
	}

	private void validateParticipant(Long chatRoomId, Long currentUserId) {
		if (!chatRoomParticipantRepository.existsByRoomIdAndUserId(chatRoomId, currentUserId)) {
			throw new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED);
		}
	}

	private Long calculateNextCursor(List<Message> messages, boolean hasNext) {
		if (!hasNext || messages.isEmpty()) {
			return null;
		}
		return messages.get(messages.size() - 1).getId();
	}

	private int resolveSize(Integer size) {
		return size == null ? DEFAULT_SIZE : size;
	}

	private void validateCursor(Long cursor) {
		if (cursor != null && cursor <= 0) {
			throw new CustomException(ErrorCode.COMMON_BAD_PAGINATION);
		}
	}

	private void validateSize(Integer size) {
		if (size != null && size <= 0) {
			throw new CustomException(ErrorCode.COMMON_BAD_PAGINATION);
		}
	}

	private void validateChatRoomExists(Long chatRoomId) {
		if (chatRoomId == null || chatRoomId <= 0) {
			throw new CustomException(ErrorCode.CHATROOM_INVALID_INPUT);
		}
		if (!chatRoomRepository.existsById(chatRoomId)) {
			throw new CustomException(ErrorCode.CHATROOM_NOT_FOUND);
		}
	}
}

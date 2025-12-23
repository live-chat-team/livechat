package kr.sparta.livechat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.entity.Message;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.dto.admin.AdminChatDetailResponse;
import kr.sparta.livechat.dto.admin.AdminChatMessageResponse;
import kr.sparta.livechat.dto.admin.AdminChatRoomListResponse;
import kr.sparta.livechat.dto.admin.AdminChatRoomResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomRepository;
import kr.sparta.livechat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 채팅방 비즈니스 로직 서비스
 * 전체 채팅방의 상태 및 참여자 정보 조회
 * 관리자 권한 확인 및 페이징 처리
 * AdminChatService.java
 *
 * @author kimsehyun
 * @since 2025. 12. 18.
 */
@Service
@RequiredArgsConstructor
public class AdminChatService {
	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;

	/**
	 * 모든 채팅방 목록 조회
	 * 요청자의 관리자 권한 검증,
	 * 채팅방 목록을 페이징 하여 가져온다
	 * 채팅방의 참여자(구매자, 판매자) 이름을 반환한다
	 * 미응답 상태 (OPEN) 인 상담을 최상단에 나열
	 * 동일 상태 내에서는 최신 생성일 순으로 나열
	 *
	 * @param page 조회할 페이지 번호
	 * @param size 페이지장 데이터 개수
	 * @return 페이징 정보가 포함된 채팅방 목록 응답 DTO
	 */
	@Transactional(readOnly = true)
	public AdminChatRoomListResponse getAllChatRooms(int page, int size) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() ||
			authentication.getPrincipal().equals("anonymousUser")) {
			throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
		}
		boolean isAdmin = authentication.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		if (!isAdmin) {
			throw new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED);
		}
		if (page < 0 || size <= 0) {
			throw new CustomException(ErrorCode.COMMON_BAD_PAGINATION);
		}

		Pageable pageable = PageRequest.of(page, size,
			Sort.by(Sort.Order.desc("status"), Sort.Order.desc("createdAt")));

		Page<ChatRoom> chatRooms = chatRoomRepository.findAll(pageable);
		List<AdminChatRoomResponse> dtoList = chatRooms.getContent().stream()
			.map(room -> {
				String buyerName = room.getParticipants().stream()
					.filter(p -> p.getRoleInRoom() == RoleInRoom.BUYER)
					.map(p -> p.getUser().getName())
					.findFirst()
					.orElse("Unknown");

				String sellerName = room.getParticipants().stream()
					.filter(p -> p.getRoleInRoom() == RoleInRoom.SELLER)
					.map(p -> p.getUser().getName())
					.findFirst()
					.orElse("Unknown");

				return new AdminChatRoomResponse(
					room.getId(),
					room.getStatus().name(),
					room.getCreatedAt(),
					room.getClosedAt(),
					room.getProduct().getName(),
					sellerName,
					buyerName,
					room.getLastMessageSentAt()
				);
			}).toList();

		return new AdminChatRoomListResponse(
			chatRooms.getNumber(),
			chatRooms.getSize(),
			chatRooms.getTotalElements(),
			chatRooms.getTotalPages(),
			chatRooms.hasNext(),
			dtoList
		);
	}

	/**
	 * 특정 채팅방의 메시지 내역을 상세 조회합니다
	 * Slice 방식을 사용하며, 최신 메시지가 먼저 오도록 설정합니다.
	 *
	 * @param chatRoomId 조회할 채팅방의 고유 식별자 ID
	 * @param page 조회할 페이지 번호
	 * @param size 페이지당 메시지 개수
	 * @return 채팅방 상태 정보와 메시지 slice 포함한 AdminChatDetailResponse
	 */
	@Transactional(readOnly = true)
	public AdminChatDetailResponse getChatRoomDetail(Long chatRoomId, int page, int size) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() ||
			authentication.getPrincipal().equals("anonymousUser")) {
			throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
		}

		boolean isAdmin = authentication.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		if (!isAdmin) {
			throw new CustomException(ErrorCode.CHATROOM_ACCESS_DENIED);
		}

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

		org.springframework.data.domain.Pageable pageable =
			PageRequest.of(page, size, Sort.by("sentAt").descending());
		Slice<Message> messageSlice = messageRepository.findByRoomId(chatRoomId, pageable);

		List<AdminChatMessageResponse> messageDtos = messageSlice.getContent().stream()
			.map(msg -> AdminChatMessageResponse.builder()
				.messageId(msg.getId())
				.content(msg.getContent())
				.type(msg.getType().name())
				.writerId(msg.getWriter().getId())
				.writerName(msg.getWriter().getName())
				.sentAt(msg.getSentAt())
				.build())
			.toList();

		return AdminChatDetailResponse.builder()
			.chatRoomId(chatRoom.getId())
			.chatRoomStatus(chatRoom.getStatus().name())
			.page(messageSlice.getNumber())
			.size(messageSlice.getSize())
			.hasNext(messageSlice.hasNext())
			.messagesList(messageDtos)
			.build();
	}
}

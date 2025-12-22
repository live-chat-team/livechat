package kr.sparta.livechat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.sparta.livechat.domain.entity.ChatRoom;
import kr.sparta.livechat.domain.role.RoleInRoom;
import kr.sparta.livechat.dto.admin.AdminChatRoomListResponse;
import kr.sparta.livechat.dto.admin.AdminChatRoomResponse;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.ChatRoomRepository;
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
			Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

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
}

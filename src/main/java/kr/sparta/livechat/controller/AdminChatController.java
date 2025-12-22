package kr.sparta.livechat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.sparta.livechat.dto.admin.AdminChatRoomListResponse;
import kr.sparta.livechat.service.AdminChatService;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 채팅방 관리 컨트롤러
 * 관리자가 전체 채팅방 목록을 조회합니다.
 * 모든 요청은 관리자 권한이 필요합니다.
 * AdminChatController.java
 *
 * @author kimsehyun
 * @since 2025. 12. 18.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminChatController {
	private final AdminChatService adminChatService;

	/**
	 * 전체 채팅방 목록 조회
	 * 존재 하는 모든 채팅방 정보를 페이징하여 반환한다.
	 *
	 * @param page 조회한 페이지 번호 (0부터 시작)
	 * @param size 페이지당 출력할 데이터 개수( 기본 20)
	 * @return 채팅방 목록 정보와 페이징 데이터를 포함한 응답 엔티티
	 */
	@GetMapping("/chat-rooms")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AdminChatRoomListResponse> getAdminChatRooms(
		@RequestParam(value = "page", defaultValue = "0") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {

		return ResponseEntity.ok(adminChatService.getAllChatRooms(page, size));
	}
}

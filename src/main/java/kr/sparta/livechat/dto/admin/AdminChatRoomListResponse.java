package kr.sparta.livechat.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자용 채팅방 목록 응답 DTO
 * 관리자 페이지 에서 채팅방 목록을 조회할때 사용
 * 페이징 처리된 데이터와 해당 페이지의 데이터를 포함
 * AdminChatRoomListResponse.java
 *
 * @author kimsehyun
 * @since 2025. 12. 18.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminChatRoomListResponse {
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean hasNext;
	private List<AdminChatRoomResponse> chatRoomList;
}

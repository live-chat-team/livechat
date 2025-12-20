package kr.sparta.livechat.dto.chatroom;

import java.util.List;

import lombok.Getter;

/**
 * GetChatRoomListResponse 클래스입니다.
 * <p>
 * TODO: 클래스의 역할을 작성하세요.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 20.
 */
@Getter
public class GetChatRoomListResponse {
	private final int page;
	private final int size;
	private final Long totalElements;
	private final int totalPages;
	private final boolean hasNext;
	private final List<ChatRoomListItem> chatRoomList;

	public GetChatRoomListResponse(int page, int size, Long totalElements, int totalPages, boolean hasNext,
		List<ChatRoomListItem> chatRoomList) {
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.hasNext = hasNext;
		this.chatRoomList = chatRoomList;
	}
}

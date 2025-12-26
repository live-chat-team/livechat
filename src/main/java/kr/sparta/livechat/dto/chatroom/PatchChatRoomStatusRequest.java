package kr.sparta.livechat.dto.chatroom;

import jakarta.validation.constraints.NotNull;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 상태 변경 요청 DTO입니다.
 * 채팅방 상태를 CLOSED로 변경할 때 사용하는 요청 객체입니다.
 */
@Getter
@NoArgsConstructor
public class PatchChatRoomStatusRequest {

	@NotNull(message = "status는 필수입니다.")
	private ChatRoomStatus status;
}



package kr.sparta.livechat.dto.chatroom;

import jakarta.validation.constraints.NotNull;
import kr.sparta.livechat.domain.role.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 상태 변경 요청을 보내는 클래스입니다.
 * <p>
 * 상품을 등록한 판매자 또는 관리자만 가능하며, 관리자는 별도의 엔드포인트를 통해 처리할 수 있습니다.
 * Patch를 사용함으로서 수정이 필요한 필드만 요청을 보낼 수 있습니다. 단, 수정 요청 시 채팅방의 상태는 필수적으로 값을 담아야 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchChatRoomRequest {
	@NotNull(message = "채팅방 상태변경 시 상태값은 필수입니다.")
	private ChatRoomStatus status;
	private String reason;

}

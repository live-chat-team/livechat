package kr.sparta.livechat.dto.chatroom;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성을 위한 요청 DTO 클래스입니다.
 * <p>
 * 구매자가 상품에 대해 상담 채팅방을 생성할 때 전달하는 요청 정보를 담으며,
 * 채팅방 생성 시 최초로 전송할 메시지 내용을 포함합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreateChatRoomRequest {

	@NotBlank(message = "메시지 내용 입력은 필수입니다.")
	private String content;

}

package kr.sparta.livechat.dto.socket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클라이언트가 채팅 메시지를 전송할 때 사용하는 메시지 DTO입니다.
 *
 * {@code /pub/chat/message} 엔드포인트로 전달되고
 * 서버는 메세지를 저장한 뒤 채팅방 구독자들에게 브로드캐스트합니다.
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
@NoArgsConstructor
public class MessageSendRequest {

	@NotNull(message = "roomId는 필수입니다.")
	private Long roomId;

	@NotBlank(message = "type은 필수입니다.")
	private String type;

	@NotBlank(message = "content는 필수입니다.")
	private String content;
}


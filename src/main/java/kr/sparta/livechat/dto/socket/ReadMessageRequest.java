package kr.sparta.livechat.dto.socket;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 읽음 처리 요청 DTO입니다.
 * 클라이언트가 읽은 마지막 메시지 ID를 서버에 전송할 때 사용하는 요청 객체입니다.
 *
 * @author 오정빈
 * @since 2025. 12. 23.
 */
@Getter
@NoArgsConstructor
public class ReadMessageRequest {

	@NotNull(message = "roomId는 필수입니다.")
	private Long roomId;

	@NotNull(message = "lastReadMessageId는 필수입니다.")
	private Long lastReadMessageId;
}


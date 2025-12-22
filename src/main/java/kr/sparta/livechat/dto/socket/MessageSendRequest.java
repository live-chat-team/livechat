package kr.sparta.livechat.dto.socket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

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


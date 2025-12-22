package kr.sparta.livechat.dto.socket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {
	private Long id;
	private Long roomId;
	private Long writerId;
	private String type;
	private String content;
	private LocalDateTime sentAt;
	private int readCount;
}


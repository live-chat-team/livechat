package kr.sparta.livechat.dto.socket;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatEventResponse<T> {
	private String event;
	private T message;
}


package kr.sparta.livechat.domain.role;

/**
 * 채팅 메시지의 유형을 나타내는 enum 입니다.
 * <p>
 * 메시지 유형은 채팅방 내에서 전송되는 메시지의 성격을 구분하기 위해 사용되며, 메시지 렌더링 방식 및 처리 로직을 분기하는 기준이 됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 19.
 */
public enum MessageType {
	TEXT,
	IMAGE
}

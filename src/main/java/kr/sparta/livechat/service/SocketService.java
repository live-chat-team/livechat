package kr.sparta.livechat.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 실시간 채팅방 참여자 상태를 메모리에서 관리하는 서비스 클래스입니다.
 * <p>
 * 채팅방마다 구독중인 사용자 목록을 관리합니다.
 *
 * 이 정보는 데이터베이스가 아닌 In Memory 구조로 유지되고
 * 메시지 전송, 구독 검증, 읽음 처리 등에서 사용자 참여 여부를 판단하는 데 활용됩니다.
 * </p>
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 19.
 */
@Service
public class SocketService {

	private final ConcurrentHashMap<Long, Set<Long>> participants = new ConcurrentHashMap<>();

	public boolean isParticipant(Long roomId, Long userId) {
		return participants.getOrDefault(roomId, Set.of()).contains(userId);
	}
}




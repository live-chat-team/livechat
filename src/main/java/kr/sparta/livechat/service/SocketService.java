package kr.sparta.livechat.service;

import kr.sparta.livechat.repository.ChatRoomParticipantRepository;
import kr.sparta.livechat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
@RequiredArgsConstructor
public class SocketService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomParticipantRepository chatRoomParticipantRepository;
	private final ConcurrentHashMap<Long, Set<Long>> participants = new ConcurrentHashMap<>();

	/**
	 * 채팅방 참여자 확인.
	 * 메모리 맵에 없으면 DB에서 확인하고 맵에 추가
	 *
	 * @param roomId 채팅방 ID
	 * @param userId 사용자 ID
	 * @return 참여자 여부
	 */
	public boolean isParticipant(Long roomId, Long userId) {

		Set<Long> roomParticipants = participants.get(roomId);
		if (roomParticipants != null && roomParticipants.contains(userId)) {
			return true;
		}

		boolean isParticipantInDb = chatRoomParticipantRepository.existsByRoomIdAndUserId(roomId, userId);

		if (isParticipantInDb) {
			addParticipant(roomId, userId);
		}

		return isParticipantInDb;
	}

	public boolean existsRoom(Long roomId) {
		return chatRoomRepository.existsById(roomId);
	}

	/**
	 * 채팅방에 참여자 추가
	 * WebSocket 구독 및 메시지 전송 시 참여자 확인에 사용
	 *
	 * @param roomId 채팅방 ID
	 * @param userId 사용자 ID
	 */
	public void addParticipant(Long roomId, Long userId) {
		participants.computeIfAbsent(roomId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(userId);
	}
}




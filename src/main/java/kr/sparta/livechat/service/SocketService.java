package kr.sparta.livechat.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SocketService {

	private final ConcurrentHashMap<Long, Set<Long>> participants = new ConcurrentHashMap<>();

	public boolean isParticipant(Long roomId, Long userId) {
		return participants.getOrDefault(roomId, Set.of()).contains(userId);
	}
}




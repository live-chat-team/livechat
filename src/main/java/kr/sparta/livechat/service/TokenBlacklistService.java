package kr.sparta.livechat.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Access Token을 Redis에 블랙리스트로 등록하고 관리합니다.
 * TokenBlacklistService.java
 *
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
	private final RedisTemplate<String, Object> redisTemplate;
	private static final String BLACKLIST_PREFIX = "blacklist:";

	/**
	 * Access Token을 블랙리스트에 추가합니다.
	 * Redis의 TTL(Time-To-Live) 기능을 사용하여 토큰 만료 시 자동 삭제됩니다.
	 */
	public void addToBlacklist(String token, long ttlSeconds) {
		String key = BLACKLIST_PREFIX + token;
		Duration duration = Duration.ofSeconds(ttlSeconds);
		redisTemplate.opsForValue().set(key, "logout", duration);

		log.info("Access Token 블랙리스트에 추가됨 (Redis Key: {}), TTL: {}ms", key, ttlSeconds);
	}

	/**
	 * Access Token이 블랙리스트에 있는지 확인합니다.
	 * @param tokenValue 확인할 Access Token
	 * @return 블랙리스트에 있으면 true, 아니면 false
	 */
	public boolean isBlacklisted(String tokenValue) {
		String key = BLACKLIST_PREFIX + tokenValue;
		Boolean exists = redisTemplate.hasKey(key);
		return Boolean.TRUE.equals(exists);
	}
}

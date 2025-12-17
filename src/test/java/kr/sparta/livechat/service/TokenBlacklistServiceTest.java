package kr.sparta.livechat.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * TokenBlacklistService의 기능을 검증하는 통합 테스트 클래스입니다.
 * SptingBootTest 와 @ActiveProfiles("test") 사용ㅎ해서
 * 테스트용 Redis 환경에서 실제 블랙리스트 등록 및 TTL 기반 자동 만료 로직을 검증합니다.
 * TokenBlacklistServiceTest.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TokenBlacklistServiceTest {

	@Autowired
	private TokenBlacklistService tokenBlacklistService;

	@Autowired
	private StringRedisTemplate redisTemplate;
	private static final String REDIS_KEY_PREFIX = "blacklist:";
	private static final String TEST_TOKEN = "test-jwt-token-to-blacklist";
	private static final long TTL_SECONDS = 10L;

	/**
	 * 테스트 간의 독립성을 위해 사용후 잔여치클 삭제합니다.
	 */
	@Test
	void cleanUp() {
		redisTemplate.delete(TEST_TOKEN);
	}

	/**
	 * 토큰을 블랙시르스테 추가했을때
	 * TokenBlacklistService#isBlacklisted(String)이 true를 반환하는지 확인합니다.
	 * Redis에 블랙리스트 토큰키로 logout 값이 저장되었는지 확인합니다.
	 * Redis에 설정된 TTL이 유효한 범위 내인지 확인합니다.
	 */
	@Test
	@DisplayName("성공: 토큰을 블랙리스트에 추가하고 즉시 확인")
	void addToBlacklist_Success() {
		// Given

		// When
		tokenBlacklistService.addToBlacklist(TEST_TOKEN, TTL_SECONDS);

		// Then
		assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN),
			"토큰이 블랙리스트에 성공적으로 등록되어야 합니다.");

		String value = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + TEST_TOKEN);
		Long ttl = redisTemplate.getExpire(REDIS_KEY_PREFIX + TEST_TOKEN, TimeUnit.SECONDS);

		assertEquals("logout", value, "Redis에 'logout' 값으로 저장되어야 합니다.");
		assertNotNull(ttl, "TTL은 null 이 아니어야 합니다.");
		assertTrue(ttl > 0 && ttl <= TTL_SECONDS,
			"TTL은 0초보다 크고 설정된 TTL(" + TTL_SECONDS + "초)보다 작거나 같아야 합니다.");

		redisTemplate.delete(REDIS_KEY_PREFIX + TEST_TOKEN);
	}

	/**
	 * 블랙리스트에 등록되지 않는 토큰을 조회했을때
	 * TokenBlacklistService#isBlacklisted(String) 가 false를 반환하는지 확인합니다.
	 */
	@Test
	@DisplayName("성공: 블랙리스트에 없는 토큰 확인 시 false 반환")
	void isBlacklisted_NotExists_ReturnsFalse() {
		// Given(테스트 토큰이 없어야됨)

		// When
		boolean isBlacklisted = tokenBlacklistService.isBlacklisted(TEST_TOKEN);

		// Then
		assertFalse(isBlacklisted, "블랙리스트에 없는 토큰은 false를 반환해야 합니다.");
	}

	/**
	 * 짧은 TTL 토큰을 등록하였을때
	 * 토큰이 Redis에서 자동 삭제되고 TokenBlacklistService#isBlacklisted(String)가
	 * false를 반환하는지 확인합니다.
	 * @throws InterruptedException THread.sleep 호출중 발생할수 있는 예외
	 */

	@Test
	@DisplayName("성공: TTL 만료 후 토큰이 자동 삭제되고 false 반환")
	void isBlacklisted_AfterExpiration_ReturnsFalse() throws InterruptedException {
		// Given
		final long SHORT_TTL = 2L;
		tokenBlacklistService.addToBlacklist(TEST_TOKEN, SHORT_TTL);
		assertTrue(tokenBlacklistService.isBlacklisted(TEST_TOKEN), "저장 직후는 true여야 합니다.");

		// When
		Thread.sleep(Duration.ofSeconds(SHORT_TTL).toMillis() + 1000);

		// Then
		boolean isBlacklisted = tokenBlacklistService.isBlacklisted(TEST_TOKEN);
		assertFalse(isBlacklisted, "TTL 만료 후에는 블랙리스트에서 제거되어 false를 반환해야 합니다.");

		assertFalse(redisTemplate.hasKey(REDIS_KEY_PREFIX + TEST_TOKEN), "키는 Redis 에서 삭제 되어야 합니다.");
	}
}

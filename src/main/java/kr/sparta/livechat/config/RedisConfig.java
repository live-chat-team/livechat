package kr.sparta.livechat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 데이터베이스와의 연결하는 Spring 클래스입니다.
 * yml 파일에서 Redis 정보를 읽어와 RedisConnectionFactory와 RedisTemplate을 생성하여
 * Spring IoC 컨데이너 등록합니다.
 * RedisConfig.java
 *
 * @author kimsehyun
 * @since 2025. 12. 16.
 */
@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	/**
	 * Redis 연결을 위한 ConnectionFactory를 생성합니다.
	 *
	 * @return Lettuce 기반의 Redis 연결 팩토리
	 */
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	/**
	 * 데이터 조작을 위한 RedisTemplate 객체를 생성합니다.
	 * Redis 저장시 문자열 형태로 지정합니다.
	 * @param connectionFactory Redis 연결 정보
	 * @return 문자열 기반 키값을 지원하는 템플릿
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
		RedisConnectionFactory connectionFactory
	) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new StringRedisSerializer());

		return template;
	}
}

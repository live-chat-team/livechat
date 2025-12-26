package kr.sparta.livechat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트 설정 클래스입니다.
 * Access Key나 Secret Key를 사용하지 않고,
 * 기본 자격 증명 공급자를 통해  사용합니다.
 * S3Config.java
 *
 * @author 변채주
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Configuration
public class S3Config {

	DefaultCredentialsProvider defaultCredentialsProvider = DefaultCredentialsProvider.builder().build();
	@Value("${cloud.aws.region.static}")
	private String region;

	/**
	 * S3Client Bean을 생성합니다.
	 *
	 * @return S3Client 인스턴스
	 */
	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(defaultCredentialsProvider)
			.build();
	}
}

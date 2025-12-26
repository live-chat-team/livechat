package kr.sparta.livechat.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import kr.sparta.livechat.dto.UserUploadProfileResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * S3ServiceTest 테스트 클래스입니다.
 * <p>
 *     대상 클래스: {@link S3Service}
 *     AWS S3를 활용해 프로필 이미지 수정 기능을 검증합니다.
 *     파일 형식, 크기 및 S3에 이미지 업로드/삭제 로직을 테스트합니다.
 * </p>
 * @author 변채주
 * @since 2025. 12. 23.
 */
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
	Pattern uuidPattern = Pattern.compile(
		"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg$");
	@Mock
	private S3Client s3Client;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private S3Service s3Service;
	private User user;
	private Long userId;
	private MockMultipartFile validFile;

	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
		ReflectionTestUtils.setField(s3Service, "region", "ap-northeast-2");

		userId = 1L;
		user = User.builder()
			.email("tester@test.com")
			.name("테스터")
			.password("password")
			.role(Role.BUYER)
			.build();
		ReflectionTestUtils.setField(user, "id", userId);
		user.updateProfileImage("new_profile_image.jpg");

		validFile = new MockMultipartFile("file",
			"test.jpg",
			"image/jpeg",
			"testContent".getBytes());
	}

	/**
	 * 프로필 이미지 업로드 성공 케이스를 검증합니다.
	 * <p>
	 *     요청 사용자 정보 조회 → 파일 검증 통과 → S3 업로드 → 새 URL을 DB에 반영(저장) → 응답 반환
	 *     S3 URL 형태와 UUID 형식 준수 여부를 검증합니다.
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 수정/업로드 성공")
	void successCaseUploadProfileImage() {
		//given
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		//when
		UserUploadProfileResponse response = s3Service.uploadImage(userId, validFile);
		String fileName = response.getProfileImageUrl().substring(response.getProfileImageUrl().lastIndexOf("/") + 1);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getName()).isEqualTo(user.getName());
		assertThat(response.getProfileImageUrl()).startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/");
		assertThat(uuidPattern.matcher(fileName).matches()).isTrue();
		assertThat(response.getProfileImageUrl()).endsWith(".jpg");

		verify(userRepository).findById(userId);
		verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		verify(userRepository).save(user);
		verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
	}

	/**
	 * 존재하지 않는 사용자로 프로필 이미지 업로드 시도 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 요청 사용자 조회 실패 -> AUTH_USER_NOT_FOUND 예외 발생 -> S3 호출 없음
	 * </p>
	 */
	@Test
	@DisplayName("프로필 수정 실패 - 사용자 조회 실패 시 404")
	void failCaseUploadProfileImage_UserNotFound() {
		//given
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		//when
		Throwable thrown = catchThrowable(() -> s3Service.uploadImage(userId, validFile));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.AUTH_USER_NOT_FOUND);

		verify(userRepository).findById(userId);
		verifyNoInteractions(s3Client);
	}

	/**
	 * 허용되지 않은 파일 형식(txt, exe 등) 업로드 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 요청 사용자 조회 성공 -> 파일 형식 검증 실패 -> PROFILE_INVALID_FORMAT 예외 발생 -> S3 호출 없음
	 * </p>
	 */
	@Test
	@DisplayName("프로필 수정 실패 - 잘못된 이미지 파일 형식 업로드 시 400")
	void failCaseUploadProfileImage_InvalidFileFormat() {
		//given
		MockMultipartFile invalidFile = new MockMultipartFile(
			"file",
			"invalidTest.txt",
			"text/plain",
			"text content".getBytes()
		);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		//when
		Throwable thrown = catchThrowable(() -> s3Service.uploadImage(userId, invalidFile));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PROFILE_INVALID_FORMAT);

		verify(userRepository).findById(userId);
		verifyNoInteractions(s3Client);
	}

	/**
	 * null 또는 빈 파일 업로드 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 요청 사용자 조회 성공 -> 파일 존재 여부 검증 실패 -> PROFILE_INVALID_DATA 예외 발생 -> S3 호출 없음
	 * </p>
	 */
	@Test
	@DisplayName("프로필 수정 실패 - 빈 파일 또는 null 업로드 시 400")
	void failCaseUploadProfileImage_InvalidFileData() {
		//given
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		//when
		Throwable thrown = catchThrowable(() -> s3Service.uploadImage(userId, null));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PROFILE_INVALID_DATA);

		verify(userRepository).findById(userId);
		verifyNoInteractions(s3Client);
	}

	/**
	 * 5MB를 초과하는 파일 업로드 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 요청 사용자 조회 성공 -> 파일 크기 검증 실패 -> PROFILE_SIZE_EXCEEDED 예외 발생 -> S3 호출 없음
	 * </p>
	 */
	@Test
	@DisplayName("프로필 수정 실패 - 파일 크기 5MB 초과 시 400")
	void failCaseUploadProfileImage_SizeExceeded() {
		//given
		MockMultipartFile largeFile = new MockMultipartFile(
			"file",
			"largeImage.jpg",
			"image/jpeg",
			new byte[6 * 1024 * 1024]
		);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		//when
		Throwable thrown = catchThrowable(() -> s3Service.uploadImage(userId, largeFile));

		//then
		assertThat(thrown).isInstanceOf(CustomException.class);
		CustomException ce = (CustomException)thrown;
		assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PROFILE_SIZE_EXCEEDED);

		verify(userRepository).findById(userId);
		verifyNoInteractions(s3Client);
	}

	/**
	 * 기본 프로필 이미지를 사용 중인 사용자가 이미지 업로드 시 기존 이미지 삭제가 발생하지 않는 케이스를 검증합니다.
	 * <p>
	 * 요청 사용자 조회 성공 -> 기본 이미지 확인 -> S3 업로드 -> 기존 이미지 삭제 안 함 -> DB 저장 -> 응답 반환
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 성공 - 기본 프로필 이미지 업로드 시 S3 삭제 처리되지 않음")
	void successCaseUploadProfileImage_S3ClientError() {
		//given
		user.updateProfileImage("default_profile_image.jpg");
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		//when
		UserUploadProfileResponse response = s3Service.uploadImage(userId, validFile);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getName()).isEqualTo(user.getName());
		assertThat(response.getProfileImageUrl()).isNotEqualTo("default_profile_image.jpg");

		verify(userRepository).findById(userId);
		verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		verify(userRepository).save(user);
		verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
	}
}

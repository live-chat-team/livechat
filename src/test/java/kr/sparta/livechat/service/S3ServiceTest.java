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

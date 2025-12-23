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
	void uploadImage_Success() {
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
}

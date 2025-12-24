package kr.sparta.livechat.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import kr.sparta.livechat.config.SecurityConfig;
import kr.sparta.livechat.dto.UserUploadProfileResponse;
import kr.sparta.livechat.entity.Role;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.AuthService;
import kr.sparta.livechat.service.JwtService;
import kr.sparta.livechat.service.S3Service;

/**
 * UserControllerTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스: {@link UserController}
 * 프로필 이미지 업로드 API 엔드포인트를 검증합니다.
 * 비즈니스 로직 {@link S3Service}를 활용해 사용자 인증, 응답 형식 등의 유효성을 테스트합니다.
 * </p>
 *
 * @author 변채주
 * @since 2025. 12. 23.
 */
@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "server.port=0")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private S3Service s3Service;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UserRepository userRepository;

	private MockMultipartFile validFile;

	@BeforeEach
	void setUp() {
		validFile = new MockMultipartFile("file",
			"test.jpg",
			"image/jpeg",
			"testContent".getBytes()
		);
	}

	private void loginAs(Long userId) {
		User user = mock(User.class);
		given(user.getId()).willReturn(userId);
		given(user.getEmail()).willReturn("test@test.com");
		given(user.getName()).willReturn("테스터");
		given(user.getPassword()).willReturn("password");
		given(user.getRole()).willReturn(Role.BUYER);
		given(user.getProfileImage()).willReturn("beforeProfileImage.png");

		CustomUserDetails userDetails = new CustomUserDetails(user);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	/**
	 * 인증된 사용자가 유효한 이미지 파일로 프로필 업로드 시 성공하는 케이스를 검증합니다.
	 * <p>
	 * 인증 성공 -> 파일 업로드 -> S3Service 호출 -> 200 OK 응답 -> JSON 응답 검증
	 * JSON 응답으로 {@link UserUploadProfileResponse} 형식이 맞는지 확인합니다.
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 성공 - 인증된 사용자는 200 OK, UserUploadProfileResponse JSON 반환 검증")
	void successCaseUpdateProfile() throws Exception {
		//given
		loginAs(1L);
		UserUploadProfileResponse response = new UserUploadProfileResponse(1L,
			"테스터",
			"https://test-bucket.s3.ap-northeast-2.amazonaws.com/uuid.jpg"
		);

		given(s3Service.uploadImage(anyLong(), any(MultipartFile.class))).willReturn(response);

		//when & then
		mockMvc.perform(multipart("/api/users/me/profile")
				.file(validFile)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				}))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.userId").value(1))
			.andExpect(jsonPath("$.name").value("테스터"))
			.andExpect(jsonPath("$.profileImageUrl").exists());

		then(s3Service).should(times(1)).uploadImage(anyLong(), any(MultipartFile.class));
	}

	/**
	 * 인증되지 않은 사용자의 프로필 업로드 시도 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 로그인하지 않음, 인증 없음 -> 403 Forbidden 반환
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 실패 - 로그인 하지 않았거나 인증이 없으면 403 Forbidden 반환 검증")
	void failCaseUpdateProfile_Forbidden() throws Exception {
		//given

		//when & then
		mockMvc.perform(multipart("/api/users/me/profile")
				.file(validFile)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				}))
			.andExpect(status().isForbidden());

		verifyNoInteractions(s3Service);
	}

	/**
	 * 파일 없이 프로필 업로드 시도 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 인증 성공 -> 파일 없음 -> 400 Bad Request, ErrorResponse 반환
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 실패 - 파일 없으면 400 Bad Request")
	void failCaseUpdateProfile_NoFile() throws Exception {
		//given
		loginAs(1L);

		given(s3Service.uploadImage(anyLong(), isNull()))
			.willThrow(new CustomException(ErrorCode.PROFILE_INVALID_DATA));

		//when & then
		mockMvc.perform(multipart("/api/users/me/profile")
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				}))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.PROFILE_INVALID_DATA.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_INVALID_DATA.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	/**
	 * 잘못된 파일 형식으로 프로필 업로드 시도 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 인증 성공 -> txt 파일 업로드 -> 400 Bad Request, ErrorResponse 반환
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 실패 - 잘못된 파일 형식은 400 Bad Request 반환 검증")
	void failCaseUpdateProfile_InvalidFormat() throws Exception {
		//given
		loginAs(1L);

		MockMultipartFile invalidFile = new MockMultipartFile(
			"file",
			"test.txt",
			"text/plain",
			"textContent".getBytes()
		);

		given(s3Service.uploadImage(anyLong(), any(MultipartFile.class)))
			.willThrow(new CustomException(ErrorCode.PROFILE_INVALID_FORMAT));

		//when & then
		mockMvc.perform(multipart("/api/users/me/profile")
				.file(invalidFile)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				}))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.PROFILE_INVALID_FORMAT.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_INVALID_FORMAT.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());
	}

	/**
	 * 제한된 파일 초과 크기로 프로필 업로드 시도 시 실패하는 케이스를 검증합니다.
	 * <p>
	 * 인증 성공 -> 5MB 초과 파일 업로드 -> 400 Bad Request, ErrorResponse 반환
	 * </p>
	 */
	@Test
	@DisplayName("프로필 이미지 업로드 실패 - 파일 크기 초과 시 400 Bad Request")
	void failUpdateProfile_SizeExceeded() throws Exception {
		//given
		loginAs(1L);

		MockMultipartFile largeFile = new MockMultipartFile("file",
			"large.jpg",
			"image/jpeg",
			new byte[6 * 1024 * 1024]
		);

		given(s3Service.uploadImage(anyLong(), any(MultipartFile.class)))
			.willThrow(new CustomException(ErrorCode.PROFILE_SIZE_EXCEEDED));

		//when & then
		mockMvc.perform(multipart("/api/users/me/profile")
				.file(largeFile)
				.with(request -> {
					request.setMethod("PATCH");
					return request;
				}))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.code").value(ErrorCode.PROFILE_SIZE_EXCEEDED.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorCode.PROFILE_SIZE_EXCEEDED.getMessage()))
			.andExpect(jsonPath("$.timestamp").exists());
	}
}

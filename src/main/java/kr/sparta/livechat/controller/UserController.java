package kr.sparta.livechat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kr.sparta.livechat.dto.UserUploadProfileResponse;
import kr.sparta.livechat.security.CustomUserDetails;
import kr.sparta.livechat.service.S3Service;
import lombok.RequiredArgsConstructor;

/**
 * UserController 클래스입니다.
 * <p>
 * 사용자의 프로필 이미지 수정 요청을 처리하는 컨트롤러입니다.
 * </p>
 * @author 변채주
 * @version 1.0
 * @since 2025. 12. 23.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	private final S3Service s3Service;

	/**
	 * 로그인한 사용자의 프로필 이미지 URL을 수정합니다.
	 * <p>
	 * 새로운 프로필 이미지 파일을 AWS S3에 업로드하고,
	 * 이전 이미지 파일을 S3에서 삭제합니다.
	 * 새로운 프로필 이미지의 S3 URL을 사용자 정보에 저장합니다.
	 * </p>
	 * @param userDetails 인증된 사용자 정보
	 * @param file 새로운 이미지 파일
	 * @return 수정된 사용자 정보(id, 이름, 수정된 프로필 이미지 URL)로 구성된 응답 DTO
	 */
	@PatchMapping("/me/profile")
	public ResponseEntity<UserUploadProfileResponse> updateProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		MultipartFile file) {
		UserUploadProfileResponse response = s3Service.uploadImage(userDetails.getUserId(), file);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}

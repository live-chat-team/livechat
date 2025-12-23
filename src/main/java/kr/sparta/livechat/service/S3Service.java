package kr.sparta.livechat.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import kr.sparta.livechat.dto.UserUploadProfileResponse;
import kr.sparta.livechat.entity.User;
import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3에 이미지 파일 업로드/삭제를 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class S3Service {

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	private final S3Client s3Client;
	private final UserRepository userRepository;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	/**
	 * 이미지 파일을 S3에 업로드합니다.
	 *
	 * @param id 요청 사용자 식별자
	 * @param file 업로드할 이미지 파일
	 * @return 변경된 이미지의 URL이 포함된 사용자 정보 응답
	 * @throws CustomException 404(사용자 없음)
	 * @throws CustomException 500(IOException - S3 업로드 실패 시 CustomException으로 예외 처리)
	 */
	@Transactional
	public UserUploadProfileResponse uploadImage(Long id, MultipartFile file) {

		User user = userRepository.findById(id).orElseThrow(
			() -> new CustomException(ErrorCode.AUTH_USER_NOT_FOUND));

		validateFile(file);

		String beforeImageUrl = user.getProfileImage();

		String originalFilename = file.getOriginalFilename();
		String extension = getFileExtension(originalFilename);
		String newFileName = UUID.randomUUID() + "." + extension;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(newFileName)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			String profileImageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region,
				newFileName);

			user.updateProfileImage(profileImageUrl);
			userRepository.save(user);

			if (user.getProfileImage().equals(profileImageUrl)) {
				deleteImage(beforeImageUrl);
			}

			return new UserUploadProfileResponse(user.getId(), user.getName(), profileImageUrl);
		} catch (IOException ex) {
			throw new CustomException(ErrorCode.COMMON_INTERNAL_ERROR);
		}

	}

	/**
	 * S3에서 이미지를 삭제합니다.
	 * 기본 이미지는 삭제하지 않습니다.
	 *
	 * @param imageUrl 삭제할 이미지의 S3 URL
	 */
	public void deleteImage(String imageUrl) {
		if (imageUrl == null || imageUrl.contains("default_profile_image")) {
			return;
		}

		String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(fileName)
			.build();

		s3Client.deleteObject(deleteObjectRequest);
	}

	/**
	 * 파일의 유효성(확장자 형식, 파일 크기 등)을 검증합니다.
	 *
	 * @param file 검증할 파일
	 * @throws CustomException 400(파일 형식 제한, 파일 크기 제한, 잘못된 입력값)
	 */
	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CustomException(ErrorCode.PROFILE_INVALID_DATA);
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new CustomException(ErrorCode.PROFILE_SIZE_EXCEEDED);
		}

		String extension = getFileExtension(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new CustomException(ErrorCode.PROFILE_INVALID_FORMAT);
		}
	}

	/**
	 * 파일명에서 확장자를 추출합니다.
	 *
	 * @param filename 파일명
	 * @return 확장자
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new CustomException(ErrorCode.PROFILE_INVALID_DATA);
		}
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
}

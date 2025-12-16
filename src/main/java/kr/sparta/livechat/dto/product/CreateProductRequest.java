package kr.sparta.livechat.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * createProductRequest 클래스입니다.
 * <p>
 * 상품관리의 CRUD 클래스 중 상품 등록에 관련된 입력값을 입력받는 DTO 클래스입니다.
 * 입력 시 제약에 맞춘 어노테이션을 사용
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 12.
 */
@Getter
@NoArgsConstructor
public class CreateProductRequest {

	@NotBlank
	@Size(max = 100)
	private String name;

	@NotNull
	@Min(0)
	private Integer price;

	@NotBlank
	@Size(max = 500)
	private String description;
}
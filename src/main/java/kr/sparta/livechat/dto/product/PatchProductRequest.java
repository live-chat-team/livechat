package kr.sparta.livechat.dto.product;

import kr.sparta.livechat.domain.role.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 수정 기능 구현 중 수정 요청을 보내는 클래스입니다.
 * <p>
 * 상품을 등록한 판매자만 수정이 가능하며, Patch를 사용함으로서 수정이 필요한 필드만 요청을 보낼 수 있습니다.
 * 전달되지 않은 필드는 기존 값을 유지합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Getter
@NoArgsConstructor
public class PatchProductRequest {
	private String name;
	private Integer price;
	private String description;
	private ProductStatus status;

}

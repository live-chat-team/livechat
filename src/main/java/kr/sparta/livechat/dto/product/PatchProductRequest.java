package kr.sparta.livechat.dto.product;

import kr.sparta.livechat.domain.role.ProductStatus;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class PatchProductRequest {
	private String name;
	private Integer price;
	private String description;
	private ProductStatus status;

	/**
	 * PATCH 요청이 빈 바디인지 여부를 판단합니다.
	 * <p>
	 * name, price, description, status 필드가 모두 그대로인 경우 수정할 내용이 없는 요청으로 간주합니다.
	 * 빈 바디 요청은 유효하지 않은 것으로 약속하여 컨트롤러 또는 서비스 레이어에서 {@code PRODUCT_INVALID_INPUT} 예외를 발생시켜 처리합니다.
	 * </p>
	 *
	 * @return 모든 수정 대상 필드가 {@code null}이면 {@code true}, 그렇지 않으면 {@code false}
	 */
	public boolean isEmpty() {
		return name == null && price == null && description == null && status == null;
	}

}

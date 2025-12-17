package kr.sparta.livechat.dto.product;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.entity.Product;
import lombok.Builder;
import lombok.Getter;

/**
 * CreateProductResponse 클래스입니다.
 * <p>
 * 상품등록 요청 이후 응답문을 처리하기 위한 DTO 클래스입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 12.
 */
@Getter
@Builder
public class CreateProductResponse {

	private Long productId;
	private Long sellerId;
	private String name;
	private Integer price;
	private String description;
	private LocalDateTime createdAt;

	public static CreateProductResponse from(Product product) {
		return CreateProductResponse.builder()
			.productId(product.getId())
			.sellerId(product.getSeller().getId())
			.name(product.getName())
			.price(product.getPrice())
			.description(product.getDescription())
			.createdAt(product.getCreatedAt())
			.build();
	}
}
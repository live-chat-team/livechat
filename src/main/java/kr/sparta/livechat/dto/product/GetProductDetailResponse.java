package kr.sparta.livechat.dto.product;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ProductStatus;
import lombok.Getter;

/**
 * 특정 상품의 상세 정보를 조회할 수 있는 DTO 클래스입니다.
 * <p>
 * 로그인 여부와 관계없이 상품을 조회할 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 17.
 */
@Getter
public class GetProductDetailResponse {

	private final Long productId;
	private final String name;
	private final Integer price;
	private final String description;
	private final Long sellerId;
	private final ProductStatus status;
	private final LocalDateTime createdAt;

	/**
	 * 상품 상세 조회 응답 DTO의 필드를 초기화하는 생성자입니다.
	 *
	 * @param productId   상품 고유 식별자
	 * @param name        상품명
	 * @param price       상품가격
	 * @param description 상품설명
	 * @param sellerId    판매자 ID
	 * @param status      상품의 판매상태(ONSALE / SOLDOUT)
	 * @param createdAt   상품의 등록일시
	 */
	private GetProductDetailResponse(Long productId, String name, int price, String description, Long sellerId,
		ProductStatus status, LocalDateTime createdAt) {
		this.productId = productId;
		this.name = name;
		this.price = price;
		this.description = description;
		this.sellerId = sellerId;
		this.status = status;
		this.createdAt = createdAt;
	}

	/**
	 * Product 엔티티를 기반으로 상품 상세 조회 응답 DTO를 생성합니다.
	 *
	 * @param product 조회된 상품 엔티티
	 * @return 상품 상세 조회 응답 DTO
	 */
	public static GetProductDetailResponse from(Product product) {
		return new GetProductDetailResponse(
			product.getId(),
			product.getName(),
			product.getPrice(),
			product.getDescription(),
			product.getSeller().getId(),
			product.getStatus(),
			product.getCreatedAt()
		);
	}
}

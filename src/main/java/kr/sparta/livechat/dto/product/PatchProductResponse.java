package kr.sparta.livechat.dto.product;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.role.ProductStatus;
import lombok.Getter;

/**
 * 상품 상세 정보를 응답으로 전달하기 위한 DTO 클래스입니다.
 * <p>
 * 상품 조회 및 수정 API의 응답으로 사용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Getter
public class PatchProductResponse {

	private final Long productId;
	private final String name;
	private final Integer price;
	private final String description;
	private final Long sellerId;
	private final ProductStatus status;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	/**
	 * 상품 수정 응답 DTO 생성자입니다.
	 *
	 * @param productId   상품 ID
	 * @param name        상품명
	 * @param price       상품 가격
	 * @param description 상품 설명
	 * @param sellerId    판매자 ID
	 * @param status      상품 판매 상태
	 * @param createdAt   상품 등록 일시
	 * @param updatedAt   상품 수정 일시
	 */
	public PatchProductResponse(Long productId, String name, Integer price, String description, Long sellerId,
		ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.productId = productId;
		this.name = name;
		this.price = price;
		this.description = description;
		this.sellerId = sellerId;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}

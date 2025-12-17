package kr.sparta.livechat.dto.product;

import java.time.LocalDateTime;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ProductStatus;
import lombok.Getter;

/**
 * 상품 목록 조회 시, 목록 내 단일 상품 정보를 담는 DTO입니다.
 * <p>
 * 목록 관련 필요한 필드만 포함하며, {@link Product} 엔티티를 DTO로 변환하여 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 17.
 */
@Getter
public class ProductListItem {

	private final Long productId;
	private final String name;
	private final int price;
	private final String description;
	private final Long sellerId;
	private final ProductStatus status;
	private final LocalDateTime createdAt;

	public ProductListItem(Product product) {
		this.productId = product.getId();
		this.name = product.getName();
		this.price = product.getPrice();
		this.description = product.getDescription();
		this.sellerId = product.getSeller().getId();
		this.status = product.getStatus();
		this.createdAt = product.getCreatedAt();
	}
}

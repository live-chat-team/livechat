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

	/**
	 * Product 엔티티를 기반으로 상품 목록 조회용 DTO를 생성합니다.
	 * <p>
	 * Service 계층에서 Product 엔티티를 직접 노출하지 않고,
	 * 목록 조회에 필요한 최소한의 정보만 전달하기 위해 사용됩니다.
	 * </p>
	 */
	public ProductListItem(Product product) {
		this.productId = product.getId();
		this.name = product.getName();
		this.price = product.getPrice();
		this.description = product.getDescription();
		this.sellerId = product.getSeller().getId();
		this.status = product.getStatus();
		this.createdAt = product.getCreatedAt();
	}

	/**
	 * Product 엔티티를 ProductListItem DTO로 변환하는 정적 팩토리 메서드입니다.
	 * <p>
	 * DTO 생성 책임을 DTO 자체로 위임하여 Service 계층의 역할을 단순화하고,
	 * 변환 로직의 응집도를 높이기 위해 사용됩니다.
	 * </p>
	 *
	 * @param product 변환 대상 Product 엔티티
	 * @return 변환된 ProductListItem DTO
	 */
	public static ProductListItem from(Product product) {
		return new ProductListItem(product);
	}
}

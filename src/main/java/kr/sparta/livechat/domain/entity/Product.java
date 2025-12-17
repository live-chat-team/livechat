package kr.sparta.livechat.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.dto.product.CreateProductRequest;
import kr.sparta.livechat.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품(Product) 정보를 영속화하기 위한 JPA 엔티티입니다.
 * <p>
 * 상품의 기본 속성(이름, 가격, 설명, 상태)과 판매자(User) 연관관계를 보유합니다.
 * 엔티티 생성은 정적 팩토리 메서드({@link #create(User, CreateProductRequest)})를 통해
 * 초기 상태 {@link ProductStatus#ONSALE}을 보장하도록 설계되었습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 12.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "products")
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	@Column(nullable = false, length = 500)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	/**
	 * 상품 등록을 위한 {@link Product} 엔티티를 생성합니다.
	 * 생성 시 상품의 초기 상태는 {@link ProductStatus#ONSALE}로 설정됩니다.
	 *
	 * @param seller 상품을 등록하는 판매자
	 * @param req    상품 등록 요청 데이터(이름, 가격, 설명 등)
	 * @return 생성된 {@link Product} 엔티티
	 */
	public static Product create(User seller, CreateProductRequest req) {
		return Product.builder()
			.seller(seller)
			.name(req.getName())
			.price(req.getPrice())
			.description(req.getDescription())
			.status(ProductStatus.ONSALE)
			.build();
	}
}

package kr.sparta.livechat.dto.chatroom;

import lombok.Getter;

/**
 * ProductInfo 클래스입니다.
 * <p>
 * 상품의 식별자와 상품명을 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 21.
 */
@Getter
public class ProductInfo {
	private final Long productId;
	private final String productName;

	/**
	 * 상품 기본정보를 담습니다.
	 *
	 * @param productId   상품 고유 식별자
	 * @param productName 상담 채팅방으로 문의한 상품 이름
	 */
	public ProductInfo(Long productId, String productName) {
		this.productId = productId;
		this.productName = productName;
	}
}

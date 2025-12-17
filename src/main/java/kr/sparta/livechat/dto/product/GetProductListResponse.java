package kr.sparta.livechat.dto.product;

import java.util.List;

import lombok.Getter;

/**
 * 상품 목록 조회 API 응답 DTO 클래스입니다.
 * <p>
 * 페이징 정보와 상품 목록을 함께 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 17.
 */
@Getter
public class GetProductListResponse {
	private final int page;
	private final int size;
	private final Long totalElements;
	private final int totalPages;
	private final boolean hasNext;
	private final List<ProductListItem> productList;

	/**
	 * 상품 목록 조회 응답 DTO를 생성합니다.
	 *
	 * @param page          요청 페이지 번호
	 * @param size          요청 페이지 크기
	 * @param totalElements 전체 상품 개수
	 * @param totalPages    전체 페이지 수
	 * @param hasNext       다음 페이지 존재 여부
	 * @param productList   상품 목록
	 */
	public GetProductListResponse(
		int page, int size, Long totalElements, int totalPages, boolean hasNext, List<ProductListItem> productList) {
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.hasNext = hasNext;
		this.productList = productList;
	}
}

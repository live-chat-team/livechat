package kr.sparta.livechat.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.sparta.livechat.domain.entity.Product;
import kr.sparta.livechat.domain.role.ProductStatus;
import kr.sparta.livechat.entity.User;

/**
 * ProductRepository 인터페이스입니다.
 * <p>
 * 상품(Product) 엔티티에 대한 CRUD 및 등록 시 중복/상태 검증을 위한 조회 메서드를 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 13.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
	boolean existsBySellerAndName(User seller, String name);

	Page<Product> findAllByStatusNot(ProductStatus status, Pageable pageable);

	Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
}

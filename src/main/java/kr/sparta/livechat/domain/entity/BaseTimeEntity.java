package kr.sparta.livechat.domain.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 엔티티의 생성 시간과 수정 시간을 자동으로 관리하기 위한 공통 베이스 추상 클래스입니다.
 * <p>
 * {@link AuditingEntityListener}를 사용하여 엔티티가 저장되거나 수정될 때
 * {@code createdAt}, {@code updatedAt} 필드를 자동으로 설정합니다.
 * </p>
 *
 * <p>
 * 본 클래스는 {@code @MappedSuperclass}로 선언되어 있으며,
 * 실제 테이블로 매핑되지 않고 상속받는 엔티티에 필드만 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 12.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

}

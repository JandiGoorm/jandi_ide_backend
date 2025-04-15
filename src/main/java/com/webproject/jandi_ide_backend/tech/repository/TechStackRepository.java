package com.webproject.jandi_ide_backend.tech.repository;

// TechStack 엔티티 클래스를 가져옴
import com.webproject.jandi_ide_backend.tech.entity.TechStack;
// JPA에서 제공하는 기본 CRUD 기능이 정의된 인터페이스
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 기술 스택(TechStack) 엔티티에 대한 데이터베이스 접근을 처리하는 인터페이스
 * Spring Data JPA가 이 인터페이스의 구현체를 자동으로 생성해주기 때문에,
 * 별도의 구현 없이도 CRUD 기능을 사용할 수 있음.
 */
public interface TechStackRepository extends JpaRepository<TechStack, Integer> {

    /**
     * 기술 스택의 이름(name)으로 DB에서 해당 엔티티를 검색하는 메서드
     * Spring Data JPA의 메서드 이름 규칙을 따르기 때문에,
     * 메서드 이름만으로도 자동으로 쿼리가 생성됨.
     * SELECT * FROM tech_stack WHERE name = ? LIMIT 1;
     *
     * @param name 검색할 기술 스택의 이름
     * @return 해당 이름을 가진 TechStack 엔티티가 존재하면 Optional로 감싸서 반환, 없으면 빈 Optional
     */
    Optional<TechStack> findByName(String name);
}

package com.webproject.jandi_ide_backend.tech.service;

// 클라이언트로부터 전달받는 요청 DTO
import com.webproject.jandi_ide_backend.tech.dto.TechStackRequestDTO;
// 클라이언트에게 응답할 DTO
import com.webproject.jandi_ide_backend.tech.dto.TechStackResponseDTO;
// 실제 데이터베이스와 매핑되는 엔티티
import com.webproject.jandi_ide_backend.tech.entity.TechStack;
// 데이터베이스와 연결되는 레포지토리 인터페이스
import com.webproject.jandi_ide_backend.tech.repository.TechStackRepository;

// 트랜잭션 처리를 위한 어노테이션 (javax가 아닌 jakarta)
import jakarta.transaction.Transactional;
// 스프링의 서비스 빈으로 등록하는 어노테이션
import org.springframework.stereotype.Service;

/**
 * 기술 스택(TechStack)에 대한 비즈니스 로직을 처리하는 서비스 클래스
 * 컨트롤러로부터 요청을 받아 데이터를 가공하거나, 중복 검사 및 예외 처리 등을 수행하고,
 * 필요한 경우 DB 접근을 위해 Repository와 연결됨.
 */
@Service // 이 클래스가 Spring의 서비스 컴포넌트임을 나타냄 (빈으로 등록됨)
public class TechStackService {

    // 기술 스택에 대한 DB 작업을 처리할 레포지토리
    private final TechStackRepository techStackRepository;

    /**
     * 생성자 주입 방식으로 Repository 객체를 주입받음
     * @param techStackRepository 기술 스택용 JPA Repository
     */
    public TechStackService(TechStackRepository techStackRepository) {
        this.techStackRepository = techStackRepository;
    }

    /**
     * 새로운 기술 스택을 DB에 저장하는 메서드
     * 중복된 이름은 허용하지 않으며, 저장된 결과를 응답 DTO로 반환함.
     *
     * @param requestDto 클라이언트로부터 전달된 기술 스택 이름 정보
     * @return 저장된 기술 스택의 ID와 이름이 담긴 응답 DTO
     */
    @Transactional // 메서드 실행 중 예외 발생 시 롤백을 위해 트랜잭션 처리
    public TechStackResponseDTO createTechStack(TechStackRequestDTO requestDto) {

        // 🔍 1. 이름 중복 검사
        techStackRepository.findByName(requestDto.getName()).ifPresent(ts -> {
            // 동일한 이름의 기술 스택이 이미 존재하는 경우 예외 발생
            throw new IllegalArgumentException("이미 존재하는 기술 스택입니다.");
        });

        // 💾 2. 새로운 TechStack 엔티티 생성 및 이름 설정
        TechStack techStack = new TechStack();
        techStack.setName(requestDto.getName());

        // 💽 3. DB에 저장 (INSERT)
        TechStack saved = techStackRepository.save(techStack);

        // 📦 4. 저장된 결과를 응답 DTO로 포장하여 반환
        return new TechStackResponseDTO(saved.getId(), saved.getName());
    }
}

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

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 기존 기술 스택의 이름을 수정하는 메서드
     * ID를 기준으로 엔티티를 찾아서 이름만 새 값으로 교체함
     *
     * @param id 수정할 기술 스택의 고유 ID
     * @param requestDto 새로운 이름을 담고 있는 요청 객체
     * @return 수정된 기술 스택 정보를 담은 응답 DTO
     */
    @Transactional // 수정 작업도 트랜잭션으로 처리 (중간에 오류 발생 시 백)
    public TechStackResponseDTO updateTechStack(Integer id, TechStackRequestDTO requestDto) {

        // 🔍 [1단계] ID로 대상 기술 스택 조회 (없으면 예외 발생)
        TechStack techStack = techStackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 기술 스택이 존재하지 않습니다."));

        // 🔍 [2단계] 중복 이름 검사 (단, 자기 자신은 제외해야 함)
        techStackRepository.findByName(requestDto.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                // 다른 엔티티가 이 이름을 이미 쓰고 있다면 예외 처리
                throw new IllegalArgumentException("이미 존재하는 기술 스택입니다.");
            }
        });

        // ✏️ [3단계] 이름 변경
        techStack.setName(requestDto.getName());

        // 💽 [4단계] 변경된 내용 저장 (JPA의 dirty checking으로 update 쿼리 수행됨)
        TechStack updated = techStackRepository.save(techStack);

        // 📦 [5단계] 수정된 결과를 응답용 DTO로 포장하여 반환
        return new TechStackResponseDTO(updated.getId(), updated.getName());
    }


    /**
     * 기술 스택을 삭제하는 메서드
     * 주어진 ID에 해당하는 기술 스택이 존재하는지 먼저 확인하고,
     * 존재할 경우 해당 엔티티를 데이터베이스에서 삭제합니다.
     *
     * @param id 삭제할 기술 스택의 고유 ID (기본키)
     */
    @Transactional // 메서드 실행 중 예외 발생 시 데이터 변경사항을 모두 롤백하도록 트랜잭션 처리
    public void deleteTechStack(Integer id) {

        // 🔍 1단계: 삭제 대상 기술 스택이 실제로 DB에 존재하는지 확인
        // findById(id)는 Optional<TechStack>을 반환하므로,
        // 값이 없을 경우 orElseThrow()를 통해 예외를 발생시켜 요청을 종료시킴
        TechStack techStack = techStackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 기술 스택이 존재하지 않습니다."));

        // 🗑️ 2단계: 존재하는 기술 스택 객체를 데이터베이스에서 삭제
        // JPA에서 delete(entity) 메서드는 해당 엔티티를 삭제하는 역할을 수행
        techStackRepository.delete(techStack);
    }

    @Transactional
    public List<TechStackResponseDTO> getAllTechStacks() {
        /**
         * 기술 스택 전체 목록을 조회하는 메서드
         *
         * - 데이터베이스에 저장된 모든 기술 스택(TechStack 엔티티)을 조회한 후,
         * - 클라이언트 응답용 DTO 리스트로 변환하여 반환합니다.
         *
         * @return List<TechStackResponseDTO> - ID와 name만 담긴 DTO 리스트
         */

        // 1. 레포지토리를 통해 모든 기술 스택 엔티티 조회
        return techStackRepository.findAll().stream()

                // 2. 각 엔티티를 클라이언트 응답용 DTO로 변환 (ID, name만 포함)
                .map(ts -> new TechStackResponseDTO(ts.getId(), ts.getName()))

                // 3. 스트림을 리스트로 수집하여 반환
                .collect(Collectors.toList());
    }

}

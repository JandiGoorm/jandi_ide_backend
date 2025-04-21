package com.webproject.jandi_ide_backend.algorithm.problemSet.service;

import com.webproject.jandi_ide_backend.algorithm.problemSet.Repository.ProblemSetRepository;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.PostReqProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.PostRespProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.UpdateReqProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ProblemSetService {
    private final ProblemSetRepository problemSetRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public ProblemSetService(ProblemSetRepository problemSetRepository, UserRepository userRepository, CompanyRepository companyRepository) {
        this.problemSetRepository = problemSetRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    /// create
    public Object createProblemSet(PostReqProblemSetDTO probSetDTO, String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 만약 기업 문제인 경우 기업 확인
        Company company = null;
        if(probSetDTO.getIsCompanyProb()){
            String companyName = probSetDTO.getCompanyName();
            if(companyName == null){
                throw new RuntimeException("기업 문제인 경우 기업을 선택해야 합니다.");
            }
            company = (Company) companyRepository.findByCompanyName(companyName).orElse(null);
            if(company == null){
                throw new RuntimeException("존재하지 않는 기업을 선택했습니다.");
            }
        }

        // 데이터베이스에 문제 추가 및 반환
        ProblemSet problemSet = createData(probSetDTO, user, company);
        return new PostRespProblemSetDTO(problemSet);
    }

    /// read
    public List<PostRespProblemSetDTO> readProblemSet(String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 문제집 조회
        List<ProblemSet> problemSetList = problemSetRepository.findAllByUser(user);
        return problemSetList.stream()
                .map(PostRespProblemSetDTO::fromEntity)
                .toList();
    }
    
    /// upate
    public Object updateProblemSet(Long problemSetId, UpdateReqProblemSetDTO probSetDTO, String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 변경할 이름을 잘못 전달했다면 에러 반환
        if(probSetDTO.getTitle().isEmpty()){
            throw new RuntimeException("변경할 이름을 적어주세요");
        }

        // 이름 변경 및 반환
        ProblemSet problemSet = updateData(problemSetId, user, probSetDTO.getTitle());
        return new PostRespProblemSetDTO(problemSet);
    }


    // 데이터베이스에 추가
    public ProblemSet createData(PostReqProblemSetDTO probSetDTO, User user, Company company) {
        ProblemSet problemSet = new ProblemSet();
        problemSet.setTitle(probSetDTO.getTitle());
        problemSet.setIsPrevious(probSetDTO.getIsCompanyProb());
        problemSet.setProblems(probSetDTO.getProblemIds());
        problemSet.setSolvingTimeInMinutes(probSetDTO.getMinutes());
        problemSet.setUser(user);
        problemSet.setCompany(company);
        problemSet.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        problemSet.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        problemSetRepository.save(problemSet);
        return problemSet;
    }

    public ProblemSet updateData(Long problemSetId, User user, String newTitle) {
        // 문제집 검증
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new RuntimeException("문제집이 존재하지 않습니다."));

        // 본인의 문제집이 아니라면 에러 반환
        if(!problemSet.getUser().equals(user)){
            throw new RuntimeException("본인만 수정할 수 있습니다");
        }

        // 문제집 수정 및 반환
        problemSet.setTitle(newTitle);
        problemSet.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        problemSetRepository.save(problemSet);
        return problemSet;
    }
}

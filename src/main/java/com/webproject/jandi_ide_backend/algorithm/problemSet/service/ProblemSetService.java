package com.webproject.jandi_ide_backend.algorithm.problemSet.service;

import com.webproject.jandi_ide_backend.algorithm.problemSet.Repository.ProblemSetRepository;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.PostReqProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.PostRespProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
                .orElseThrow(() -> new RuntimeException("유저가 없습니다."));

        // 만약 기업 문제인 경우 기업 확인
        Company company = null;
        if(probSetDTO.getIsCompanyProb()){
            String companyName = probSetDTO.getCompanyName();
            if(companyName == null){
                throw new RuntimeException("기업 문제인 경우 기업을 선택해야 합니다.");
            }
            company = (Company) companyRepository.findByCompanyName(companyName).orElse(null);
            if(company == null){
                throw new RuntimeException("잘못된 기업을 선택했습니다.");
            }
        }

        ProblemSet problemSet = createNewData(probSetDTO, user, company);
        return new PostRespProblemSetDTO(problemSet);
    }

    // 데이터베이스에 추가
    public ProblemSet createNewData(PostReqProblemSetDTO probSetDTO, User user, Company company) {
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
}

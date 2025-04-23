package com.webproject.jandi_ide_backend.algorithm.problemSet.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.problemSet.Repository.ProblemSetRepository;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqPostProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.ReqUpdateProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.RespDetailProblemSet;
import com.webproject.jandi_ide_backend.algorithm.problemSet.dto.RespProblemSetDTO;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.company.entity.Company;
import com.webproject.jandi_ide_backend.company.repository.CompanyRepository;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSetService {
    private final ProblemSetRepository problemSetRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProblemService problemService;

    /// API
    public RespProblemSetDTO createProblemSet(ReqPostProblemSetDTO probSetDTO, String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 문제집 유형에 따라 company, problemIds 매칭
        Company company;
        List<Integer> problemIds;
        if(probSetDTO.getIsCompanyProb()){ // 기업 문제집인 경우
            company = getCompanyByName(probSetDTO.getCompanyName());
            problemIds = setRandProblems(company);
        }
        else{ // 커스텀 문제집인 경우
            company = null;
            problemIds = getCustomProblems(probSetDTO.getProblemIds());
        }

        // 데이터베이스에 문제 추가 및 반환
        ProblemSet problemSet = createData(probSetDTO, user, company, problemIds);
        return new RespProblemSetDTO(problemSet);
    }

    public List<RespProblemSetDTO> readProblemSet(String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 문제집 조회
        List<ProblemSet> problemSetList = problemSetRepository.findAllByUser(user);
        return problemSetList.stream()
                .map(RespProblemSetDTO::fromEntity)
                .toList();
    }

    public RespDetailProblemSet readProblemSetDetail(Long id) {
        ProblemSet problemSet = problemSetRepository.findById(id).orElseThrow(()->new CustomException(CustomErrorCodes.PROBLEMSET_NOT_FOUND));
        List<ProblemDetailResponseDTO> problems = getProblemsInProblemSet(problemSet);

        return new RespDetailProblemSet(
                problemSet.getId(),
                problemSet.getTitle(),
                problemSet.getIsPrevious(),
                problemSet.getSolvingTimeInMinutes(),
                problemSet.getDescription(),
                problemSet.getLanguage(),
                problemSet.getCreatedAt(),
                problemSet.getUpdatedAt(),
                problems
        );
    }

    public RespProblemSetDTO updateProblemSet(Long problemSetId, ReqUpdateProblemSetDTO probSetDTO, String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 변경할 이름을 잘못 전달했다면 에러 반환
        if(probSetDTO.getTitle().isEmpty()){
            throw new RuntimeException("변경할 이름을 적어주세요");
        }

        // 이름 변경 및 반환
        ProblemSet problemSet = updateData(problemSetId, user, probSetDTO.getTitle());
        return new RespProblemSetDTO(problemSet);
    }

    public Boolean deleteProblemSet(Long problemSetId, String githubId) {
        // 유저 검증
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // 문제집 삭제 및 성공 여부 반환
        deleteData(problemSetId, user);
        return problemSetRepository.findById(problemSetId).isEmpty(); // 없으면 정상 삭제로 간주
    }

    /// 함수 분리
    // 기업 문제집 - 기업 가져오기
    private Company getCompanyByName(String companyName) {
        if(companyName == null){
            throw new RuntimeException("기업 문제인 경우 기업을 선택해야 합니다.");
        }
        Company company = (Company) companyRepository.findByCompanyName(companyName).orElse(null);
        if(company == null){
            throw new RuntimeException("존재하지 않는 기업을 선택했습니다.");
        }
        return company;
    }

    // 기업 문제집 - 랜덤 지정 문제 로드
    private List<Integer> setRandProblems(Company company) {
        List<Integer> problemIds = new ArrayList<>(); // 반환할 문제 id 값의 배열
        Map<Integer, Integer> levelCountMap = new HashMap<>();

        // 레벨별로 몇 개 뽑아야 하는지 세기
        for (int level : company.getLevels()) {
            levelCountMap.put(level, levelCountMap.getOrDefault(level, 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : levelCountMap.entrySet()) {
            int level = entry.getKey();
            int count = entry.getValue();

            // 레벨에 해당하는 모든 문제 가져오기
            List<Problem> problemsByLevel = problemRepository.findByLevel(level);

            // 문제 수가 부족하면 예외 처리
            if (problemsByLevel.size() < count) {
                throw new CustomException(CustomErrorCodes.INSUFFICIENT_PROBLEMS);
            }

            // 랜덤 셔플 후 앞에서 count 개 선택
            Collections.shuffle(problemsByLevel);
            List<Problem> selected = problemsByLevel.subList(0, count);

            for (Problem p : selected) {
                problemIds.add(p.getId());
            }
        }

        return problemIds;
    }

    // 커스텀 문제집 - 사용자 지정 문제 로드
    private List<Integer> getCustomProblems(List<Integer> problemIds) {
        // 사용자가 선택한 문제가 없다면 에러 처리
        if(problemIds == null){
            throw new RuntimeException("선택된 문제가 없습니다.");
        }

        // 존재하는 문제만 선별
        List<Integer> customProblems = new ArrayList<>();
        for (Integer problemId : problemIds) {
            if (problemRepository.findById(problemId).isPresent()) {
                log.info("{} 존재", problemId);
                customProblems.add(problemId);
            }else{
                log.info("{} 미존재", problemId);
            }
        }
        if(customProblems.isEmpty()) // 선택된 문제는 있었으나 잘못된 문제뿐이었던 경우 에러 반환
            throw new RuntimeException("잘못된 문제를 선택하셨습니다.");

        return customProblems;
    }

    //문제집에 포함된 문제들의 자세한 내용을 로드
    public List<ProblemDetailResponseDTO> getProblemsInProblemSet(ProblemSet problemSet){
        List<Integer> problemIds = problemSet.getProblems();
        return problemIds.stream()
                .map(problemService::getProblemDetail)
                .collect(Collectors.toList());
    }

    /// 실제 DB CRUD
    // 데이터베이스에 추가
    private ProblemSet createData(ReqPostProblemSetDTO probSetDTO, User user, Company company, List<Integer> problemIds) {
        ProblemSet problemSet = new ProblemSet();
        problemSet.setTitle(probSetDTO.getTitle());
        problemSet.setIsPrevious(probSetDTO.getIsCompanyProb());
        problemSet.setProblems(problemIds);
        problemSet.setSolvingTimeInMinutes(probSetDTO.getMinutes());
        problemSet.setUser(user);
        problemSet.setCompany(company);
        problemSet.setLanguage(probSetDTO.getLanguage());
        problemSet.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        problemSet.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        problemSetRepository.save(problemSet);
        return problemSet;
    }

    // 데이터베이스 수정
    private ProblemSet updateData(Long problemSetId, User user, String newTitle) {
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

    // 데이터베이스에서 삭제
    private void deleteData(Long problemSetId, User user) {
        // 문제집 검증
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new RuntimeException("문제집이 존재하지 않습니다."));

        // 본인의 문제집이 아니라면 에러 반환
        if(!problemSet.getUser().equals(user)){
            throw new RuntimeException("본인만 삭제할 수 있습니다");
        }

        // 문제집 삭제
        problemSetRepository.delete(problemSet);
    }
}

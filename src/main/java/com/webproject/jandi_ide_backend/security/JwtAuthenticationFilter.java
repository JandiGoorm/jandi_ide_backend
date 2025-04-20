package com.webproject.jandi_ide_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webproject.jandi_ide_backend.global.ErrorResponseDTO;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("request:{}",request.getHeader("Authorization"));

        try{
            String token = jwtTokenProvider.resolveToken(request);
            log.info("token:{}",token);
            if(token == null) {
                if(isSecuredPath(request)){
                    handleCustomException(response,new CustomException(CustomErrorCodes.INVALID_JWT_TOKEN));
                    return;
                }else{
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            try{
                jwtTokenProvider.decodeToken(token);

                Authentication auth = jwtTokenProvider.getAuthentication(token);
                log.info("auth:{}",auth);
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            } catch (CustomException ex){
                log.info("token decoding failed");
                handleCustomException(response,ex);
            }
        } catch (Exception e) {
            log.error("error:{}",e);
            handleGenericException(response);
        }
    }

    // 커스텀한 에러를 위해
    private void handleCustomException(HttpServletResponse response, CustomException ex) throws IOException {
        response.setStatus(ex.getCustomErrorCode().getStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getCustomErrorCode().getStatusCode().value(),
                ex.getCustomErrorCode().getErrorCode(),
                ex.getCustomErrorCode().getMessage(),
                ex.getCustomErrorCode().getTimestamp()
        );

        // JSON 응답 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    // 일반적인 에러를 위해
    private void handleGenericException(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증에 실패했습니다.\"}");
    }

    // 인증이 필요한 요청인지 확인
    private boolean isSecuredPath(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/") &&
                !path.equals("/api/users/login") &&
                !path.equals("/api/users/refresh");
    }
} 
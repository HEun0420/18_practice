package com.ohgiraffers.api_request.section01;

import com.ohgiraffers.api_request.section01.dto.RequestDTO;
import com.ohgiraffers.api_request.section01.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;  // 추가
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class RestTemplateService {

    private final RestTemplate restTemplate;

    // FastAPI 서버 URL
    private static final String FAST_API_SERVER_URL = "http://localhost:8000/analyze";

    public RestTemplateService(RestTemplate restTemplate) {
        this.restTemplate = new RestTemplate();
    }

    public ResponseDTO AnalyzationUrl(RequestDTO requestDTO) {

        // 1. Form 데이터 준비 (MultiValueMap 사용)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("image_url", requestDTO.getImage_url());
        formData.add("locations", requestDTO.getLocations());

        // 2. HttpHeaders 설정 (Form 데이터 전송을 위한 Content-Type)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. HttpEntity에 Form 데이터와 헤더 추가
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        try {
            // 4. FastAPI로 POST 요청을 보내고 응답 받기
            ResponseEntity<ResponseDTO> response = restTemplate.exchange(
                    FAST_API_SERVER_URL,    // FastAPI URL
                    HttpMethod.POST,        // HTTP 메서드 (POST)
                    entity,                 // 요청 본문 (Form 데이터 + 헤더)
                    ResponseDTO.class       // 응답 타입 (ResponseDTO로 매핑)
            );

            // FastAPI에서 받은 응답을 로그로 출력하고 반환
            log.info("FastAPI 응답: {}", response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("FastAPI 호출 중 오류 발생", e);
            throw new RuntimeException("FastAPI 호출 실패", e);
        }
    }
}

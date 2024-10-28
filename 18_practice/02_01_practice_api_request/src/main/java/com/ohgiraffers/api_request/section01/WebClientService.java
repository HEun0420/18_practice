package com.ohgiraffers.api_request.section01;

import com.ohgiraffers.api_request.section01.dto.RequestDTO;
import com.ohgiraffers.api_request.section01.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WebClientService {

    private final WebClient webClient;

    // FastAPI 서버 URL
    private static final String FAST_API_SERVER_URL = "http://localhost:8000";

    public WebClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(FAST_API_SERVER_URL).build();
    }

    public Mono<ResponseDTO> AnalyzationUrl(RequestDTO requestDTO) {
        return webClient.post()
                .uri(FAST_API_SERVER_URL+"/analyze") // root URI 사용
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("image_url", requestDTO.getImage_url())
                        .with("locations", requestDTO.getLocations()))
                .retrieve()
                .bodyToMono(ResponseDTO.class)
                .doOnError(e -> log.error("FastAPI 호출 중 오류 발생", e));
    }
}

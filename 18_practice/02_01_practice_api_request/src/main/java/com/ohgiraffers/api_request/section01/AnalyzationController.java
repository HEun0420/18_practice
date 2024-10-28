package com.ohgiraffers.api_request.section01;

import com.ohgiraffers.api_request.section01.dto.RequestDTO;
import com.ohgiraffers.api_request.section01.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/analyze")
@Slf4j
public class AnalyzationController {

    private final WebClientService webClientService;
    private final RestTemplateService restTemplateService;

    // 생성자 주입 (두 서비스 모두 주입)
    public AnalyzationController(RestTemplateService restTemplateService, WebClientService webClientService) {
        this.restTemplateService = restTemplateService;
        this.webClientService = webClientService;
    }

    // 테스트 엔드포인트
    @GetMapping("/test")
    public String test() {
        log.info("/test로 get 요청 들어옴...");
        return "요청 성공";
    }

    // RestTemplate로 POST 요청 처리
    @PostMapping("/resttemplate")
    public ResponseDTO translateByRestTemplate(@RequestBody RequestDTO requestDTO) {
        log.info("번역[RestTemplate] controller 요청 들어옴....");
        log.info("url: {}, location: {}", requestDTO.getImage_url(), requestDTO.getLocations());

        ResponseDTO result = restTemplateService.AnalyzationUrl(requestDTO);
        return result;
    }

    // WebClient로 POST 요청 처리
    @PostMapping("/webclient")
    public Mono<ResponseDTO> translateByWebClient(@RequestBody RequestDTO requestDTO) {
        log.info("번역[WebClient] controller 요청 들어옴....");
        log.info("url: {}, location: {}", requestDTO.getImage_url(), requestDTO.getLocations());

        Mono<ResponseDTO> result = webClientService.AnalyzationUrl(requestDTO);
        return result;
    }
}

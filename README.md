<div align="center">

<!-- logo -->

### 사진 속 장소와 input한 도시와의 유사도 ✅

[<img src="https://img.shields.io/badge/-readme.md-important?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/-tech blog-blue?style=flat&logo=google-chrome&logoColor=white" />]() [<img src="https://img.shields.io/badge/release-v0.0.0-yellow?style=flat&logo=google-chrome&logoColor=white" />]() 
<br/> [<img src="https://img.shields.io/badge/프로젝트 기간-2024.12.10~2024.12.19-green?style=flat&logo=&logoColor=white" />]()

</div> 

## 📝 소개


1. **어떤 문제를 해결하고 싶은지 정의**
    
        가지고 있는 이미지가 입력한 지역과 유사한지 알기
    
2. **문제를 해결 할 수 있는 ai task 정리**
    - 이미지의 url를 넣고, 해당 이미지가 어느 지역인지(choices) 텍스트 입력해보기(생략시 정확도가 떨어짐),
    이미지를 분석하여 입력된 지역들의 유사도를 비교 후 가장 유사도가 높은 값이 출력
    - 한국어로 입력된 텍스트를 영어로 변환하여 찾아주고, 결과값을 입력한 단어(한국어)로 출력
3. **task 별 모델들을 가져와 적합한 모델 찾기**
    
    geolocal/StreetCLIP
    
    google-t5/t5-base
    
    - 코드
        
        ```jsx
        from PIL import Image
        import requests
        import torch
        from transformers import CLIPProcessor, CLIPModel
        from googletrans import Translator
        
        model = CLIPModel.from_pretrained("geolocal/StreetCLIP")
        processor = CLIPProcessor.from_pretrained("geolocal/StreetCLIP")
        
        # 이미지 불러오기
        url = "https://huggingface.co/geolocal/StreetCLIP/resolve/main/sanfrancisco.jpeg"
        image = Image.open(requests.get(url, stream=True).raw)
        
        # 지역 이름 입력받기
        choices = []
        original_choices = []  # 원래 입력한 지역 이름을 저장할 리스트(한국어)
        translator = Translator()  # 번역기 초기화
        
        while True:
            print("지역 이름을 입력하세요. 입력을 끝내려면 'x'를 입력하세요.")
            
            while True:
                location = input("지역 이름: ")
                if location.lower() == "x":
                    break
                if location:  
                    translated_location = translator.translate(location, src='ko', dest='en').text
                    choices.append(translated_location)  # 번역된 지역 이름 추가(영어)
                    original_choices.append(location)  # 원래 지역 이름 추가(한국어)
        
            if not choices:
                print("입력된 지역이 없습니다. 정확한 추론을 위해 한 가지 이상의 지역을 입력해주세요.")
            else:
                break
        
        # 이미지와 입력한 텍스트를 모델에 전달
        inputs = processor(text=choices, images=image, return_tensors="pt", padding=True)
        
        outputs = model(**inputs)
        logits_per_image = outputs.logits_per_image
        probs = logits_per_image.softmax(dim=1)
        
        # 유사도가 가장 큰 값의 인덱스 구하고, 유사도 값 구하기
        max_index = torch.argmax(logits_per_image).item()
        most_similar_location = original_choices[max_index]  # 원래 지역 이름(한국어) 사용
        similarity = probs[0, max_index].item() * 100  # 유사도 값을 퍼센트로 변환
        
        if similarity < 70:
            print(f"입력하신 지역들은 이미지와 유사도가 낮아 정확하게 찾아줄 수는 없습니다.\n 그러나 이 이미지는 현재 입력하신 지역 중 {most_similar_location}가 가장 유사도가 높습니다. 유사도는 {similarity:.2f}%입니다.")
        else:
            print(f"이 이미지는 {most_similar_location}가 가장 유사도가 높습니다. 유사도는 {similarity:.2f}%입니다.")
        
        ```
        
4. **모델을 이용한 fast api 서빙**
    - API 코드
        
        ```jsx
        from fastapi import FastAPI, HTTPException
        from fastapi.middleware.cors import CORSMiddleware
        from pydantic import BaseModel
        from PIL import Image
        import requests
        import torch
        from transformers import CLIPProcessor, CLIPModel
        from googletrans import Translator
        from io import BytesIO
        
        app = FastAPI()
        
        app.add_middleware(
            CORSMiddleware,
            allow_origins=["*"],  
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )
        
        model = CLIPModel.from_pretrained("geolocal/StreetCLIP")
        processor = CLIPProcessor.from_pretrained("geolocal/StreetCLIP")
        translator = Translator() 
        
        class LocationRequest(BaseModel):
            image_url: str
            locations: list[str]
        
        @app.post("/analyze/")
        async def analyze_location(request: LocationRequest):
            try:
                image = Image.open(requests.get(request.image_url, stream=True).raw)
            except Exception as e:
                raise HTTPException(status_code=400, detail="URL을 입력해주세요.")
        
            # 지역 이름 입력받기
            choices = []
            original_choices = []  
        
            for location in request.locations:
                if location:  
                    translated_location = translator.translate(location, src='ko', dest='en').text
                    choices.append(translated_location)  # 번역된 지역 이름 추가
                    original_choices.append(location)  # 원래 지역 이름 추가
        
            # 예외처리
            if not choices:
                raise HTTPException(status_code=400, detail="지역을 입력해주세요.")
        
            # 이미지와 입력한 텍스트를 모델에 전달
            inputs = processor(text=choices, images=image, return_tensors="pt", padding=True)
        
            outputs = model(**inputs)
            logits_per_image = outputs.logits_per_image
            probs = logits_per_image.softmax(dim=1)
        
            # 가장 유사도가 큰 값의 인덱스 구하기
            max_index = torch.argmax(logits_per_image).item()
        
            # 가장 높은 유사도를 가진 지역과 유사도 값
            most_similar_location = original_choices[max_index]  
            similarity = probs[0, max_index].item() * 100  # 유사도 값을 퍼센트로 변환
        
            if similarity <= 75:
                return {
                    "message": (
                        f"입력하신 지역들은 이미지와 유사도가 낮아 정확하게 찾아줄 수는 없습니다."
                        f"\n그러나 이 이미지는 현재 입력하신 지역 중 {most_similar_location}가 가장 유사도가 높습니다."
                        f" 유사도는 {similarity:.2f}%입니다."
                    )
                }
            else:
                return {
                    "message": f"이 이미지는 {most_similar_location}가 가장 유사도가 높습니다. 유사도는 {similarity:.2f}%입니다."
                }
        
        if __name__ == "__main__":
            import uvicorn
            uvicorn.run(app, host="0.0.0.0", port=8000)
        
        ```
        

- 결과 이미지
    
    높은 유사도(75% 초과)가 나온 결과
    
    ![리퀘스트바디.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/b2cee893-6d98-43b2-bd3c-1b3b2ee8287e/%EB%A6%AC%ED%80%98%EC%8A%A4%ED%8A%B8%EB%B0%94%EB%94%94.png)
    
    ![결과1.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/74e248d2-5a03-4a8f-b773-eafcf99a805e/%EA%B2%B0%EA%B3%BC1.png)
    
    낮은 유사도(75% 이하)가 나온 결과
    
    ![결과2.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/1b148433-1958-4a4c-83be-c3340cd8e73d/%EA%B2%B0%EA%B3%BC2.png)
    
- 예외처리
    
    지역 입력이 안된 예)
    
    ![지역 입력안한 에러.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/a4a9f62a-7eba-4ebe-b749-3ed775c8d2d5/%EC%A7%80%EC%97%AD_%EC%9E%85%EB%A0%A5%EC%95%88%ED%95%9C_%EC%97%90%EB%9F%AC.png)
    
    이미지 URL이 입력 안된 예)
    
    ![URL에러.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/9df9de56-4072-4ae8-84e1-ed2fcd25f944/URL%EC%97%90%EB%9F%AC.png)
    

최종 완성

- 최종 완성 html 이미지
    
    ![css넣기.PNG](https://prod-files-secure.s3.us-west-2.amazonaws.com/2e42b292-3597-492a-9d2f-caaf0ff36a48/d5c74922-b741-4e7c-8887-c02b9397ce5c/css%EB%84%A3%EA%B8%B0.png)

<br />


from fastapi import HTTPException
from PIL import Image
import requests
import torch
from transformers import CLIPProcessor, CLIPModel
from googletrans import Translator
from io import BytesIO
from fastapi.responses import HTMLResponse

model = CLIPModel.from_pretrained("geolocal/StreetCLIP")
processor = CLIPProcessor.from_pretrained("geolocal/StreetCLIP")
translator = Translator()

def render_form():
    with open("templates/index.html", "r", encoding="utf-8") as f:
        html_content = f.read()
    return HTMLResponse(content=html_content)


def analyze_image(image_url: str, locations: str):
    try:
        # Load image from URL
        image = Image.open(requests.get(image_url, stream=True).raw)
    except Exception:
        raise ValueError("유효한 이미지 URL을 입력해주세요.")
    
    # 지역 이름 입력받기 및 번역
    choices = []
    original_choices = []
    
    for location in locations.split(","):
        location = location.strip()
        if location:
            translated_location = translator.translate(
                location, src="ko", dest="en"
            ).text
            choices.append(translated_location)
            original_choices.append(location)
    
    if not choices:
        raise ValueError("지역을 입력해주세요.")
    
    # 이미지와 텍스트를 모델에 전달
    inputs = processor(text=choices, images=image, return_tensors="pt", padding=True)
    outputs = model(**inputs)
    logits_per_image = outputs.logits_per_image
    probs = logits_per_image.softmax(dim=1)
    
    # 가장 유사도가 큰 값의 인덱스 구하기
    max_index = torch.argmax(logits_per_image).item()
    most_similar_location = original_choices[max_index]
    similarity = probs[0, max_index].item() * 100
    
    message = ''
    if similarity <= 75:
        message = f"입력하신 지역들은 이미지와 유사도가 낮아 정확하게 찾아줄 수는 없습니다.\n그러나 이 이미지는 현재 입력하신 지역 중 {most_similar_location}가 가장 유사도가 높습니다.\n유사도는 {similarity:.2f}%입니다."
    else:
        message = f"이 이미지는 {most_similar_location}가 가장 유사도가 높습니다. 유사도는 {similarity:.2f}%입니다."
    
    # 결과 메시지 생성
    return message
        
    

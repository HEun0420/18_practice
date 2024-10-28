from pydantic import BaseModel

# 리퀘스트 폼
class AnalyzeRequest(BaseModel):
    image_url: str
    locations: str


class ResponseDTO(BaseModel):
    message: str
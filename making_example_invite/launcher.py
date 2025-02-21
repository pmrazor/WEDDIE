# pip install Pillow requests openai
from PIL import Image, ImageDraw, ImageFont
import requests
from io import BytesIO
from invite import get_background_url
import os

# overlay.jpg 파일 체커, 파일 없다고 할 때 확인해볼 것
# # 현재 작업 디렉토리 출력
# print("현재 작업 디렉토리:", os.getcwd())

# # 현재 디렉토리의 파일 목록 출력
# print("디렉토리 내 파일들:", os.listdir())

# # overlay.jpg 파일이 있는지 구체적으로 확인
# if os.path.exists('overlay.jpg'):
#     print("overlay.jpg 파일이 존재합니다.")
# else:
#     print("overlay.jpg 파일이 존재하지 않습니다.")

def load_image_from_url(url):
    response = requests.get(url)
    return Image.open(BytesIO(response.content))

# 현재 스크립트의 디렉토리 경로를 가져옴
current_dir = os.path.dirname(os.path.abspath(__file__))
overlay_path = os.path.join(current_dir, 'overlay.jpg')

def create_circular_mask(size):
    # 원형 마스크 생성
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size[0], size[1]), fill=255)
    return mask

# URL 파싱
background_image_url = get_background_url()

# 배경 이미지와 상단에 올릴 이미지 열기
background = load_image_from_url(background_image_url)
overlay = Image.open(overlay_path)

# 이미지 크기 조정
overlay = overlay.resize((350, 350))

# RGBA 모드로 변환
if overlay.mode != 'RGBA':
    overlay = overlay.convert('RGBA')

# 원형 마스크 생성 및 적용
mask = create_circular_mask(overlay.size)
circular_overlay = Image.new('RGBA', overlay.size, (0,0,0,0))
circular_overlay.paste(overlay, (0,0), mask)

# 그림자용 검은색 원형 생성
shadow = Image.new('RGBA', overlay.size, (0,0,0,0))  # 먼저 완전 투명으로 생성
shadow_draw = ImageDraw.Draw(shadow)
shadow_draw.ellipse((0, 0, overlay.size[0], overlay.size[1]), fill=(0,0,0,51))  # RGB 검정색, alpha 51

# 이미지를 합성할 위치 계산
position = (
    (background.width - overlay.width) // 2,
    (background.height - overlay.height) // 5
)

# 그림자 위치 계산 (약간 오른쪽 아래로)
shadow_position = (
    position[0] + 7,  # 10픽셀 오른쪽으로
    position[1] + 7   # 10픽셀 아래로
)

# 새로운 이미지 생성
new_image = Image.new('RGBA', background.size)

# 배경 이미지 붙이기
new_image.paste(background, (0, 0))

# 배경 이미지가 RGB 모드인 경우 RGBA로 변환
if background.mode != 'RGBA':
    background = background.convert('RGBA')

# 그림자 먼저 붙이기
new_image.paste(shadow, shadow_position, shadow)

# 원형 이미지 붙이기
new_image.paste(circular_overlay, position, circular_overlay)
# 텍스트 추가
draw = ImageDraw.Draw(new_image)
try:
    # 폰트 파일 경로 지정 (시스템에 설치된 폰트 사용)
    font = ImageFont.truetype("malgun.ttf", 40)  # Windows의 경우 malgun.ttf (맑은 고딕)
except:
    font = ImageFont.load_default()  # 기본 폰트 사용 (폰트 파일이 없는 경우)

# 텍스트 내용
text = "여기에 텍스트를 입력"

# 텍스트 크기 계산
bbox = draw.textbbox((0, 0), text, font=font)
text_width = bbox[2] - bbox[0]
text_height = bbox[3] - bbox[1]

# 텍스트 위치 계산 (원형 이미지 아래 중앙)
text_position = (
    (background.width - text_width) // 2,  # 가로 중앙
    position[1] + overlay.height + 200  # 원형 이미지 아래 20픽셀 간격
)

# 텍스트 그리기
draw.text(
    text_position,
    text,
    font=font,
    fill=(255, 255, 255),  # 흰색 텍스트
    stroke_width=1,  # 텍스트 테두리 두께
    stroke_fill=(0, 0, 0)  # 검은색 테두리
)

# 결과 저장
new_image.save('result.png')
new_image.show()
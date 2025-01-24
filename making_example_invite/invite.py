import openai

def get_background_url():
    client = openai.OpenAI(api_key = "sk-YOUR_OPENAI_API_KEY_HERE")
    
    response = client.images.generate(
        model="dall-e-3",
        prompt="Create an elegant wedding invitation design with a pure white background. Add delicate floral arrangements in the corners and edges of the frame, featuring soft pastel roses, peonies, and small delicate flowers with green leaves. The floral elements should create a romantic and decorative border that gracefully frames the invitation, while maintaining a clean and sophisticated look. The flowers should have a watercolor-like appearance with subtle shading and natural, organic shapes. Ensure the center area remains clear for text placement. The overall style should be elegant and timeless with a modern touch.",
        size="1024x1024",
        quality="standard",
        n=1,
    )
    
    return response.data[0].url

if __name__ == "__main__":
    # 단독으로 실행할 때 테스트용
    url = get_background_url()
    print(url)
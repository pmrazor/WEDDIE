// 색상 정의 (예제 색상)
export const colors = {
  WHITE: "#FFF",
  BLACK: "#000",
  
  // 기본 색상
  PRIMARY: "", 
  PRIMARY_HOVER: "",
  PRIMARY_ACTIVED: "",

  SECONDARY: "",
  SECONDARY_HOVER: "",
  SECONDARY_ACTIVED: "",

  DELETE: "",
  DELETE_HOVER: "",
  DELETE_ACTIVED: "",

  // Grayscale
  GRAY_50: "",
  GRAY_100: "",
  GRAY_200: "",
  GRAY_300: "",
  GRAY_400: "",
  GRAY_500: "",
  GRAY_600: "",
  GRAY_700: "",
  GRAY_800: "",
  GRAY_900: "",
};

// CSS 변수로 색상을 주입하는 함수
export const injectColors = () => {
  const root = document.documentElement;

  Object.entries(colors).forEach(([key, value]) => {
    if (value) {
      root.style.setProperty(`--${key}`, value);
    }
  });
};

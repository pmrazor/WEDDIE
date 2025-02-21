// 색상 정의 (예제 색상)
export const colors = {
  WHITE: "#FFFFFF",
  BLACK: "#3C3C3C",
  // BLACK_LIGHT: "#3C3C3C",
  // BLACK_EXTRALIGHT: "#3C3C3C",

  BEIGE : "#FFEACC",

  YELLOW : "#FFE47A",
  YELLOW_DEEP : "#FBBC05",

  // 기본 성별 색상
  genderColors : {
    Default : {
      PRIMARY_EXTRADEEP: "#7C6FC1",
      PRIMARY_DEEP: "#8E83CA",
      PRIMARY: "#B3ACDB", 
      PRIMARY_LIGHT: "#D8D5EC",
      PRIMARY_EXTRALIGHT: "#EBE9F5",  
    },
    Male : {
      PRIMARY_EXTRADEEP: "#58AAFF",
      PRIMARY_DEEP: "#7EBDFF",
      PRIMARY: "#B3D8FF", 
      PRIMARY_LIGHT: "#D8EBFF",
      PRIMARY_EXTRALIGHT: "#EBF4FF",
    }, 
    Female : {
      PRIMARY_EXTRADEEP: "#FF758F",
      PRIMARY_DEEP: "#FF94A8",
      PRIMARY: "#FFC0CC", 
      PRIMARY_LIGHT: "#FFDFE5",
      PRIMARY_EXTRALIGHT: "#FFEEF1",
    }, 
  }, 

  DELETE: "#FF0000", // 예시로 빨간색 값 넣기
  DELETE_HOVER: "#FF6666", // 예시로 밝은 빨간색 값 넣기
  DELETE_ACTIVED: "#CC0000", // 예시로 활성화된 빨간색 값 넣기

  // Grayscale을 실제 값으로 채우기
  GRAY_50: "#F7F7F7", // 예시로 밝은 회색 넣기
  GRAY_100: "#E1E1E1",
  GRAY_200: "#C4C4C4",
  GRAY_300: "#A8A8A8",
  GRAY_400: "#8B8B8B",
  GRAY_500: "#6F6F6F",
  GRAY_600: "#535353",
  GRAY_700: "#373737",
  GRAY_800: "#1C1C1C",
  GRAY_900: "#000000",

};



// CSS 변수로 색상을 주입하는 함수
export const injectColors = () => {
  const root = document.documentElement;

  const setColors = (prefix, obj) => {
    Object.entries(obj).forEach(([key, value]) => {
      if (typeof value === "object") {
        setColors(`${prefix}-${key}`, value);
      } else {
        root.style.setProperty(`--${prefix}-${key}`, value);
      }
    });
  };

  setColors("", colors);
};

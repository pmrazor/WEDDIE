// 색상 정의 (예제 색상)
export const colors = {
  WHITE: "#FFFFFF",
  BLACK: "##3C3C3C",
  BLACK_LIGHT: "##3C3C3C",
  BLACK_EXTRALIGHT: "##3C3C3C",

  // 기본 색상
  PRIMARY_EXTRADEEP: "#7C6FC1",
  PRIMARY_DEEP: "#8E83CA",
  PRIMARY: "#B3ACDB", 
  PRIMARY_LIGHT: "#D8D5EC",
  PRIMARY_EXTRALIGHT: "#EBE9F5",
 
  M_PRIMARY_EXTRADEEP: "#58AAFF",
  M_PRIMARY_DEEP: "#7EBDFF",
  M_PRIMARY: "#B3D8FF", 
  M_PRIMARY_LIGHT: "#D8EBFF",
  M_PRIMARY_EXTRALIGHT: "#EBF4FF",

  W_PRIMARY_EXTRADEEP: "#FF758F",
  W_PRIMARY_DEEP: "#FF94A8",
  W_PRIMARY: "#FFC0CC", 
  W_PRIMARY_LIGHT: "#FFDFE5",
  W_PRIMARY_EXTRALIGHT: "#FFEEF1",


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

// 전역 상태 관리가 필요해서 context 폴더 생성, 배치함 
import { colors } from "@/constants/colors";
import { createContext, useContext, useEffect, useState } from "react";

const ThemeContext = createContext() 

export const ThemeProvider = ({ children }) => {
  const [gender, setGender] = useState("groom") // 기본값 : 신랑("groom"), 신부("bride")로 변경 가능 
  
  const theme = {
    PRIMARY : gender === "groom" ? colors.M_PRIMARY : colors.W_PRIMARY , 
    PRIMARY_HOVER: gender === "groom" ? colors.M_PRIMARY_HOVER : colors.W_PRIMARY_HOVER,
    PRIMARY_ACTIVED: gender === "groom" ? colors.M_PRIMARY_ACTIVED : colors.W_PRIMARY_ACTIVED,
    PRIMARY_DEACTIVED: gender === "groom" ? colors.M_PRIMARY_DEACTIVED : colors.W_PRIMARY_DEACTIVED,
    PRIMARY_BACKGROUND: gender === "groom" ? colors.M_PRIMARY_BACKGROUND : colors.W_PRIMARY_BACKGROUND,
  }
  
  useEffect(() => {
    const root = document.documentElement
    root.style.setProperty('--PRIMARY', theme.PRIMARY)
    console.log("[from theme.jsx] theme.PRIMARY color is : ", theme.PRIMARY)
  }, [theme])

  return (
    <ThemeContext.Provider value={{theme, setGender}}>
      {children}
    </ThemeContext.Provider>
  )
}

export const useTheme = () => useContext(ThemeContext)
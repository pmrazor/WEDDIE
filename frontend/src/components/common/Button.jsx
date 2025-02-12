import useAuthStore from "@/store/auth"
import "@/style/common/Button.css"
import { style } from "framer-motion/client"

// 이후에 전역변수로 import할 gender -> gender에 따라 변할 component에다 두면 돼  
const Button = ({ text, onClick, stickyType, style, addClass }) => {
  
  // console.log(gender+type)
  return (
    <button 
      onClick={onClick}
      className={`Button sticky-${stickyType} ${addClass}`}
      style={style}
    >
      {text}
    </button>
  )
}


export default Button
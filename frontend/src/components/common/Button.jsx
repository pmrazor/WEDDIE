import "@/style/common/Button.css"

// 이후에 전역변수로 import할 gender -> gender에 따라 변할 component에다 두면 돼  
const gender = "W_" // ("", "M_" / "W-")  
const Button = ({ text, type, onClick, stickyType }) => {

  console.log(gender+type)
  return (
    <button 
      onClick={onClick}
      className={`Button ${gender}${type} sticky-${stickyType}`}
    >
      {text}
    </button>
  )
}


export default Button
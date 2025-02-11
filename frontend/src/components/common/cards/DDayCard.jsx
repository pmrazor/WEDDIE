import { colors } from "@/constants/colors"
import "@/style/component/DDayCard.css"

const gender = "" // ("", "M_" / "W-")  
const DDayCard = ({ onClick, head_text, sub_text, style, headTextStyle }) => {


  return (
    <div className="DDayCard" style={style}
      onClick={onClick}
    >
      <h3 className="sub_text">
        {sub_text}
      </h3>
      <h1 className="head_text" style={headTextStyle}>
        {head_text} 
      </h1>
    </div>
  )
}


export default DDayCard
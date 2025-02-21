import { colors } from "@/constants/colors"
import "@/style/component/DDayCard.css"

const gender = "" // ("", "M_" / "W-")  
const DDayCard = ({ onClick, headText, subText, style, headTextStyle }) => {


  return (
    <div className="DDayCard" style={style}
      onClick={onClick}
    >
      <h3 className="sub_text">
        {subText}
      </h3>
      <h1 className="head_text" style={headTextStyle}>
        {headText} 
      </h1>
    </div>
  )
}


export default DDayCard
import { colors } from "@/constants/colors"
import "../style/component/DDayCard.css"

const DDayCard = ({ onClick, head_text, sub_text, style }) => {

  return (
    <div className="DDayCard" style={style}
      onClick={onClick}
    >
      <h1 className="head_text">
        {head_text} 
      </h1>
      <h3 className="sub_text">
        {sub_text}
      </h3>
    </div>
  )
}


export default DDayCard
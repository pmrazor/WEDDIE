import "@/style/component/VirginRoadCard.css"
import Button from "../Button"
import { useNavigate } from "react-router-dom"
// import status1 from "@/assets/VirginRoadCard/status1.svg"
// import status2 from "@/assets/VirginRoadCard/status2.svg"
// import status3 from "@/assets/VirginRoadCard/status3.svg"
// import status4 from "@/assets/VirginRoadCard/status4.svg"
import man1 from "@/assets/VirginRoadCard/man1.svg"
import man2 from "@/assets/VirginRoadCard/man2.svg"
import man3 from "@/assets/VirginRoadCard/man3.svg"
import man4 from "@/assets/VirginRoadCard/man4.svg"
import battery1 from "@/assets/VirginRoadCard/battery1.svg"
import battery2 from "@/assets/VirginRoadCard/battery2.svg"
import battery3 from "@/assets/VirginRoadCard/battery3.svg"
import battery4 from "@/assets/VirginRoadCard/battery4.svg"
import { colors } from "@/constants/colors"

const gender = "Male"

const VirginRoadCard = ({step, planStatus, visible, isEditing, toggleActivated }) => {

  const iconForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START" : return battery1
      case "IN_PROGRESS" : return battery2
      case "NOT_TIME_CAPSULE" : return battery3
      case "FINISHED" : return battery4
    }
  }
  const backgroundForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START" : return colors.genderColors[gender].PRIMARY
      case "IN_PROGRESS" : return colors.genderColors[gender].PRIMARY_EXTRADEEP
      case "NOT_TIME_CAPSULE" : return colors.genderColors[gender].PRIMARY_DEEP
      case "FINISHED" : return colors.genderColors[gender].PRIMARY
    }
  }

  const nav = useNavigate()
  const onClickToGo = () => {
    const planId = 1 // 임시로 1 지정 
    nav(`/virginroad/${planId}`)
  }
  
  return (
    <div 
      className={`VirginRoadCard ${isEditing ? "shake" : ""}`}
      style={
        visible !== true
        ? {backgroundColor : colors.GRAY_100} 
        : planStatus === "FINISHED" 
          ? {border : `3px solid ${backgroundForStatus()}`, backgroundColor: backgroundForStatus()}
          : {border : `3px solid ${backgroundForStatus()}`}}
    >
      <div className="icon-area">
        <img src={iconForStatus()} alt="icon"/>
      </div>
      <div className="text-area">
        {step}
      </div>
      <div className="to-go-button">
        {isEditing ? (
          <Button 
            onClick={toggleActivated}
            text={"-"}
            addClass={"hide-button"}
            style={{backgroundColor : colors.WHITE, color : colors.DELETE}}
          /> 
        ) : (
          <Button 
            onClick={onClickToGo}
            text={">"}
            addClass={"to-go-button"}
            style={{backgroundColor : colors.WHITE , color : colors.genderColors[gender].PRIMARY_DEEP}}
          />
        )}
      </div>
    </div>
  )
}

export default VirginRoadCard
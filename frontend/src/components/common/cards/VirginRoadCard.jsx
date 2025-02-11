import "@/style/component/VirginRoadCard.css"
import Button from "../Button"
import { useNavigate } from "react-router-dom"
import status1 from "@/assets/VirginRoadCard/status1.svg"
import status2 from "@/assets/VirginRoadCard/status2.svg"
import status3 from "@/assets/VirginRoadCard/status3.svg"
import status4 from "@/assets/VirginRoadCard/status4.svg"
import { colors } from "@/constants/colors"

const gender = "Male"

const VirginRoadCard = ({step, planStatus, activated}) => {
  
  const iconForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START" : return status1
      case "IN_PROGRESS" : return status2
      case "NOT_TIME_CAPSULE" : return status3
      case "FINISHED" : return status4
    }
  }
  const backgroundForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START" : return colors.genderColors[gender].PRIMARY
      case "IN_PROGRESS" : return colors.genderColors[gender].PRIMARY_EXTRADEEP
      case "NOT_TIME_CAPSULE" : return colors.genderColors[gender].PRIMARY_DEEP
      case "FINISHED" : return colors.genderColors[gender].PRIMARY_EXTRALIGHT
    }
  }

  const nav = useNavigate()
  const onClickToGo = () => {
    const planId = 1 // 임시로 1 지정 
    nav(`/virginroad/${planId}`)
  }
  
  return (
    <div className="VirginRoadCard" style={{backgroundColor : backgroundForStatus()}}>
      <div className="icon-area">
        <img src={iconForStatus()} alt="icon"/>
      </div>
      <div className="text-area">
        {step}
      </div>
      <div className="to-go-button">
        <Button 
          onClick={onClickToGo}
          text={">"}
        />
      </div>
    </div>
  )
}

export default VirginRoadCard
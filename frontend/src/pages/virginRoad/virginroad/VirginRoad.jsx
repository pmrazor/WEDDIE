import VirginRoadList from "@/components/VirginRoadList"
import "@/style/page/VirginRoad.css"
import Button from "@/components/common/Button"
import { useNavigate } from "react-router-dom"


const VirginRoad = () => {
  const nav = useNavigate()

  const onClickBack = () => {
    nav(-1)
  }

  return (
    <div className="VirginRoad">
      <div className="NavBar">
        <Button 
          onClick={onClickBack}
          text={"<"}
        />
        <div className="text">
          우리의 버진 로드
        </div>
      </div>
      <div className="VirginRoadItem">
        <VirginRoadList />         
      </div>
    </div>
  )
}

export default VirginRoad
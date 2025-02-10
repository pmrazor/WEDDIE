import "../style/VirginRoadList.css"
import VirginRoadCard from "./common/cards/VirginRoadCard"

// const planStatus = "FINISHED" 
// BEFORE_START , IN_PROGRESS , NOT_TIME_CAPSULE, FINISHED  
const VirginRoadList = () => {

  return (
    <div className="VirginRoadList">
      <VirginRoadCard 
        step={"웨딩홀"}
        planStatus={"FINISHED"}
        activated={"TRUE"}
      />
      {/* <VirginRoadCard 
        step={"신혼 여행"}
        planStatus={"FINISHED"}
        activated={"TRUE"}
      /> */}
      <VirginRoadCard 
        step={"스튜디오"}
        planStatus={"NOT_TIME_CAPSULE"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"드레스"}
        planStatus={"IN_PROGRESS"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"메이크업 스튜디오"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"한복"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"남성 예복"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"스냅 촬영"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"청첩장 만들기"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"피부케어 및 관리"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
      <VirginRoadCard 
        step={"허니문 준비"}
        planStatus={"BEFORE_START"}
        activated={"TRUE"}
      />
    </div>
  )
}

export default VirginRoadList
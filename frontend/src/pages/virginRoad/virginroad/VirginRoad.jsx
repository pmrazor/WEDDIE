import VirginRoadList from "@/components/VirginRoadList"
import "@/style/page/VirginRoad.css"
import Button from "@/components/common/Button"
import { useNavigate } from "react-router-dom"
import AdvertisementCard from "@/components/common/cards/AdvertisementCard"
import { colors } from "@/constants/colors"
import { useState } from "react"
import dummy06 from "/Advertisement/dummy06.jpg"
import useAuthStore from "@/store/auth"
import battery1 from "@/assets/VirginRoadCard/battery1.svg"
import battery2 from "@/assets/VirginRoadCard/battery2.svg"
import battery3 from "@/assets/VirginRoadCard/battery3.svg"
import battery4 from "@/assets/VirginRoadCard/battery4.svg"
import useVirginRoadStore from "@/store/virginroad"

// const gender = useAuthStore(userInfo.gender)
const gender = "Male"

// 더미 데이터 (mockData)
const mockData = [
  { step: "웨딩홀", planStatus: "FINISHED", activated: "TRUE" },
  { step: "스튜디오", planStatus: "NOT_TIME_CAPSULE", activated: "TRUE" },
  { step: "드레스", planStatus: "IN_PROGRESS", activated: "FALSE" },
  { step: "메이크업 스튜디오", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "한복", planStatus: "IN_PROGRESS", activated: "TRUE" },
  { step: "남성 예복", planStatus: "BEFORE_START", activated: "FALSE" },
  { step: "스냅 촬영", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "청첩장 만들기", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "피부케어 및 관리", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "허니문 준비", planStatus: "BEFORE_START", activated: "TRUE" }
];

const VirginRoad = () => {
  const nav = useNavigate()

  const onClickBack = () => {
    nav(-1)
  }

  const [isOpen, setIsOpen] = useState(false)

  const onClickToggle = () => {
    setIsOpen((prev) => !prev)
  }

  const [isEditing, setIsEditing] = useState(false)
  console.log("now isEditing : ", isEditing)

  const toggleEditing = () => {
    setIsEditing((prev) => !prev) // true -> false , false -> true 
  }
  
  const { plans } = useVirginRoadStore()

  // 현재 진행중인 아이템을 advertisement 카드에서 소개하려고 
  const [VirginRoadData, setVirginRoadData] = useState(plans)

  // isEditing 이 true이면 모든 데이터를 보여주기 , false일 경우 activated가 "TRUE"인 데이터만 보여주기 
  const visibleData = isEditing ? VirginRoadData : VirginRoadData.filter(item => item.visible === true)

  const inProgressItem = VirginRoadData.find(item => item.planStatus === "IN_PROGRESS")

  const toggleActivated = (index) => {
    setVirginRoadData((prevData) =>
      prevData.map((item, i) =>
        i === index ? { ...item, visible: item.visible === true ? false : true } : item
      )
    );
  };


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
        <div className="edit-button">
          <Button 
            onClick={toggleEditing} 
            text={isEditing ? "완료" : "편집"}
            // text={"편집"}
          /> 
        </div>
      </div>
      <div className="recommand-area">
        <AdvertisementCard 
          headText={inProgressItem ? `현재 진행 중인 로드 : ${inProgressItem.step}` : "현재 진행 중인 로드가 없습니다."}
          subText1={inProgressItem ? "진척도 : 75%, " : "지금 바로 버진 로드를 시작해봐요!"}
          subText2={inProgressItem ? "계속해서 서로를 향한 사랑으로 버진 로드를 만들어가요" : ""}
          style={{backgroundImage : `url(${dummy06})`}}
        />
      </div>
      <div className="tutorial-notice" style={{backgroundColor : colors.genderColors[gender].PRIMARY}}>
      <Button 
          onClick={onClickToggle}
          // style={{backgroundColor : colors.genderColors[gender].PRIMARY}}
          text={"튜토리얼"}
        /> 
        
      </div>
      { isOpen && (
        <div className="tutorial">

          <div className="tutorial-container">
            <img src={battery1} alt="before-start" />
            <div className="text-container">
              아직 진행하지 않은 버진 로드를 의미합니다. 지금 시작해보세요!
            </div>
          </div>
          <div className="tutorial-container">
            <img src={battery2} alt="before-start" />
            <div className="text-container">
              지금 진행 중인 버진 로드를 의미합니다. 
            </div>
          </div>
          <div className="tutorial-container">
            <img src={battery3} alt="before-start" />
            <div className="text-container">
              마쳤으나 아직 타임 캡슐을 작성하지 않은 버진 로드를 의미합니다. <br />시간을 내어 소중한 추억을 타임 캡슐에 기록해주세요
            </div>
          </div>
          <div className="tutorial-container">
            <img src={battery4} alt="before-start" />
            <div className="text-container">
              타임 캡슐까지 작성 완료한 버진 로드를 의미합니다. <br />수고하셨어요! 다음 로드가 기대돼요!
            </div>
          </div>
        </div>
      )}
      <div className="tutorial-button" style={{backgroundColor : colors.genderColors[gender].PRIMARY}}>
        <Button 
          onClick={onClickToggle}
          // style={{backgroundColor : colors.genderColors[gender].PRIMARY}}
          text={isOpen ? "튜토리얼 접기" : "자세히 보기" }
        /> 
       </div>
      <div className="VirginRoadItem">
        <VirginRoadList 
          // mockData={visibleData}
          isEditing={isEditing}
          toggleActivated={toggleActivated}
          plans={visibleData}
          />         
      </div>
    </div>
  )
}

export default VirginRoad
import Button from "@/components/common/Button"
import "@/style/page/VirginRoadDetail.css"
import { useNavigate } from "react-router-dom"
import notice from "@/assets/VirginRoadDetail/notice.png"
import more from "@/assets/VirginRoadDetail/more.svg"
import NoticeCard from "@/components/common/cards/NoticeCard"
import useAuthStore from "@/store/auth"
import CheckListItem from "@/components/CheckListItem"
import { colors } from "@/constants/colors"
import { Radius } from "lucide-react"

const VirginRoadDetail = () => {

  const nav = useNavigate() 
  
  const onclickBack = () => {
    nav("/virginroad")
  }
  const onclickReservation = () => {
    nav("/reservation")
  }
  const onClickMore = () => {
    // 제어 화면이 떠야 한다. 
    // 1. 이 항목을 visible하게 할지 선택하기, 2. 예산 조정하기, 3. 버진로드 종료하기, 4. 이 로드는 패스하기
  }
  const nowNode = "웨딩홀 예약" // api에서 가져와야겠지? 

  // const gender = useAuthStore((state) => state.gender)
  const gender = "Default"
  const genderBack = () => {
    switch (gender) {
      case "Male" : return "male" 
      case "Female" : return "female"  
      default : return "default" 
    }
  }

  // budget 예시 데이터 
  const ages = "30대 초반"
  const averageBudget = 750000
  const budgetPercentage = "15%"

  // checklist mokData - 백엔드에서 받아올 것
  const checkMokData = [
    {id : 1, content : "체크리스트1", isFinished : "False"}, 
    {id : 2, content : "체크리스트2", isFinished : "False"}, 
    {id : 3, content : "체크리스트3", isFinished : "False"}, 
    {id : 4, content : "체크리스트4", isFinished : "False"}, 
  ]

  return (
    <div className="VirginRoadDetail">
      <div className="header">
        <Button 
          onClick={onclickBack}
          text={"<"}
        />
        <div className="text">
          {/* 이 로드의 이름 들어가기 */}
          {`${nowNode}하기`} 
        </div>
        {/* <Button 
          onClick={onClickMore}
          text={"..."}
        /> */}
        <div className="hamburger">
          <img src={more} alt="more" onClick={onClickMore}/>
        </div>
      </div>
      {/* <div className="budget" style={{backgroundImage: `url(${w_budget_background})`}}> */}
      <div className={`budget ${genderBack()}`}>
        <div className="text-area" style={{color : colors.genderColors[gender].PRIMARY_EXTRADEEP}}>
          <div className="head-text1">{`우리와 같은 ${ages} 부부들은`}</div>
          <div className="head-text2">{`${nowNode}에`}</div>
          <div className="head-text3">{`평균 ${averageBudget}원,`}</div>
          <div className="head-text4">{`전체 예산의 ${budgetPercentage}를 썼어요.`}</div>
        </div>
      </div>
      <div className="tips">
        <NoticeCard
          icon={notice}
          headText={"그거 아세요?"}
          subText1={`${nowNode} 로드를 걷는 ${gender === "Male" ? "예랑이" : gender === "Female" ? "예신이" : "부부"}라면`}
          subText2={`놓치지 말아야 할 꿀팁을 전해드려요 `}
          backgroundColor={colors.genderColors[gender].PRIMARY_DEEP}
        />
      </div>
      <div className="checklist-area" style={{backgroundColor : colors.genderColors[gender].PRIMARY_LIGHT}}>
        <div className="checklist-header">
          <h3>체크리스트</h3>
          <button>편집</button>
          <button>추가</button>
        </div>
        <div className="checklist">
          {checkMokData.map((item) => (
            <CheckListItem
            key={item.id}
            content={item.content}
            isFinished={item.isFinished}
            /> 
          ))}
        </div>
      </div>
      <Button 
        onClick={onclickReservation}
        style={{
          backgroundColor : colors.genderColors[gender].PRIMARY_DEEP, 
          borderRadius : "10px"
        }}
        text={`${nowNode}하러 가기`}
        stickyType={"UNDER"}
      />
    </div>
  )
}

export default VirginRoadDetail 
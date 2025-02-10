// 버진로드 작성하라는 트리거 페이지 
import "../style/BeforeOnBoarding.css"
import useAuthStore from "@/store/auth"
import { useNavigate } from "react-router-dom"
import Card from "@/components/common/cards/IconCard"
import Button from "@/components/common/Button"
import { colors } from "@/constants/colors"
import { useTheme } from "@/contexts/theme"
import man from "@/assets/BeforeOnBoarding/man.svg"
import heartwithribbon from "@/assets/BeforeOnBoarding/heartwithribbon.svg"
import flowerbouquet from "@/assets/BeforeOnBoarding/flower-bouquet.png"

const BeforeOnBoarding = () => {

  const { isAuthenticated } = useAuthStore() 
  const nav = useNavigate()

  const onClickButton = () => {
    switch (isAuthenticated) {
      case false : return nav("/login") 
      case true : return nav("/virginroad")
    }
  }

  const { theme } = useTheme() 
  console.log("[from BeforeOnBoarding.jsx] theme.PRIMARY : ", theme.PRIMARY)


  return (
    <div className="BeforeOnBoarding">
      {/* BeforeOnBoarding */}
      {/* <div>
        {!isAuthenticated && <button onClick={() => nav("/login")}>login</button>} <br />
        {!isAuthenticated && <button onClick={() => nav("/register")}>Register</button>} <br />
        <button onClick={() => nav("/onboarding")}>onBoarding</button>
      </div>   */}
      <div>
        <Button 
          onClick={onClickButton}
          type={"PRIMARY_DEEP"} 
          stickyType={"UNDER"}  
          text={"우리 부부만의 버진 로드 시작하기"} 
        />
      </div>
      <div className="card-container">
        <Card 
          type={"OnBoarding"}
          head_text1={"결혼을 결심하셨나요?"}
          head_text2={"정말 잘 하셨어요!"}
          sub_text1={"우리 부부만의 AI 비서가"}
          sub_text2={"결혼과 이후 과정까지 책임질게요"}
          icon={<img src= {man} alt="man icon" />}
          style={{backgroundColor: colors.M_PRIMARY_EXTRALIGHT}}
          />  
        <Card 
          type={"OnBoarding"} 
          head_text1={"같이 걸어볼까요?"}
          head_text2={"우리 부부만의 버진 로드"}
          sub_text1={"AI 비서가 우리 부부의 상황을 분석해"}
          sub_text2={"최적의 로드를 설계해 드릴게요"}
          icon={<img src={flowerbouquet} alt="flower-bouquet icon" />}
          style={{backgroundColor: colors.W_PRIMARY_EXTRALIGHT}}
          />  
        <Card 
          type={"OnBoarding"}
          head_text1={"불편한 부담은 덜고"}
          head_text2={"서로를 조금 더 생각해요"}
          sub_text1={"귀찮고 어려운 건 Weddie가 도와 드릴게요"}
          sub_text2={"아름다운 버진 로드, 서로 사랑만 하며 만들어가요"}
          icon={<img src={heartwithribbon} alt="heartwithribbon icon" />}
          style={{backgroundColor: colors.PRIMARY_EXTRALIGHT}}
          />  
      </div>
      {/* <div style={{paddingBlockEnd:"15vh"}}></div> */}
    </div>
  )
}

export default BeforeOnBoarding
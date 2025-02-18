import DDayCard from "@/components/common/cards/DDayCard"
import VirginRoadCard from "@/components/common/cards/VirginRoadCard"
import "@/style/page/Main.css"
import { useEffect, useRef, useState } from "react"
import { useNavigate } from "react-router-dom"
import AdvertisementCard from "@/components/common/cards/AdvertisementCard"
import lv1 from "@/assets/main-cy-image/lv1.png"
import lv11 from "@/assets/main-cy-image/lv11.png"
import page1 from "@/assets/main-cy-image/1.png"
import { colors } from "@/constants/colors"
import MiniHome from "./MiniHome"
import before from "@/assets/main-cy-image/before.png"
import after from "@/assets/main-cy-image/after.png"
import backmale from "@/assets/main/back_male.png"
import backfemale from "@/assets/main/back_female.png"

const mockData = {
  "isSuccess":true,
  "code":"COMMON200",
  "message":"성공입니다.",
  "result":{
    "groupedPlans":{
      "BEFORE_START":[
        {"planId":1, "shopName":"웨딩홀", "serviceType":"WEDDING_HALL", "tip":{"tipId":1, "tipContent":"결혼 준비는 일찍 시작하세요!"}},
        {"planId":2, "shopName":"드레스샵", "serviceType":"DRESS_SHOP", "tip":{"tipId":2, "tipContent":"드레스 선택은 충분히 시간을 두고 하세요."}},
        {"planId":3, "shopName":"스튜디오", "serviceType":"STUDIO", "tip":{"tipId":3, "tipContent":"스튜디오 예약은 미리 하세요!"}}
      ],
      "IN_PROGRESS":[
        {"planId":4, "shopName":"메이크업 스튜디오", "serviceType":"MAKEUP_STUDIO", "tip":{"tipId":4, "tipContent":"메이크업 예약은 서두르세요!"}},
        {"planId":5, "shopName":"한복", "serviceType":"HANBOK", "tip":{"tipId":5, "tipContent":"한복은 결혼 2주 전에 준비하세요!"}}
      ],
      "NOT_TIME_CAPSULE":[
        {"planId":6, "shopName":"스냅 촬영", "serviceType":"SNAP", "tip":{"tipId":6, "tipContent":"스냅 촬영 예약은 여유 있게 하세요!"}},
        {"planId":7, "shopName":"청첩장", "serviceType":"INVITATION", "tip":{"tipId":7, "tipContent":"청첩장 디자인을 고를 때 충분한 시간을 두세요."}}
      ],
      "FINISHED":[
        {"planId":8, "shopName":"신혼여행", "serviceType":"HONEYMOON", "tip":{"tipId":8, "tipContent":"신혼여행지는 미리 계획하세요!"}},
        {"planId":9, "shopName":"남성 예복", "serviceType":"TAILOR_SHOP", "tip":{"tipId":9, "tipContent":"남성 예복을 미리 맞추세요!"}}
      ]
    }
  }
}

const getRandomMessage = () => {
  const messages = [
    "우리의 새로운 시작이 다가오고 있어요!",
    "설렘 가득한 하루하루, 함께 준비해요!",
    "기다림도 특별한 추억이 될 거예요!",
    "하나씩 준비해 가는 과정도 소중한 순간이 될 거예요!",
    "함께할 날이 가까워질수록 마음이 더 두근거려요!",
    "우리의 사랑이 더 빛날 날을 기대해요!",
    "오늘도 한 걸음 더 가까이, 행복한 결혼식이 기다리고 있어요!",
    "설렘과 기쁨이 가득한 날들을 함께 채워가요!",
    "완벽한 순간을 위해 함께하는 모든 순간이 소중해요!",
    "우리 둘만의 특별한 이야기가 시작되고 있어요!",
    "사랑과 행복으로 가득 찬 하루하루를 만들어가요!",
    "조금씩 준비해 가며 우리만의 색깔을 채워가요!",
    "소중한 사람들과 함께할 행복한 날이 머지않았어요!",
    "결혼식 준비도 우리 둘만의 추억이 될 거예요!",
    "설렘과 기대 속에서 하나씩 완성해 가는 중이에요!",
    "우리의 시작이 더 아름다워질 수 있도록, 오늘도 함께해요!",
    "신부와 신랑으로서의 우리의 새로운 이야기가 시작돼요!",
    "평생 함께할 우리의 첫걸음, 설렘과 행복으로 가득 채워가요!",
    "사랑이 깊어지는 이 시간, 더 소중하게 기억할 거예요!",
    "우리의 약속이 현실이 되어가는 순간, 기대되지 않나요?"
  ];
  return messages[Math.floor(Math.random() * messages.length)];
};


const weddingDay = new Date("2025-06-13")
const startDay = new Date("2023-11-01")

const fromTodayDDay = (targetDate) => {
  const today = new Date() // 오늘 기준으로 targetDate가 얼마나 남았는지 
  today.setHours(0, 0, 0, 0)
  const diffTime = targetDate.getTime() - today.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

const gender = "Female"

const Main = () => {

  const nav = useNavigate();
  const [untilWedding, setUntilWedding] = useState(fromTodayDDay(weddingDay));
  const [randomMessage, setRandomMessage] = useState(getRandomMessage());

  useEffect(() => {
    const updateDDay = () => {
      setUntilWedding(fromTodayDDay(weddingDay));
      setRandomMessage(getRandomMessage());
    };

    updateDDay();
    const interval = setInterval(updateDDay, 24 * 60 * 60 * 1000);
    return () => clearInterval(interval);
  }, []);

  const level = 11;

  const { groupedPlans } = mockData.result 
  const nowTodo = groupedPlans["BEFORE_START"] || []
  const furtherTodo = groupedPlans["IN_PROGRESS"] || []
  const incomplete = groupedPlans["NOT_TIME_CAPSULE"] || []

  const todoRef = useRef(null) 
  const scrollToTodo = () => {
    if (todoRef.current) {
      todoRef.current.scrollIntoView({ behavior: "smooth" }) // 부드러운 스크롤
    }
  }

  const [isLoaded, setIsLoaded] = useState(false)

  useEffect(() => {
    setTimeout(() => setIsLoaded(true), 500)
  }, [])

  return (
    <div className="Main" >
      <div className="first">
        <div className="d-day">
          <AdvertisementCard 
            headText1={`D-${untilWedding}`}
            subText1={`우리의 사랑이 결실을 맺을 날, 얼마 남지 않았어요`}
            subText3={randomMessage}
            style={gender === "Male" ? {backgroundImage : `url(${backmale})`} : {backgroundImage : `url(${backfemale})`}}
            />
          {/* <DDayCard 
            headText={D-${untilWedding}}
            subText={"결혼식까지"}
            /> */}
        </div>
        <div className="cy-image">
          {level === 1 ? <img src={before} alt="" /> : <img src={after} alt="" /> }
          {/* <MiniHome /> */}
        </div>
          
        <div className="scroll-down-btn" onClick={scrollToTodo}>
          <button>
            <svg 
              xmlns="http://www.w3.org/2000/svg" 
              viewBox="0 0 600 600" 
              width="50" 
              height="50" 
              fill="grey"
            >
              <path d="M283.46 274.37C273.34 274.37 263.22 272.17 253.79 267.77L16.75 157.16C4.23 151.32 -1.17 136.44 4.66 123.93C10.50 111.42 25.38 106.01 37.89 111.85L274.94 222.46C280.35 224.99 286.57 224.99 291.98 222.46L529.03 111.85C541.54 106.01 556.42 111.42 562.26 123.93C568.09 136.44 562.68 151.32 550.17 157.16L313.13 267.77C303.70 272.17 293.58 274.37 283.46 274.37Z"/>
            </svg>
          </button>
        </div>
      </div>

      <div className="second" style={{background : colors.genderColors[gender].PRIMARY}}>
        <div className="todo" ref={todoRef}>
          <div className="now-todo">
            <div className="text-field">
              <div className="head">지금 진행하셔야 해요! ✨</div>
              <div className="sub">
                본식까지 <strong>{untilWedding}일</strong> 남았어요. <br />
                두 사람의 특별한 날을 더욱 완벽하게 준비할 시간이에요. <br />
                지금 해야 할 일들을 확인하고 차근차근 진행해볼까요? 😊
              </div>
            </div>
            {nowTodo.map((plan) => (
              <VirginRoadCard 
              key={plan.planId}
              step={plan.shopName}
              serviceType={plan.serviceType}
              ment={plan.tip.tipContent}
              />
            ))}
          </div>

          <div className="further-todo">
            <div className="text-field">
              <div className="head">다가오는 일정, 미리 준비해요 ⏳</div>
              <div className="sub">
                곧 진행할 웨딩 준비 목록들이에요. <br />
                감안해두면 더욱 여유롭고 행복한 결혼 준비가 될 거예요. 💍
              </div>
            </div>
            {furtherTodo.map((plan) => (
              <VirginRoadCard 
              key={plan.planId}
              step={plan.shopName}
              serviceType={plan.serviceType}
              ment={plan.tip.tipContent}
              />
            ))}        
          </div>

          <div className="incomplete">
            <div className="text-field">
              <div className="head">타임 캡슐을 작성해주세요 📌</div>
              <div className="sub">
                아직 타임 캡슐을 작성하지 않은 항목들이에요. <br />
                시간을 내어 우리의 소중한 추억을 기록해주세요! 📝
              </div>
            </div>
            {incomplete.map((plan) => (
              <VirginRoadCard 
              // icon={() => iconForPlans(plan)}
              key={plan.planId}
              step={plan.shopName}
              serviceType={plan.serviceType}
              ment={plan.tip.tipContent}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Main 
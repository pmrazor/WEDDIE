// to 버진로드 , to 미작성 타임캡슐 작성 트리거, ... 
// 온보딩 이후(버진로드 생성된 이후)의 사용자들에게 보여줄 페이지 
import { useNavigate } from "react-router-dom"
import "@/style/page/AfterOnBoarding.css"
import Card from "@/components/common/cards/IconCard"
import AdvertisementCard from "@/components/common/cards/AdvertisementCard"
import DDayCard from "@/components/common/cards/DDayCard"
import { useEffect, useState } from "react"
import { colors } from "@/constants/colors"
import Button from "@/components/common/Button"
import flowerbouquet from "@/assets/BeforeOnBoarding/flower-bouquet.png"
import dummy02 from "/Advertisement/dummy02.jpg"


const weddingDay = new Date("2025-05-05")
const startDay = new Date("2023-11-01")

const fromTodayDDay = (targetDate) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diffTime = targetDate.getTime() - today.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

const AfterOnBoarding = () => {
  const nav = useNavigate()
  const [untilWedding , setUntilWedding] = useState(fromTodayDDay(weddingDay))
  const [fromStart, setFromStart] = useState(-fromTodayDDay(startDay))

  useEffect(() => {
    const updateDDay = () => {
      setUntilWedding(fromTodayDDay(weddingDay))
      setFromStart(-fromTodayDDay(startDay))
    }

    updateDDay()

    const now = new Date()
    const msUntilMidnight = 
      new Date(now.getFullYear(), now.getMonth(), now.getDate() +1, 0, 0, 0) 
      - now 
    
    const timeout = setTimeout(() => {
      updateDDay()
      setInterval(updateDDay, 24 * 60 * 60 * 1000)
    }, msUntilMidnight)

    return () => clearTimeout(timeout)
  }, [])

  const onClickVirginRoad = () => {
    return nav("/virginroad")
  }

  return (
    <div className="AfterOnBoarding">
      <div className="Advertisement">
        <AdvertisementCard 
          headText={"작지만 확실한 행복, 스몰웨딩"}
          subText1={"소중한 사람들과 함께하는 작은 결혼식 준비 가이드"}
          style={{backgroundImage : `url(${dummy02})`}}
        />
        {/* 여기에 n개 중 몇번째인지 보여주는 버튼 UI */}
      </div>
      <div className="D-Day"> 
        <DDayCard 
          head_text={`D-${untilWedding}`}
          sub_text={"결혼식까지"}
          style={{
            backgroundColor : colors.genderColors["Default"].PRIMARY_LIGHT, 
          }}
          headTextStyle={{color : colors.genderColors["Default"].PRIMARY_EXTRADEEP}}
          />
        <DDayCard 
          head_text={`D+${fromStart}`}
          sub_text={"우리가 만난 지 "}
          style={{
            backgroundColor : colors.genderColors["Default"].PRIMARY_LIGHT, 
          }}
          headTextStyle={{color : colors.genderColors["Default"].PRIMARY_EXTRADEEP}}
        />
      </div>
      <div className="to-virgin-road">
        <Card 
          // onClick={onClickVirginRoad}
          type={"OnBoarding"}
          head_text1={"버진 로드를 걸어보아요"}
          // head_text2={"서로를 조금 더 생각해요"}
          sub_text1={"우리 부부만을 위한 버진로드가 완성되었어요"}
          // sub_text2={"아름다운 버진 로드, 서로 사랑만 하며 만들어가요"}
          icon={<img src={flowerbouquet} alt="flower-bouquet icon" />}
          style={{backgroundColor: colors.genderColors["Default"].PRIMARY_EXTRALIGHT}}
        /> 
        <Button 
          onClick={onClickVirginRoad}
          text={"버진 로드 걷기"}
          style={{
            backgroundColor : colors.genderColors["Default"].PRIMARY
          }}
        />
      </div>
    </div>
  )
}

export default AfterOnBoarding
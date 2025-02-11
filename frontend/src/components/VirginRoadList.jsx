import { useEffect, useState } from "react";
import "../style/VirginRoadList.css"
import VirginRoadCard from "./common/cards/VirginRoadCard"

// 더미 데이터 (mockData)
const mockData = [
  { step: "웨딩홀", planStatus: "FINISHED", activated: "TRUE" },
  { step: "스튜디오", planStatus: "NOT_TIME_CAPSULE", activated: "TRUE" },
  { step: "드레스", planStatus: "IN_PROGRESS", activated: "TRUE" },
  { step: "메이크업 스튜디오", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "한복", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "남성 예복", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "스냅 촬영", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "청첩장 만들기", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "피부케어 및 관리", planStatus: "BEFORE_START", activated: "TRUE" },
  { step: "허니문 준비", planStatus: "BEFORE_START", activated: "TRUE" }
];

const VirginRoadList = () => {
  const [VirginRoadData, setVirginRoadData] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // virginRoad 생성 후 데이터 불러오는 함수
  const createVirginRoad = async () => {
    try {
      const response = await fetch("/api/virginRoad", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({})
      })
      
      if (!response.ok) {
        throw new Error("Failed to create virgin road")
      }
      console.log("Virgin road created successfully")
    } catch (err) {
      setError(err.message)
    }
  }

  // virginRoad 데이터 불러오는 함수 (GET 요청)
  const fetchVirginRoadData = async () => {
    try {
      const response = await fetch("/api/virginRoad")
      if (!response.ok) {
        throw new Error("Failed to fetch data")
      }
      const data = await response.json()
      setVirginRoadData(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // useEffect로 컴포넌트 로드 시 virginRoad 데이터 불러오기
  useEffect(() => {
    createVirginRoad()  // 처음에 virginRoad 생성 (POST)
    fetchVirginRoadData()  // 생성된 데이터 불러오기 (GET)
  }, [])

  return (
    <div className="VirginRoadList">
      {mockData.map((item, index) => (
        <VirginRoadCard 
          key={index} 
          step={item.step} 
          planStatus={item.planStatus} 
          activated={item.activated} 
        />
      ))}
    </div>
  );
}

export default VirginRoadList;

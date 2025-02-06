// to 버진로드 , to 미작성 타임캡슐 작성 트리거, ... 
// 온보딩 이후(버진로드 생성된 이후)의 사용자들에게 보여줄 페이지 
import { useNavigate } from "react-router-dom"
import "../style/AfterOnBoarding.css"


const AfterOnBoarding = () => {
  const nav = useNavigate()

  return (
    <div>
      AfterOnBoarding <br/>
      <button onClick={() => nav("/virginroad")}>to VirginRoad</button>
    </div>
  )
}

export default AfterOnBoarding
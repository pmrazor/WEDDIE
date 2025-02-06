// 버진로드 작성하라는 트리거 페이지 
import useAuthStore from "@/store/auth"
import "../style/BeforeOnBoarding.css"
import { useNavigate } from "react-router-dom"

const BeforeOnBoarding = () => {
  const { isAuthenticated } = useAuthStore() 
  const nav = useNavigate()

  return (
    <div>
      BeforeOnBoarding
      <div>
        {!isAuthenticated && <button onClick={() => nav("/login")}>login</button>} <br />
        {!isAuthenticated && <button onClick={() => nav("/register")}>Register</button>} <br />
        <button onClick={() => nav("/onboarding")}>onBoarding</button>
      </div>
    </div>
  )
}

export default BeforeOnBoarding
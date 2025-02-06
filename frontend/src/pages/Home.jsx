// Home : 로고 스플래시 화면으로 구현 

import "../style/Home.css"
import { useNavigate } from "react-router-dom"
import useOnBoardingStore from "@/store/onboarding"
import { useEffect } from "react"

const Home = () => {

  const nav = useNavigate()
  const { isOnBoarded } = useOnBoardingStore()

  useEffect(() => {
    setTimeout(() => {
      nav(isOnBoarded ? "/after_onboarding" : "/before_onboarding")
    }, 2000); // 2초
  }, [isOnBoarded, nav])

  return (
    <div>
      로딩 중 ...
    </div>
  )
}

export default Home
// Home : 로고 스플래시 화면으로 구현

import "@/style/page/Home.css";
import { useNavigate } from "react-router-dom";
import useOnBoardingStore from "@/store/onboarding";
import { useEffect } from "react";
import splash from "@/assets/logo.webp";

const Home = () => {
  const nav = useNavigate();
  const { isOnBoarded } = useOnBoardingStore();

  useEffect(() => {
    setTimeout(() => {
      nav(isOnBoarded ? "/afteronboarding" : "/beforeonboarding")
    }, 2000); // 2초
  }, [isOnBoarded, nav])

  return (
    <div className="flex items-center justify-center h-screen bg-background">
      <img
        src={splash}
        alt="Logo"
        className="w-[1000px] max-w-full h-auto animate-pulse -mt-32"
      />
    </div>
  );
};

export default Home;

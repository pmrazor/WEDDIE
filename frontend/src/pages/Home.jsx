// Home : 로고 스플래시 화면으로 구현

import "@/style/page/Home.css";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import useOnBoardingStore from "@/store/onboarding";
import useVirginRoadStore from "@/store/virginroad";
import splash from "@/assets/logo.webp";

const Home = () => {
  const nav = useNavigate();
  const [hasVirginRoad, setHasVirginRoad] = useState(false);
  const { isOnBoarded } = useOnBoardingStore();
  const { getVirginRoad } = useVirginRoadStore();

  useEffect(() => {
    const checkUserStatus = async () => {
      try {
        await getVirginRoad();
        setHasVirginRoad(true);
      } catch (error) {
        if (error.message.includes("존재하지 않는 플래너입니다")) {
          setHasVirginRoad(false);
        }
      }
    };
    checkUserStatus();
  }, [getVirginRoad]);

  useEffect(() => {
    setTimeout(() => {
      nav(hasVirginRoad ? "/main" : "/beforeonboarding");
    }, 2000); // 2초
  }, [hasVirginRoad, nav]);

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

import "./App.css";
import { Routes, Route } from "react-router-dom";
import Login from "@/pages/login/login"; // 또는 별도의 Login 컴포넌트
import Home from "@/pages/Home";
import Onboarding from "./pages/auth/onboarding/OnBoarding";
import VirginRoad from "./pages/virginRoad/virginroad/VirginRoad";
import TimeCapsule from "./pages/virginRoad/timecapsule/TimeCapsule";
import NotFound from "./pages/NotFound";
import BeforeOnBoarding from "./pages/BeforeOnBoarding";
import AfterOnBoarding from "./pages/AfterOnBoarding";
import Register from "./pages/auth/register/Register";
import Callback from "@/pages/auth/oauth/callback";
import CouplePage from "@/pages/couple/CouplePage";
import { useEffect } from "react";
import { injectColors } from "./constants/colors";
import VirginRoadDetail from "./pages/virginRoad/virginroad/VirginRoadDetail";
import ApiTest from "./pages/apitest/ApiTest";
import VirginRoadSplash from "./pages/virginRoad/splash";
import ShopList from "./pages/shop/shopList";
import ShopDetail from "./pages/shop/shopDetail";
import Main from "./pages/Main";
import VirginRoad2 from "./pages/virginRoadV2/NewVirginRoad";
import WatchTimecapsule from "./pages/virginRoadV2/WatchTimecapsule";

function App() {
  // 색상 설정 함수 호출
  useEffect(() => {
    injectColors();
  }, []);

  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/callback" element={<Callback />} />
        <Route path="/register" element={<Register />} />
        <Route path="/beforeonboarding" element={<BeforeOnBoarding />} />
        <Route path="/afteronboarding" element={<AfterOnBoarding />} />
        <Route path="/onboarding" element={<Onboarding />} />
        {/* <Route path="/virginroad" element={<VirginRoad />} /> */}
        <Route path="/virginroad/splash" element={<VirginRoadSplash />} />
        {/* <Route path="/virginroad/:planId" element={<VirginRoadDetail />} /> */}
        {/* <Route path="/timecapsule/:nodeId" element={<TimeCapsule />} /> */}
        <Route path="/couple" element={<CouplePage />} />
        <Route path="/api-test" element={<ApiTest />} />
        <Route path="/shop/list" element={<ShopList />} />
        <Route path="/shop/:shopId" element={<ShopDetail />} />
        <Route path="/main" element={<Main />} />
        <Route path="/virginroad" element={<VirginRoad2 />} />
        <Route path="/timecapsule" element={<WatchTimecapsule />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}

export default App;

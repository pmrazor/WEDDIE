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
import Test from "./style/text";
import { useEffect } from "react";
import { injectColors } from "./constants/colors";
import { ThemeProvider } from "./contexts/theme";

function App() {
  // 색상 설정 함수 호출 
  useEffect(() => {
    injectColors()
  }, [])
  
  return (
    <>
    <ThemeProvider>
      <Routes>
        <Route path="/" element={<Home/>} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/callback" element={<Callback />} />
        <Route path="/register" element={<Register />} />
        <Route path="/beforeonboarding" element={<BeforeOnBoarding />} />
        <Route path="/afteronboarding" element={<AfterOnBoarding />} />
        <Route path="/onboarding" element={<Onboarding />} />
        <Route path="/virginroad" element={<VirginRoad />} />
        <Route path="/timecapsule/:nodeId" element={<TimeCapsule />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </ThemeProvider>
    </>
  );
}


export default App;

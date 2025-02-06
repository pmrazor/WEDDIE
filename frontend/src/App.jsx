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

function App() {
  return (
    <>
      <Routes>
        {/* <Route path="/" element={<Home/>} /> */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/oauth/callback" element={<Callback />} />
        <Route path="/register" element={<Register />} />
        <Route path="/before_onboarding" element={<BeforeOnBoarding />} />
        <Route path="/after_onboarding" element={<AfterOnBoarding />} />
        <Route path="/onboarding" element={<Onboarding />} />
        <Route path="/virginroad" element={<VirginRoad />} />
        <Route path="/timecapsule/:nodeId" element={<TimeCapsule />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </>
  );
}

export default App;

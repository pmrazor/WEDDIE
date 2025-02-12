// 버진로드 작성하라는 트리거 페이지
import { useState, useEffect } from "react";
import "@/style/page/BeforeOnBoarding.css";
import useAuthStore from "@/store/auth";
import { useNavigate } from "react-router-dom";
import Card from "@/components/common/cards/IconCard";
import Button from "@/components/common/Button";
import Modal from "@/components/common/Modal";
import { colors } from "@/constants/colors";
import userApi from "@/utils/userApi";
import coupleApi from "@/utils/coupleApi";
import man from "@/assets/BeforeOnBoarding/man.svg";
import heartwithribbon from "@/assets/BeforeOnBoarding/heartwithribbon.svg";
import flowerbouquet from "@/assets/BeforeOnBoarding/flower-bouquet.png";
import useOnboardingStore from "@/store/onboarding";
import useVirginRoadStore from "@/store/virginroad";

const BeforeOnBoarding = () => {
  const { isAuthenticated } = useAuthStore();
  const { getVirginRoad } = useVirginRoadStore();
  const [isOnBoarded, setIsOnBoarded] = useState(false);
  const [hasCoupleInfo, setHasCoupleInfo] = useState(false);
  const [hasVirginRoad, setHasVirginRoad] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalMessage, setModalMessage] = useState("");
  const [pendingNavigation, setPendingNavigation] = useState("");
  const nav = useNavigate();

  useEffect(() => {
    const checkUserStatus = async () => {
      if (isAuthenticated) {
        try {
          console.log("Checking onboarding status...");
          const onBoardingStatus = await userApi.getUserDetailFlag();
          console.log("Onboarding status:", onBoardingStatus);
          setIsOnBoarded(onBoardingStatus);

          console.log("Fetching couple details...");
          const coupleDetails = await coupleApi.getCoupleDetails();
          console.log("Couple details:", coupleDetails);
          setHasCoupleInfo(true);

          // 버진로드 존재 여부 확인
          try {
            await getVirginRoad();
            setHasVirginRoad(true);
          } catch (error) {
            if (error.message.includes("존재하지 않는 플래너입니다")) {
              setHasVirginRoad(false);
            }
          }
        } catch (error) {
          console.error("Error checking user status:", error);
          if (error.message.includes("커플 정보를 불러오는데 실패했습니다")) {
            setModalMessage(
              "커플 정보가 설정되어 있지 않습니다. 설정하러 가시겠습니까?"
            );
            setShowModal(true);
            setPendingNavigation("/couple");
          }
        }
      }
    };
    checkUserStatus();
  }, [isAuthenticated, nav, getVirginRoad]);

  const onClickButton = () => {
    if (!isAuthenticated) {
      nav("/login");
    } else if (!isOnBoarded) {
      setModalMessage(
        "온보딩이 완료되지 않았습니다. 온보딩을 진행하시겠습니까?"
      );
      setShowModal(true);
      setPendingNavigation("/onboarding");
    } else if (!hasCoupleInfo) {
      setModalMessage(
        "커플 정보가 설정되어 있지 않습니다. 설정하러 가시겠습니까?"
      );
      setShowModal(true);
      setPendingNavigation("/couple");
    } else if (!hasVirginRoad) {
      setModalMessage(
        "버진로드가 아직 생성되지 않았습니다. AI 추천 로드를 생성하러 가시겠습니까?"
      );
      setShowModal(true);
      setPendingNavigation("/virginroad/splash"); // 버진로드 생성 페이지 경로
    } else {
      nav("/afteronboarding");
    }
  };

  const handleModalConfirm = () => {
    setShowModal(false);
    if (pendingNavigation) {
      nav(pendingNavigation);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    setPendingNavigation("");
  };

  // const { theme } = useTheme()
  // console.log("[from BeforeOnBoarding.jsx] theme.PRIMARY : ", theme.PRIMARY)

  const gender = "Male";

  return (
    <div className="BeforeOnBoarding">
      {/* BeforeOnBoarding */}
      {/* <div>
        {!isAuthenticated && <button onClick={() => nav("/login")}>login</button>} <br />
        {!isAuthenticated && <button onClick={() => nav("/register")}>Register</button>} <br />
        <button onClick={() => nav("/onboarding")}>onBoarding</button>
      </div>   */}
      <div>
        <Button
          onClick={onClickButton}
          stickyType={"UNDER"}
          text={"우리 부부만의 버진 로드 시작하기"}
          style={{ backgroundColor: colors.genderColors["Default"].PRIMARY }}
        />
      </div>
      <Modal
        isOpen={showModal}
        onClose={handleModalClose}
        onConfirm={handleModalConfirm}
        message={modalMessage}
      />
      <div className="card-container">
        <Card
          type={"OnBoarding"}
          head_text1={"결혼을 결심하셨나요?"}
          head_text2={"정말 잘 하셨어요!"}
          sub_text1={"우리 부부만의 AI 비서가"}
          sub_text2={"결혼과 이후 과정까지 책임질게요"}
          icon={<img src={man} alt="man icon" />}
          style={{
            backgroundColor: colors.genderColors["Male"].PRIMARY_EXTRALIGHT,
          }}
        />
        <Card
          type={"OnBoarding"}
          head_text1={"같이 걸어볼까요?"}
          head_text2={"우리 부부만의 버진 로드"}
          sub_text1={"AI 비서가 우리 부부의 상황을 분석해"}
          sub_text2={"최적의 로드를 설계해 드릴게요"}
          icon={<img src={flowerbouquet} alt="flower-bouquet icon" />}
          style={{
            backgroundColor: colors.genderColors["Female"].PRIMARY_EXTRALIGHT,
          }}
        />
        <Card
          type={"OnBoarding"}
          head_text1={"불편한 부담은 덜고"}
          head_text2={"서로를 조금 더 생각해요"}
          sub_text1={"귀찮고 어려운 건 Weddie가 도와 드릴게요"}
          sub_text2={"아름다운 버진 로드, 서로 사랑만 하며 만들어가요"}
          icon={<img src={heartwithribbon} alt="heartwithribbon icon" />}
          style={{
            backgroundColor: colors.genderColors["Default"].PRIMARY_EXTRALIGHT,
          }}
        />
      </div>
      {/* <div style={{paddingBlockEnd:"15vh"}}></div> */}
    </div>
  );
};

export default BeforeOnBoarding;

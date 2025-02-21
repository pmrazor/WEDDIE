// 버진로드 작성하라는 트리거 페이지
import { useState, useEffect } from "react";
import "@/style/page/BeforeOnBoarding.css";
import useAuthStore from "@/store/auth";
import { useNavigate } from "react-router-dom";
import IconCard from "@/components/common/cards/IconCard";
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
import "@/style/page/BeforeOnBoarding.css";
import dummy02 from "/Advertisement/dummy02.jpg";
import logo from "@/assets/logo.png";

const BeforeOnBoarding = () => {
  const { isAuthenticated } = useAuthStore();
  const { getVirginRoad } = useVirginRoadStore();
  const [isOnBoarded, setIsOnBoarded] = useState(false);
  const [hasCoupleInfo, setHasCoupleInfo] = useState(false);
  const [hasVirginRoad, setHasVirginRoad] = useState(false);
  const [hasBudget, setHasBudget] = useState(false);
  const [hasMarriageDate, setHasMarriageDate] = useState(false);
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

          if (
            coupleDetails.budget !== null &&
            coupleDetails.marriageDate !== null
          ) {
            setHasBudget(true);
            setHasMarriageDate(true);
          }

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
    } else if (!hasBudget && !hasMarriageDate) {
      setModalMessage(
        "커플 상세 정보가 설정되어 있지 않습니다. 설정하러 가시겠습니까?"
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

  // // 폭죽 이펙트
  // const createFireworks = (x, y) => {
  //   const firework = document.createElement("div");
  //   firework.classList.add("firework");
  //   firework.style.left = `${x - 50}px`;  // 폭죽의 중심 위치 조정
  //   firework.style.top = `${y - 50}px`;   // 폭죽의 중심 위치 조정
  //   document.body.appendChild(firework);

  //   setTimeout(() => {
  //     firework.remove();
  //   }, 1000); // 1초 후에 폭죽 제거
  // };

  useEffect(() => {
    const handleScroll = () => {
      const cards = document.querySelectorAll(".IconCard");
      cards.forEach((card) => {
        const rect = card.getBoundingClientRect();
        const windowHeight = window.innerHeight;
        const offset = rect.top - windowHeight * 0.2; // 조금 더 부드럽게 보이도록 조절

        // 이미 1로 보이는 건 수정하지 않음
        if (card.style.opacity === "1") return;

        // 화면 중앙보다 위에 있을 때 === 스크롤 아래로 내릴 때
        if (offset < 0) {
          // card.classList.add("show")
          let opacity = 1 - Math.abs(offset) / 200; // 200px 이상 넘어가면 최대 opacity 1
          opacity = Math.max(opacity, 0);
          card.style.opacity = opacity;

          if (opacity >= 0.5) {
            card.style.opacity = "1";
            if (!card.classList.contains("show")) {
              card.classList.add("show");
              // // card 위치 기준 폭죽 터트리기
              // const cardRect = card.getBoundingClientRect()
              // createFireworks(cardRect.left + cardRect.width / 2, cardRect.top + cardRect.height / 2)
            }
          }
        } else {
          card.style.opacity = 0; // 화면 아래 있을 때는
        }
      });
    };

    window.addEventListener("scroll", handleScroll);
    handleScroll(); // 초기 로드 시 실행

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  return (
    <div className="BeforeOnBoarding">
      <Modal
        isOpen={showModal}
        onClose={handleModalClose}
        onConfirm={handleModalConfirm}
        message={modalMessage}
      />
      <div className="card-container">
        <IconCard
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
        <IconCard
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
        <IconCard
          type={"OnBoarding"}
          head_text1={"불편한 부담은 덜고"}
          head_text2={"서로를 조금 더 생각해요"}
          sub_text1={"귀찮고 어려운 건 Weddie가 도와 드릴게요"}
          sub_text2={"아름다운 버진 로드, 서로 사랑만 하며 만들어가요"}
          icon={ <img src={heartwithribbon} alt="heartwithribbon icon" /> }
          style={{
            backgroundColor: colors.genderColors["Default"].PRIMARY_EXTRALIGHT,
          }}
        />
        <IconCard
          head_text1={"이제 시작해볼까요?"}
          sub_text1={"우리 부부만의 아름다운 버진 로드 말이에요!"}
          sub_text2={"이 버튼을 누르고 , 버진 로드를 만들어봐요"}
          icon={
            <Button
              onClick={onClickButton}
              text={"우리 부부만의 버진 로드 시작하기"}
              style={{ 
                backgroundColor: colors.genderColors["Default"].PRIMARY,
                color : "white"
              }}
            />
          }    
        />
        
        <div className="footer">
          <img className="logo" src={logo} alt="Logo" />
          <span>with Weddie</span>
          <img src={dummy02} alt="Dummy Image" />
        </div>
      </div>
    </div>
  );
};

export default BeforeOnBoarding;

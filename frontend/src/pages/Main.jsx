import DDayCard from "@/components/common/cards/DDayCard";
import VirginRoadCard from "@/components/common/cards/VirginRoadCard";
import "@/style/page/Main.css";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import AdvertisementCard from "@/components/common/cards/AdvertisementCard";
import lv1 from "@/assets/main-cy-image/lv1.png";
import lv11 from "@/assets/main-cy-image/lv11.png";
import page1 from "@/assets/main-cy-image/1.png";
import { colors } from "@/constants/colors";
import MiniHome from "./MiniHome";
import before from "@/assets/main-cy-image/before.png";
import after from "@/assets/main-cy-image/after.png";
import backmale from "@/assets/main/back_male.png";
import backfemale from "@/assets/main/back_female.png";
import axios from "axios";
import instance from "@/lib/axios";
import usePerplexityStore from "@/store/perplexityAdvice";
import Slider from "react-slick"
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import loveLetter from "@/assets/main/loveletter.svg"
import memo from "@/assets/main/memo.svg"
import ReactMarkdown from "react-markdown";

const getStepName = (serviceType) => {
  const stepNames = {
    DRESS_SHOP: "드레스샵",
    STUDIO: "스튜디오",
    WEDDING_HALL: "웨딩홀",
    MAKEUP_STUDIO: "메이크업",
    SNAP: "스냅사진",
    HANBOK: "한복",
    TAILOR_SHOP: "맞춤복",
    INVITATION: "청첩장",
    FACIAL_CARE: "피부관리",
    HONEYMOON: "신혼여행",
    WEDDING_DAY: "웨딩데이",
  };
  return stepNames[serviceType] || serviceType;
};

const getRandomMessage = () => {
  const messages = [
    "우리의 새로운 시작이 다가오고 있어요!",
    "설렘 가득한 하루하루, 함께 준비해요!",
    "기다림도 특별한 추억이 될 거예요!",
    "하나씩 준비해 가는 과정도 소중한 순간이 될 거예요!",
    "함께할 날이 가까워질수록 마음이 더 두근거려요!",
    "우리의 사랑이 더 빛날 날을 기대해요!",
    "오늘도 한 걸음 더 가까이, 행복한 결혼식이 기다리고 있어요!",
    "설렘과 기쁨이 가득한 날들을 함께 채워가요!",
    "완벽한 순간을 위해 함께하는 모든 순간이 소중해요!",
    "우리 둘만의 특별한 이야기가 시작되고 있어요!",
    "사랑과 행복으로 가득 찬 하루하루를 만들어가요!",
    "조금씩 준비해 가며 우리만의 색깔을 채워가요!",
    "소중한 사람들과 함께할 행복한 날이 머지않았어요!",
    "결혼식 준비도 우리 둘만의 추억이 될 거예요!",
    "설렘과 기대 속에서 하나씩 완성해 가는 중이에요!",
    "우리의 시작이 더 아름다워질 수 있도록, 오늘도 함께해요!",
    "신부와 신랑으로서의 우리의 새로운 이야기가 시작돼요!",
    "평생 함께할 우리의 첫걸음, 설렘과 행복으로 가득 채워가요!",
    "사랑이 깊어지는 이 시간, 더 소중하게 기억할 거예요!",
    "우리의 약속이 현실이 되어가는 순간, 기대되지 않나요?",
  ];
  return messages[Math.floor(Math.random() * messages.length)];
};

const weddingDay = new Date("2025-06-13");
const startDay = new Date("2023-11-01");

const fromTodayDDay = (targetDate, todayDate) => {
  if (!targetDate) return null;

  const target = new Date(targetDate); // 문자열을 Date 객체로 변환
  const today = new Date(todayDate);
  today.setHours(0, 0, 0, 0);
  const diffTime = target.getTime() - today.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

// const gender = "Female";

const Main = () => {
  const nav = useNavigate();
  const { practicalAdvice, emotionalAdvice, getPracticalAdvice, getEmotionalAdvice } = usePerplexityStore();
  const [untilWedding, setUntilWedding] = useState(null);
  const [marriageDate, setMarriageDate] = useState(null);
  const [currentDate, setCurrentDate] = useState(null);
  const [randomMessage, setRandomMessage] = useState(getRandomMessage());
  const [isLoading, setIsLoading] = useState(true);
  const [groupedPlans, setGroupedPlans] = useState(null);
  const [error, setError] = useState(null);

  const getGenderFromLocalStorage = () => {
    const authStorage = localStorage.getItem("auth-storage");
    if (authStorage) {
      const { state } = JSON.parse(authStorage);
      return state.gender;
    }
    return "Female";
  };

  const [gender, setGender] = useState(null);

  useEffect(() => {
    getPracticalAdvice();
  }, [getPracticalAdvice]);

  useEffect(() => {
    console.log("Fetching emotional advice...");
    getEmotionalAdvice().then(() => {
      console.log("Emotional advice fetched successfully");
    }).catch(err => {
      console.error("Error fetching emotional advice:", err);
    });
  }, [getEmotionalAdvice]);

  useEffect(() => {
    const currentGender = getGenderFromLocalStorage();
    setGender(currentGender);
  }, []);

  useEffect(() => {
    setTimeout(() => setIsLoaded(true), 500);
  }, []);

  useEffect(() => {
    const fetchPlans = async () => {
      try {
        const response = await instance.get("/api/plan/main");
        console.log("response : ", response);
        if (response.data.isSuccess) {

          const filteredPlans = {}
          Object.keys(response.data.result.groupedPlans).forEach((key) => {
            filteredPlans[key] = response.data.result.groupedPlans[key].filter(
              (plan) => plan.visible === true 
            )
          })
          console.log(filteredPlans)
          // setGroupedPlans(response.data.result.groupedPlans);
          setGroupedPlans(filteredPlans);
          setMarriageDate(response.data.result.marriageDate);
          setCurrentDate(response.data.result.currentDate);
          // marriageDate가 설정되면 바로 D-day 계산
          setUntilWedding(fromTodayDDay(response.data.result.marriageDate, response.data.result.currentDate));
        } else {
          throw new Error("플랜 데이터를 불러오는 데 실패했습니다.");
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchPlans();

    const updateDDay = () => {
      if (marriageDate) {
        setUntilWedding(fromTodayDDay(marriageDate));
        setRandomMessage(getRandomMessage());
      }
    };

    const interval = setInterval(updateDDay, 24 * 60 * 60 * 1000);
    return () => clearInterval(interval);
  }, [marriageDate]); // marriageDate를 의존성 배열에 추가

  const level = 1;

  const todoRef = useRef(null);
  const scrollToTodo = () => {
    if (todoRef.current) {
      todoRef.current.scrollIntoView({ behavior: "smooth" }); // 부드러운 스크롤
    }
  };

  const [isLoaded, setIsLoaded] = useState(false);

  if (isLoading) return <div>로딩 중...</div>;
  if (error) return <div>오류 발생: {error}</div>;

  const nowTodo = groupedPlans?.["IN_PROGRESS"] || [];
  const furtherTodo = groupedPlans?.["BEFORE_START"] || [];
  const incomplete = groupedPlans?.["NOT_TIME_CAPSULE"] || [];

  const carouselSettings = {
    dots: true,          
    infinite: true,      
    speed: 500,          
    slidesToShow: 1,     
    slidesToScroll: 1,   
    autoplay: true,      
    autoplaySpeed: 5000, 
    arrows: true,       
    prevArrow: <button className="prev-arrow">{"<"}</button>,
    nextArrow: <button className="next-arrow">{">"}</button>,
  };
  
  return (
    <div className="Main">
      <div className="first">
        <div className="d-day">
          <AdvertisementCard
            headText1={`D${untilWedding > 0 ? `-${untilWedding}` : `+${-untilWedding}`}`}
            subText1={`우리의 사랑이 결실을 맺을 날, 얼마 남지 않았어요`}
            subText3={randomMessage}
            style={
              gender === "Male"
                ? { backgroundImage: `url(${backmale})` }
                : { backgroundImage: `url(${backfemale})` }
            }
          />
        </div>
        <div 
          className="notice-area"
          style={{backgroundColor : colors.genderColors[gender].PRIMARY_EXTRALIGHT}}
        >
          {/* 캐러셀 - AI 조언과 , 감성 조언을 알려주는 부분 */}
          <Slider {...carouselSettings}>
            <div className="notice-container">
              {/* <div className="notice-head">AI 할일 조언 </div> */}
              <div className="notice-icon">
                <img src={memo} alt="" />
              </div>
              <div className="notice-text">
              <ReactMarkdown>{practicalAdvice}</ReactMarkdown>
            </div>
            </div>
            <div className="notice-container">
              {/* <div className="notice-head">AI 감성 조언 </div> */}
              <div className="notice-icon">
                <img src={loveLetter} alt="" />
              </div>
              <div className="notice-text">
              <ReactMarkdown>{emotionalAdvice}</ReactMarkdown>
              </div>

            </div>
          </Slider> 
        </div>
        <div className="cy-image">
          {level === 1 ? (
            <img src={before} alt="" />
          ) : (
            <img src={after} alt="" />
          )}
          {/* <MiniHome /> */}
        </div>

        <div className="scroll-down-btn" onClick={scrollToTodo}>
          <button>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 600 600"
              width="50"
              height="50"
              fill="grey"
            >
              <path d="M283.46 274.37C273.34 274.37 263.22 272.17 253.79 267.77L16.75 157.16C4.23 151.32 -1.17 136.44 4.66 123.93C10.50 111.42 25.38 106.01 37.89 111.85L274.94 222.46C280.35 224.99 286.57 224.99 291.98 222.46L529.03 111.85C541.54 106.01 556.42 111.42 562.26 123.93C568.09 136.44 562.68 151.32 550.17 157.16L313.13 267.77C303.70 272.17 293.58 274.37 283.46 274.37Z" />
            </svg>
          </button>
        </div>
      </div>

      <div
        className="second"
        style={{ background: colors.genderColors[gender].PRIMARY }}
      >
        <div className="todo" ref={todoRef}>
          {nowTodo && nowTodo.length > 0 ? (

            <div className="now-todo">
              <div className="text-field">
                <div className="head">지금 진행하셔야 해요! ✨</div>
                <div className="sub">
                  본식까지 <strong>{untilWedding}일</strong> 남았어요. <br />
                  두 사람의 특별한 날을 더욱 완벽하게 준비할 시간이에요. <br />
                  지금 해야 할 일들을 확인하고 차근차근 진행해볼까요? 😊
                </div>
              </div>
              {nowTodo.slice(0, 3).map((plan) => (
                <VirginRoadCard
                key={plan.planId}
                step={getStepName(plan.serviceType)}
                planTime={plan.planTime}
                serviceType={plan.serviceType}
                ment={plan.tip}
                currentDate={currentDate}
                />
              ))}
            </div>
            ) : 
            <div className="now-todo">
              <div className="text-field">
                <div className="head">현재 급한 일이 없습니다.</div>
                {/* <div className="sub">{randomMessage}</div> */}
              </div>
            </div>}

            {furtherTodo && furtherTodo.length > 0 ? (

            <div className="further-todo">
              <div className="text-field">
                <div className="head">다가오는 일정, 미리 준비해요 ⏳</div>
                <div className="sub">
                  곧 진행할 웨딩 준비 목록들이에요. <br />
                  감안해두면 더욱 여유롭고 행복한 결혼 준비가 될 거예요. 💍
                </div>
              </div>
            {furtherTodo.slice(0, 3).map((plan) => (
              <VirginRoadCard
              key={plan.planId}
              step={getStepName(plan.serviceType)}
              planTime={plan.planTime}
              serviceType={plan.serviceType}
              ment={plan.tip}
              currentDate={currentDate}
            />
            ))}
          </div>
          ) : 
          <div className="further-todo">
            <div className="text-field">
              <div className="head">다가오는 일정이 없습니다.</div>
              {/* <div className="sub">{randomMessage}</div> */}
            </div>
          </div>
          }

          {incomplete && incomplete.length > 0 && (

            <div className="incomplete">
            <div className="text-field">
              <div className="head">타임 캡슐을 작성해주세요 📌</div>
              <div className="sub">
                아직 타임 캡슐을 작성하지 않은 항목들이에요. <br />
                시간을 내어 우리의 소중한 추억을 기록해주세요! 📝
              </div>
            </div>
            {incomplete.slice(0, 3).map((plan) => (
              <VirginRoadCard
                key={plan.planId}
                step={getStepName(plan.serviceType)}
                planTime={plan.planTime}
                serviceType={plan.serviceType}
                ment={plan.tip}
                currentDate={currentDate}
              />
            ))}
          </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Main;

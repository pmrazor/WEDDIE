import "@/style/component/VirginRoadCard.css";
import { useEffect, useState } from "react";
import Button from "../Button";
import { useNavigate } from "react-router-dom";
// import status1 from "@/assets/VirginRoadCard/status1.svg"
// import status2 from "@/assets/VirginRoadCard/status2.svg"
// import status3 from "@/assets/VirginRoadCard/status3.svg"
// import status4 from "@/assets/VirginRoadCard/status4.svg"
import man1 from "@/assets/VirginRoadCard/man1.svg";
import man2 from "@/assets/VirginRoadCard/man2.svg";
import man3 from "@/assets/VirginRoadCard/man3.svg";
import man4 from "@/assets/VirginRoadCard/man4.svg";
import battery1 from "@/assets/VirginRoadCard/battery1.svg";
import battery2 from "@/assets/VirginRoadCard/battery2.svg";
import battery3 from "@/assets/VirginRoadCard/battery3.svg";
import battery4 from "@/assets/VirginRoadCard/battery4.svg";
import { colors } from "@/constants/colors";
import dressshop from "@/assets/IconforPlans/DRESSSHOP.png";
import facialW from "@/assets/IconforPlans/FACIALCAREW.png";
import facialM from "@/assets/IconforPlans/FACIALCAREM.png";
import hanbokM from "@/assets/IconforPlans/HANBOKM.png";
import hanbokW from "@/assets/IconforPlans/HANBOKW.png";
import honeymoon from "@/assets/IconforPlans/HONEYMOON.png";
import invitaion from "@/assets/IconforPlans/INVITATION.png";
import makeupStudio from "@/assets/IconforPlans/MAKEUPSTUDIO.png";
import snap from "@/assets/IconforPlans/SNAP.png";
import studio from "@/assets/IconforPlans/STUDIO.png";
import tailorShop from "@/assets/IconforPlans/TAILORSHOP.png";
import weddingDay from "@/assets/IconforPlans/WEDDINGDAY.png";
import WeddingHall from "@/assets/IconforPlans/WeddingHall.png";

// const gender = "Female";

const VirginRoadCard = ({
  step,
  planStatus,
  serviceType,
  planTime,
  visible,
  isEditing,
  toggleActivated,
  currentDate,
  ment,
}) => {
  const iconForPlans = () => {
    switch (serviceType) {
      case "WEDDING_DAY":
        return weddingDay;
      case "FACIAL_CARE":
        return gender === "Male" ? facialM : facialW;
      case "DRESS_SHOP":
        return dressshop;
      case "MAKEUP_STUDIO":
        return makeupStudio;
      case "SNAP":
        return snap;
      case "STUDIO":
        return studio;
      case "TAILOR_SHOP":
        return tailorShop;
      case "WEDDING_HALL":
        return WeddingHall;
      case "HONEYMOON":
        return honeymoon;
      case "HANBOK":
        return gender === "Male" ? hanbokM : hanbokW;
      case "INVITATION":
        return invitaion;
      default:
        return weddingDay; // 기본값 설정
    }
  };
  
  // D-Day 계산 함수
  const calculateDday = (dateString, currentDate) => {
    if (!dateString) return "";
    const targetDate = new Date(dateString);
    const today = new Date(currentDate);
    
    // 시간, 분, 초, 밀리초를 0으로 설정하여 날짜만 비교
    today.setHours(0, 0, 0, 0);
    targetDate.setHours(0, 0, 0, 0);
    
    const diffTime = targetDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return "D-Day";
    else if (diffDays > 0) return `D-${diffDays}`;
    else return `D+${Math.abs(diffDays)}`;
  };
  
  const getGenderFromLocalStorage = () => {
    const authStorage = localStorage.getItem("auth-storage");
    if (authStorage) {
      const { state } = JSON.parse(authStorage);
      return state.gender;
    }
    return "Female"; // 기본값 설정
  };

  const [gender, setGender] = useState(getGenderFromLocalStorage()); // 초기값을 바로 설정

  const iconForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START":
        return battery1;
      case "IN_PROGRESS":
        return battery2;
      case "NOT_TIME_CAPSULE":
        return battery3;
      case "FINISHED":
        return battery4;
    }
  };
  const backgroundForStatus = () => {
    switch (planStatus) {
      case "BEFORE_START":
        return colors.genderColors[gender].PRIMARY;
      case "IN_PROGRESS":
        return colors.genderColors[gender].PRIMARY_EXTRADEEP;
      case "NOT_TIME_CAPSULE":
        return colors.genderColors[gender].PRIMARY_DEEP;
      case "FINISHED":
        return colors.genderColors[gender].PRIMARY;
    }
  };

  const nav = useNavigate();
  const onClickToGo = () => {
    if (isEditing) {
      toggleActivated(); // 편집 모드일 때는 토글만 실행
      return;
    }

    // 편집 모드가 아닐 때는 해당 serviceType으로 네비게이트
    nav(`/virginroad?serviceType=${serviceType}`);
  };

  return (
    <div 
      className="VirginRoadCard"
      style={{ backgroundColor: colors.genderColors[gender].PRIMARY_EXTRALIGHT }}
    >
      <div className="icon-area">
        <img src={iconForPlans()} alt="icon" />
      </div>
      <div className="status-area">
        {calculateDday(planTime, currentDate)}
      </div>
      <div className="text-area"> {step} </div>
      <div className="to-go-button">
        <Button
          onClick={onClickToGo}
          text={">"}
          addClass={"to-go-button"}
          style={{
            color: colors.genderColors[gender].PRIMARY_DEEP,
          }}
        />
      </div>
    </div>
  );
};

export default VirginRoadCard;
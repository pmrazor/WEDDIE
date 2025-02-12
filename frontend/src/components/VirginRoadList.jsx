import { useEffect } from "react";
import VirginRoadCard from "@/components/common/cards/VirginRoadCard";
import useVirginRoadStore from "@/store/virginroad";

const VirginRoadList = () => {
  const { plans, getVirginRoad, isLoading } = useVirginRoadStore();


  // 컴포넌트 마운트 시 버진로드 데이터 조회
  useEffect(() => {
    const fetchVirginRoad = async () => {
      try {
        await getVirginRoad();
      } catch (error) {
        console.error("버진로드 조회 실패:", error);
      }
    };
    fetchVirginRoad();
    }, [getVirginRoad]
  );
  // 서비스 타입별 스텝 이름 매핑
  const getStepName = (serviceType) => {
    const stepNames = {
      DRESS_SHOP: "드레스샵",
      STUDIO: "스튜디오",
      WEDDING_HALL: "웨딩홀",
      MAKEUP_STUDIO: "메이크업",
      SNAP: "스냅사진",
    };
    return stepNames[serviceType] || serviceType;
  };

  if (isLoading) {
    return <div>로딩 중...</div>;
  }

  return (
    <div className="virgin-road-list">
      {plans.map((plan) => (
        <VirginRoadCard
          key={plan.planId}
          step={getStepName(plan.serviceType)}
          planStatus={plan.planStatus}
          activated={plan.visible}
        />
      ))}
    </div>
  );
};

export default VirginRoadList;
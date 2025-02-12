
import useVirginRoadStore from "@/store/virginroad";

const VIRGIN_ROAD_INITIAL_PLAN = [
  { serviceType: "DRESS_SHOP", visible: true },
  { serviceType: "STUDIO", visible: true },
  { serviceType: "WEDDING_HALL", visible: true },
];

const VIRGIN_ROAD_UPDATE_PLAN = [
  { serviceType: "WEDDING_HALL", visible: true },
];

// React 컴포넌트로 변경
const VirginRoadDomain = () => {
  const { getVirginRoad, createVirginRoad, deleteVirginRoad, updatePlans, completeVirginRoad } = useVirginRoadStore();

  // 도메인 데이터를 반환
  return {
    name: "버진로드 계획",
    buttons: [
      {
        label: "버진로드 계획 보기",
        api: getVirginRoad,
        requestData: null,
      },
      {
        label: "버진로드 계획 추가",
        api: () => createVirginRoad(VIRGIN_ROAD_INITIAL_PLAN),
        requestData: VIRGIN_ROAD_INITIAL_PLAN,
      },
      {
        label: "버진로드 계획 업데이트",
        api: () => 
          updatePlans(VIRGIN_ROAD_UPDATE_PLAN)
        ,
        requestData: VIRGIN_ROAD_UPDATE_PLAN,
      },
      {
        label: "버진로드 계획 삭제",
        api: deleteVirginRoad,
        requestData: null,
      },
      {
        label: "버진로드 완료",
        api: () => 
          completeVirginRoad()
        ,
        requestData: null,
      }
    ],
  };
};

export default VirginRoadDomain;
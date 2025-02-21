import { useState, useEffect } from "react"; // useEffect 추가
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import useTimeCapsuleStore from "@/store/timecapsule"; // 스토어 import 추가
import useVirginRoadStore from "@/store/virginroad";
import { useToast } from "@/hooks/use-toast"; // toast import 추가

const serviceTypeMap = {
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

const TimeCapsuleView = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [direction, setDirection] = useState(0);
  const navigate = useNavigate();
  const { toast } = useToast();
  const { timeCapsules, isLoading, error, getTimeCapsule } =
    useTimeCapsuleStore();
  const { plans, getVirginRoad } = useVirginRoadStore(); // Virgin Road 스토어에서 plans 가져오기

  const getGenderFromLocalStorage = () => {
    const authStorage = localStorage.getItem("auth-storage");
    if (authStorage) {
      const { state } = JSON.parse(authStorage);
      return state.gender;
    }
    return null;
  };

  const getColorByGender = () => {
    const gender = getGenderFromLocalStorage();
    return gender === "Female" ? "#FFC0CC" : "#B3D8FF";
  };

  // 색상 상태 추가
  const [themeColor, setThemeColor] = useState(getColorByGender());

  useEffect(() => {
    // 컴포넌트 마운트 시 색상 설정
    setThemeColor(getColorByGender());
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      try {
        await getTimeCapsule();
        await getVirginRoad(); // Virgin Road 데이터도 함께 가져오기
      } catch (error) {
        toast({
          title: "데이터 로드 실패",
          description: "타임캡슐을 불러오는데 실패했습니다.",
          variant: "destructive",
        });
      }
    };
    fetchData();
  }, [getTimeCapsule, getVirginRoad, toast]);

  const variants = {
    enter: (direction) => ({
      x: direction > 0 ? 500 : -500,
      opacity: 0,
    }),
    center: {
      x: 0,
      opacity: 1,
    },
    exit: (direction) => ({
      x: direction < 0 ? 500 : -500,
      opacity: 0,
    }),
  };
  // 로딩 상태 처리
  if (isLoading) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white">
        <div className="flex flex-col items-center">
          <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-gray-600">타임캡슐을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  // 에러 상태 처리
  if (error) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white">
        <div className="text-center">
          <p className="text-red-500 mb-4">
            타임캡슐을 불러오는데 실패했습니다
          </p>
          <Button onClick={() => getTimeCapsule()}>다시 시도</Button>
        </div>
      </div>
    );
  }

  // 데이터가 없는 경우 처리
  if (!timeCapsules || timeCapsules.length === 0) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white">
        <div className="text-center">
          <p className="text-gray-600 mb-4">아직 작성된 타임캡슐이 없습니다</p>
          <Button onClick={() => navigate(-1)}>돌아가기</Button>
        </div>
      </div>
    );
  }

  const handleNext = () => {
    if (currentIndex < timeCapsules.length - 1) {
      setDirection(1);
      setCurrentIndex((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    if (currentIndex > 0) {
      setDirection(-1);
      setCurrentIndex((prev) => prev - 1);
    }
  };

  const StarRating = ({ rating }) => {
    return (
      <div className="flex gap-2">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-6 h-6 ${
              star <= rating ? "text-yellow-400 fill-current" : "text-gray-300"
            }`}
          />
        ))}
      </div>
    );
  };

  const renderTimeCapsule = (timeCapsule) => {
    const matchingPlan = plans.find(
      (plan) => plan.planId === timeCapsule.planId
    );
    const serviceType = matchingPlan
      ? serviceTypeMap[matchingPlan.serviceType]
      : "웨딩";

    return (
      <div className="h-full flex flex-col px-4">
        <div className="text-2xl font-bold text-center mb-6">
          나의 {serviceType} 타임캡슐
        </div>

        <div className="flex-1 overflow-y-auto space-y-6">
          {/* 좋았던 점 섹션 */}
          <div className="space-y-4">
            <h3 className="text-xl font-semibold">좋았던 점</h3>
            <p className="text-gray-700 bg-white p-4 rounded-lg shadow">
              {timeCapsule.goodContent}
            </p>
            {timeCapsule.goodImage && (
              <img
                src={timeCapsule.goodImage}
                alt="Good moment"
                className="w-full h-48 object-cover rounded-lg shadow"
              />
            )}
          </div>

          {/* 아쉬웠던 점 섹션 */}
          <div className="space-y-4">
            <h3 className="text-xl font-semibold">아쉬웠던 점</h3>
            <p className="text-gray-700 bg-white p-4 rounded-lg shadow">
              {timeCapsule.badContent}
            </p>
            {timeCapsule.badImage && (
              <img
                src={timeCapsule.badImage}
                alt="Bad moment"
                className="w-full h-48 object-cover rounded-lg shadow"
              />
            )}
          </div>

          {/* 별점 섹션 */}
          <div className="space-y-2">
            <h3 className="text-xl font-semibold">전체 평가</h3>
            <div className="flex justify-center">
              <StarRating rating={timeCapsule.planGrade} />
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="fixed inset-x-0 bottom-0 top-[64px] flex flex-col bg-gray-50">
      {/* 뒤로가기 버튼 */}
      <Button
        variant="ghost"
        className="absolute top-4 left-4 text-2xl"
        onClick={() => navigate(-1)}
      >
        &lt;
      </Button>

      {/* 메인 컨텐츠 영역 */}
      <div className="flex-1 relative overflow-hidden mt-16 mb-16">
        <AnimatePresence initial={false} mode="wait" custom={direction}>
          <motion.div
            key={currentIndex}
            custom={direction}
            variants={variants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{
              x: { type: "spring", stiffness: 300, damping: 30 },
              opacity: { duration: 0.2 },
            }}
            className="absolute w-full h-full"
          >
            {timeCapsules[currentIndex] &&
              renderTimeCapsule(timeCapsules[currentIndex])}
          </motion.div>
        </AnimatePresence>
      </div>

      {/* 네비게이션 버튼 */}
      <div className="flex mt-auto pb-12 mb-1">
        {currentIndex > 0 && (
          <button
            onClick={handleBack}
            className={`w-full h-12 bg-gray-200 text-gray-800 transition-all duration-300 hover:bg-gray-300 active:scale-[0.98]`}
          >
            이전
          </button>
        )}
        {currentIndex < timeCapsules.length - 1 && (
          <button
            onClick={handleNext}
            className={`w-full h-12 text-white transition-all duration-300 hover:opacity-90 active:scale-[0.98]`}
            style={{ backgroundColor: themeColor }} // 여기에 동적 색상 적용
          >
            다음
          </button>
        )}
      </div>
    </div>
  );
};

export default TimeCapsuleView;

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Input } from "@/components/ui/input";
import { useToast } from "@/hooks/use-toast";
import Age from "./level/age";
import Gender from "./level/gender";
import Phone from "./level/phone";
import Mbti from "./level/mbti";
import useOnboardingStore from "@/store/onboarding";

export default function OnBoarding() {
  const [phoneValidated, setPhoneValidated] = useState(false);

  const { toast } = useToast();
  const [formData, setFormData] = useState({
    age: "20",
    gender: "",
    phone: "",
    mbti: "",
  });
  const [step, setStep] = useState(1);
  const [direction, setDirection] = useState(0);

  const updateFormData = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleNext = () => {
    setDirection(1);
    if (step < 5) {
      setStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setDirection(-1);
      setStep((prev) => prev - 1);
    }
  };

  const submitOnboarding = useOnboardingStore(
    (state) => state.submitOnboarding
  );

  const handleSubmit = async () => {
    console.log("최종 제출된 데이터:", formData);
    try {
      const response = await submitOnboarding(formData);
      if (response.success) {
        toast({
          title: "온보딩 정보입력 성공",
          description: "온보딩 정보 입력이 완료되었습니다.",
        });

        // 잠시 후 메인 페이지로 리다이렉트
        setTimeout(() => {
          window.location.href = "/";
        }, 1500);
      } else {
        toast({
          title: "온보딩 정보 입력 실패",
          description: response.error || "온보딩 정보 입력에 실패했습니다.",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error("Onboarding error:", error);
      toast({
        title: "오류 발생",
        description: "온보딩 중 오류가 발생했습니다.",
        variant: "destructive",
      });
    }
  };

  const renderStep = () => {
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

    const getStepComponent = () => {
      switch (step) {
        case 1:
          return (
            <Age
              age={formData.age}
              setAge={(value) => updateFormData("age", value)}
            />
          );
        case 2:
          return (
            <Gender
              gender={formData.gender}
              setGender={(value) => updateFormData("gender", value)}
            />
          );
        case 3:
          return (
            <Phone
              phone={formData.phone}
              setPhone={(value) => updateFormData("phone", value)}
              setPhoneValidated={setPhoneValidated}
            />
          );
        case 4:
          return (
            <Mbti
              mbti={formData.mbti}
              setMbti={(value) => updateFormData("mbti", value)}
            />
          );

        default:
          return null;
      }
    };
    return (
      <div className="flex-1 relative overflow-hidden">
        <AnimatePresence initial={false} mode="wait" custom={direction}>
          <motion.div
            key={step}
            custom={direction}
            variants={variants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{
              x: {
                type: "tween",
                duration: 0.2, // 매우 짧은 시간
                ease: "easeInOut", // 빠르게 시작하고 천천히 끝남
              },
              opacity: {
                duration: 0.2,
              },
            }}
            className="absolute w-full h-full"
          >
            {getStepComponent()}
          </motion.div>
        </AnimatePresence>
      </div>
    );
  };

  return (
    <div className="fixed inset-x-0 bottom-0 top-[64px] flex flex-col">
      {renderStep()}
      <div className="px-4 mb-4">
        <div className="flex justify-between">
          {[1, 2, 3, 4].map((item) => (
            <div
              key={item}
              className={`h-2 flex-1 mx-1 rounded-full ${
                item <= step ? "bg-primary" : "bg-gray-200"
              }`}
            />
          ))}
        </div>
      </div>
      <div className="flex mt-auto pb-12 mb-1">
        {step > 1 && (
          <button
            onClick={handleBack}
            type="button"
            className="w-full h-12 bg-gray-200 text-gray-800 transition-all duration-300 hover:bg-gray-300 active:scale-[0.98]"
          >
            이전
          </button>
        )}
        <button
          onClick={step === 4 ? handleSubmit : handleNext}
          type="button"
          className={`w-full h-12 transition-all duration-300 bg-primary text-background hover:bg-primary/90 active:scale-[0.98]`}
        >
          {step === 4 ? "완료" : "다음"}
        </button>
      </div>
    </div>
  );
}

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useToast } from "@/hooks/use-toast";
import Id from "./level/id";
import Password from "./level/password";
import PasswordConfirm from "./level/password-check";
import Name from "./level/name";
import Gender from "./level/gender";
import useRegisterStore from "@/store/register";

export default function Register() {
  const { toast } = useToast();
  const [formData, setFormData] = useState({
    loginId: "",
    password: "",
    passwordConfirm: "",
    name: "",
    gender: "",
  });
  const [step, setStep] = useState(1);
  const [idValidated, setIdValidated] = useState(false); // ID 검증 상태 추가
  const [passwordValidated, setPasswordValidated] = useState(false);
  const register = useRegisterStore((state) => state.register); // store에서 register 함수 가져오기
  const [passwordConfirmValidated, setPasswordConfirmValidated] =
    useState(false);

  const updateFormData = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleNext = () => {
    // ID 단계에서는 검증이 완료되어야만 다음으로 진행 가능
    if (step === 1 && !idValidated) {
      return;
    }
    if (step === 2 && !passwordValidated) {
      return;
    }
    if (step === 3 && !passwordConfirmValidated) {
      return;
    }
    if (step < 5) {
      setStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep((prev) => prev - 1);
    }
  };

  const handleSubmit = async () => {
    console.log("최종 제출된 데이터:", formData);
    try {
      const { passwordConfirm, ...submitData } = formData;
      const response = await register(submitData);
      if (response.success) {
        toast({
          title: "회원가입 성공",
          description: "회원가입이 완료되었습니다.",
        });

        // 잠시 후 메인 페이지로 리다이렉트
        setTimeout(() => {
          window.location.href = "/";
        }, 1500); // 1.5초 후 리다이렉트
      } else {
        toast({
          title: "회원가입 실패",
          description: response.error || "회원가입에 실패했습니다.",
          variant: "destructive",
        });
      }
    } catch (error) {
      console.error("Registration error:", error);
      toast({
        title: "오류 발생",
        description: "회원가입 중 오류가 발생했습니다.",
        variant: "destructive",
      });
    }
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <Id
            loginId={formData.loginId}
            setLoginId={(value) => updateFormData("loginId", value)}
            setIdValidated={setIdValidated} // ID 검증 상태 설정 함수 전달
          />
        );
      case 2:
        return (
          <Password
            password={formData.password}
            setPassword={(value) => updateFormData("password", value)}
            setPasswordValidated={setPasswordValidated}
          />
        );
      case 3:
        return (
          <PasswordConfirm
            password={formData.password}
            passwordConfirm={formData.passwordConfirm}
            setPasswordConfirm={(value) =>
              updateFormData("passwordConfirm", value)
            }
            setPasswordConfirmValidated={setPasswordConfirmValidated}
          />
        );
      case 4:
        return (
          <Name
            name={formData.name}
            setName={(value) => updateFormData("name", value)}
          />
        );
      case 5:
        return (
          <Gender
            gender={formData.gender}
            setGender={(value) => updateFormData("gender", value)}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="fixed inset-0 flex flex-col h-screen pt-16">
      {renderStep()}
      <div className="px-4 mb-4">
        <div className="flex justify-between">
          {[1, 2, 3, 4, 5].map((item) => (
            <div
              key={item}
              className={`h-2 flex-1 mx-1 rounded-full ${
                item <= step ? "bg-primary" : "bg-gray-200"
              }`}
            />
          ))}
        </div>
      </div>
      <div className="flex mt-auto">
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
          onClick={step === 5 ? handleSubmit : handleNext}
          type="button"
          disabled={
            (step === 1 && !idValidated) ||
            (step === 2 && !passwordValidated) ||
            (step === 3 && !passwordConfirmValidated)
          }
          className={`w-full h-12 transition-all duration-300
            ${
              (step === 1 && !idValidated) ||
              (step === 2 && !passwordValidated) ||
              (step === 3 && !passwordConfirmValidated)
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-primary text-background hover:bg-primary/90 active:scale-[0.98]"
            }`}
        >
          {step === 5 ? "완료" : "다음"}
        </button>
      </div>
    </div>
  );
}

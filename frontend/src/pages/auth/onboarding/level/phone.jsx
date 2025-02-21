import { useState } from "react";
import { Input } from "@/components/ui/input";

export default function Phone({ phone, setPhone, setPhoneValidated }) {
  const [message, setMessage] = useState("");
  const [isValid, setIsValid] = useState(false);

  // 전화번호 형식 검증
  const validatePhone = (value) => {
    // 숫자만 추출
    const numbers = value.replace(/[^0-9]/g, "");

    // 유효성 검사
    const phoneRegex = /^01([0|1])([0-9]{3,4})([0-9]{4})$/;
    const isValidPhone = phoneRegex.test(numbers);

    if (!value) {
      setMessage("전화번호를 입력해주세요");
      setIsValid(false);
      return false;
    }

    if (isValidPhone) {
      setMessage("사용 가능한 전화번호입니다");
      setIsValid(true);
      return true;
    } else {
      setMessage("올바른 전화번호를 입력해주세요");
      setIsValid(false);
      return false;
    }
  };

  // 전화번호 형식화 (하이픈 추가)
  const formatPhoneNumber = (value) => {
    const numbers = value.replace(/[^0-9]/g, "");

    if (numbers.length <= 3) {
      return numbers;
    } else if (numbers.length <= 7) {
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    } else {
      return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(
        7,
        11
      )}`;
    }
  };

  const handlePhoneChange = (e) => {
    const inputValue = e.target.value;
    const formattedNumber = formatPhoneNumber(inputValue);
    setPhone(formattedNumber);

    const isValidPhone = validatePhone(inputValue);
    setPhoneValidated(isValidPhone);
  };

  const ConditionItem = ({ met, text }) => (
    <li
      className={`flex items-center gap-2 ${
        met ? "text-primary" : "text-gray-500"
      }`}
    >
      <span>{met ? "✓" : "○"}</span>
      {text}
    </li>
  );

  // 전화번호 조건 검사
  const conditions = {
    isStartValid: /^01[0|1]/.test(phone.replace(/[^0-9]/g, "")),
    hasValidLength: phone.replace(/[^0-9]/g, "").length >= 10,
    isNumeric: /^[0-9-]+$/.test(phone),
  };

  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">전화번호</h2>
          <h2 className="text-xl font-bold">를 입력해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">
            연락받으실 전화번호
          </h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            를 입력해주세요
          </h4>
        </div>
      </div>

      <div className="flex flex-col gap-2 mt-6">
        <Input
          type="tel"
          placeholder="010-0000-0000"
          value={phone}
          onChange={handlePhoneChange}
          maxLength={13}
          className={`h-12 transition-all duration-300 focus:scale-[1.02] text-lg ${
            isValid
              ? "border-green-500"
              : phone && !isValid
              ? "border-red-500"
              : ""
          }`}
        />

        {message && (
          <div
            className={`text-sm flex items-center gap-1 ${
              isValid ? "text-green-500" : "text-red-500"
            }`}
          >
            <span>{isValid ? "✓" : "✗"}</span>
            <span>{message}</span>
          </div>
        )}

        <div className="mt-2 space-y-1">
          <p className="text-sm text-gray-500">전화번호 조건:</p>
          <ul className="space-y-1 text-sm">
            <ConditionItem
              met={conditions.isStartValid}
              text="010, 011로 시작"
            />
            <ConditionItem
              met={conditions.hasValidLength}
              text="10-11자리 숫자"
            />
            <ConditionItem met={conditions.isNumeric} text="숫자만 입력" />
          </ul>
        </div>
      </div>
    </main>
  );
}

// level/password.jsx
import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";

export default function Password({
  password,
  setPassword,
  setPasswordValidated,
}) {
  const [message, setMessage] = useState("");
  const [isValid, setIsValid] = useState(false);
  const [conditions, setConditions] = useState({
    hasMinLength: false,
    hasNumber: false,
    hasSpecialChar: false,
    hasLetter: false,
  });

  // 비밀번호 유효성 검사 함수
  const validatePassword = (value) => {
    const newConditions = {
      hasMinLength: value.length >= 8,
      hasNumber: /\d/.test(value),
      hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(value),
      hasLetter: /[a-zA-Z]/.test(value),
    };

    setConditions(newConditions);

    // 모든 조건이 충족되었는지 확인
    const isAllValid = Object.values(newConditions).every(
      (condition) => condition
    );

    if (!value) {
      setMessage("비밀번호를 입력해주세요");
      setIsValid(false);
      return false;
    }

    if (isAllValid) {
      setMessage("사용 가능한 비밀번호입니다");
      setIsValid(true);
      return true;
    } else {
      setMessage("모든 조건을 만족해주세요");
      setIsValid(false);
      return false;
    }
  };

  const handlePasswordChange = (e) => {
    const newPassword = e.target.value;
    setPassword(newPassword);
    const isValidPassword = validatePassword(newPassword);
    setPasswordValidated(isValidPassword);
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

  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">비밀번호</h2>
          <h2 className="text-xl font-bold">를 입력해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">
            사용할 비밀번호
          </h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            를 입력해주세요
          </h4>
        </div>
      </div>

      <div className="flex flex-col gap-2 mt-6">
        <Input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={handlePasswordChange}
          className={`h-12 transition-all duration-300 focus:scale-[1.02] ${
            isValid
              ? "border-green-500"
              : password && !isValid
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
          <p className="text-sm text-gray-500">비밀번호 조건:</p>
          <ul className="space-y-1 text-sm">
            <ConditionItem met={conditions.hasMinLength} text="8자 이상" />
            <ConditionItem met={conditions.hasLetter} text="영문 포함" />
            <ConditionItem met={conditions.hasNumber} text="숫자 포함" />
            <ConditionItem
              met={conditions.hasSpecialChar}
              text="특수문자 포함"
            />
          </ul>
        </div>
      </div>
    </main>
  );
}

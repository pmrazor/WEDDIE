import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";

export default function PasswordConfirm({
  password,
  passwordConfirm,
  setPasswordConfirm,
  setPasswordConfirmValidated,
}) {
  const [message, setMessage] = useState("");
  const [isValid, setIsValid] = useState(false);

  // 비밀번호 확인 유효성 검사 함수
  const validatePasswordConfirm = (confirmValue) => {
    if (!confirmValue) {
      setMessage("비밀번호를 한번 더 입력해주세요");
      setIsValid(false);
      return false;
    }

    if (confirmValue === password) {
      setMessage("비밀번호가 일치합니다");
      setIsValid(true);
      return true;
    } else {
      setMessage("비밀번호가 일치하지 않습니다");
      setIsValid(false);
      return false;
    }
  };

  const handlePasswordConfirmChange = (e) => {
    const newPasswordConfirm = e.target.value;
    setPasswordConfirm(newPasswordConfirm);
    const isValidPasswordConfirm = validatePasswordConfirm(newPasswordConfirm);
    setPasswordConfirmValidated(isValidPasswordConfirm);
  };

  // 패스워드가 변경될 때마다 확인 검증 다시 실행
  useEffect(() => {
    if (passwordConfirm) {
      validatePasswordConfirm(passwordConfirm);
    }
  }, [password]);

  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">비밀번호를 확인</h2>
          <h2 className="text-xl font-bold">해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">
            비밀번호를 한번 더
          </h4>
          <h4 className="text-sm text-gray-500 font-semibold"> 입력해주세요</h4>
        </div>
      </div>
      <div className="flex flex-col gap-2 mt-6">
        <Input
          type="password"
          placeholder="비밀번호 확인"
          value={passwordConfirm}
          onChange={handlePasswordConfirmChange}
          className={`h-12 transition-all duration-300 focus:scale-[1.02] ${
            isValid
              ? "border-green-500"
              : passwordConfirm && !isValid
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
          <p className="text-sm text-gray-500">확인사항:</p>
          <ul className="space-y-1 text-sm">
            <li
              className={`flex items-center gap-2 ${
                isValid ? "text-primary" : "text-gray-500"
              }`}
            >
              <span>{isValid ? "✓" : "○"}</span>
              입력한 비밀번호와 일치
            </li>
          </ul>
        </div>
      </div>
    </main>
  );
}

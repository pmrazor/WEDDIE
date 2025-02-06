import { useState } from "react";
import { Input } from "@/components/ui/input";

export default function Id({ loginId, setLoginId, setIdValidated }) {
  const [isChecking, setIsChecking] = useState(false);
  const [isAvailable, setIsAvailable] = useState(null);
  const [message, setMessage] = useState("");

  const checkDuplication = async () => {
    if (!loginId) {
      setIsAvailable(false);
      setMessage("아이디를 입력해주세요.");
      setIdValidated(false); // ID 검증 실패
      return;
    }

    setIsChecking(true);
    try {
      const response = await fetch(
        `https://7bd5e385-7ad3-4453-9460-9ab5721bee57.mock.pstmn.io/api/user/checkId`
      );
      const data = await response.json();

      if (data.available) {
        setIsAvailable(true);
        setMessage("사용 가능한 아이디입니다.");
        setIdValidated(true); // ID 검증 성공
      } else {
        setIsAvailable(false);
        setMessage("이미 사용 중인 아이디입니다.");
        setIdValidated(false); // ID 검증 실패
      }
    } catch (error) {
      console.error("ID 중복 체크 중 오류 발생:", error);
      setIsAvailable(false);
      setMessage("중복 체크 중 오류가 발생했습니다.");
      setIdValidated(false); // ID 검증 실패
    } finally {
      setIsChecking(false);
    }
  };

  // Input의 onChange 핸들러 수정
  const handleIdChange = (e) => {
    setLoginId(e.target.value);
    setIsAvailable(null);
    setMessage("");
    setIdValidated(false); // 입력값이 변경되면 검증 상태 초기화
  };

  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">아이디</h2>
          <h2 className="text-xl font-bold">를 입력해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">사용할 아이디</h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            를 입력해주세요
          </h4>
        </div>
      </div>

      <div className="flex flex-col gap-2 mt-6">
        <div className="flex gap-2">
          <Input
            placeholder="아이디"
            value={loginId}
            onChange={handleIdChange}
            className={`flex-1 h-12 transition-all duration-300 focus:scale-[1.02] ${
              isAvailable === true
                ? "border-green-500"
                : isAvailable === false
                ? "border-red-500"
                : ""
            }`}
          />
          <button
            onClick={checkDuplication}
            disabled={isChecking}
            className="px-4 h-12 bg-primary text-white rounded-md hover:bg-primary/90 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isChecking ? "확인중..." : "중복확인"}
          </button>
        </div>

        {message && (
          <div
            className={`text-sm flex items-center gap-1 ${
              isAvailable ? "text-green-500" : "text-red-500"
            }`}
          >
            <span>{isAvailable ? "✓" : "✗"}</span>
            <span>{message}</span>
          </div>
        )}
      </div>
    </main>
  );
}

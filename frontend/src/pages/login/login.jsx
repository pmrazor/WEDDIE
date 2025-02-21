import { useState } from "react";
import { Link } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { cn } from "../../lib/utils";
import { useToast } from "@/hooks/use-toast";
import useAuthStore from "@/store/auth";
import instance from "../../lib/axios";

import logoImg from "@/assets/logo.png";
import vecImg from "@/assets/vercel.svg";

export default function Home() {
  const [showEmailLogin, setShowEmailLogin] = useState(false);

  const [loginId, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { toast } = useToast();

  const handleEmailLogin = async (e) => {
    e.preventDefault();
    try {
      const data = await useAuthStore.getState().login(loginId, password);
      console.log("Login response:", data);

      if (data && data.result && data.result.accessToken) {
        toast({
          title: "로그인 성공",
          description: "환영합니다!",
          duration: 2000,
        });

        setTimeout(() => {
          window.location.href = "/";
        }, 500);
      } else {
        throw new Error("Invalid login response");
      }
    } catch (error) {
      console.error("Login error details:", {
        error,
        response: error.response,
        status: error.response?.status,
        message: error.response?.data?.message,
      });

      toast({
        variant: "destructive",
        title: "로그인 실패",
        description:
          error.response?.data?.message ||
          "아이디 또는 비밀번호를 확인해주세요.",
        duration: 2000,
      });
    }
  };
  return (
    <div className="grid grid-rows-[20px_1fr_20px] pt-20 items-center justify-items-center font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-8 row-start-2 items-center sm:items-start">
        <img
          className="object-contain"
          src={logoImg}
          alt="Logo"
          width="140"
          height="38"
        />
        <h1 className="text-4xl sm:text-6xl font-bold text-center sm:text-left">
          WEDDIE
        </h1>
        <a>당신을 위한 AI 웨딩플래너</a>

        <div className="relative w-full min-h-[300px] max-w-xl mx-auto">
          {/* OAuth 로그인 버튼들 */}
          <div
            className={cn(
              "absolute w-full flex gap-4 items-center flex-col transition-all duration-500 ease-in-out",
              showEmailLogin
                ? "opacity-0 scale-95 invisible" // translate 대신 scale 사용
                : "opacity-100 scale-100 visible"
            )}
          >
            <a
              className="w-full rounded-full border border-solid border-transparent transition-all duration-300 flex items-center justify-center bg-[#2D8000] text-background gap-2 hover:bg-[#2DB400] hover:scale-[1.02] active:scale-[0.98] text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              href="http://weddie.ssafy.me:5000/oauth2/authorization/naver"
              onClick={(e) => {
                e.preventDefault();
                window.location.href =
                  "http://weddie.ssafy.me:5000/oauth2/authorization/naver";
              }}
            >
              <img
                className="dark:invert"
                src={vecImg}
                alt="Vercel logomark"
                width={20}
                height={20}
              />
              네이버로 로그인하기
            </a>
            <a
              className="w-full rounded-full border border-solid border-transparent transition-all duration-300 flex items-center justify-center bg-[#FEcE00] text-[#191919] gap-2 hover:bg-[#FEE500] hover:scale-[1.02] active:scale-[0.98] text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              href="http://weddie.ssafy.me:5000/oauth2/authorization/kakao"
              onClick={(e) => {
                e.preventDefault();
                window.location.href =
                  "http://weddie.ssafy.me:5000/oauth2/authorization/kakao";
              }}
            >
              <img
                className="dark:invert"
                src={vecImg}
                alt="Vercel logomark"
                width={20}
                height={20}
              />
              카카오로 로그인하기
            </a>
            <a
              className="w-full rounded-full border border-solid border-black dark:border-white transition-all duration-300 flex items-center justify-center text-foreground dark:text-white gap-2 hover:bg-black hover:text-white hover:scale-[1.02] active:scale-[0.98] text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              href="http://weddie.ssafy.me:5000/oauth2/authorization/google"
              onClick={(e) => {
                e.preventDefault();
                window.location.href =
                  "http://weddie.ssafy.me:5000/oauth2/authorization/google";
              }}
            >
              <img
                className="dark:invert"
                src={vecImg}
                alt="Vercel logomark"
                width={20}
                height={20}
              />
              구글로 로그인하기
            </a>
            <button
              className="w-full rounded-full border border-solid border-transparent transition-all duration-300 flex items-center justify-center bg-primary text-background gap-2 hover:bg-primary/90 hover:scale-[1.02] active:scale-[0.98] text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              onClick={() => setShowEmailLogin(true)}
            >
              기존 계정으로 로그인하기
            </button>
          </div>

          {/* 이메일 로그인 폼 */}
          <div
            className={cn(
              "absolute w-full transition-all duration-500 ease-in-out",
              !showEmailLogin
                ? "opacity-0 scale-95 invisible" // translate 대신 scale 사용
                : "opacity-100 scale-100 visible"
            )}
          >
            <form onSubmit={handleEmailLogin} className="space-y-4">
              <Input
                type="text"
                placeholder="아이디"
                value={loginId}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full h-12 transition-all duration-300 focus:scale-[1.02]"
              />
              <Input
                type="password"
                placeholder="비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full h-12 transition-all duration-300 focus:scale-[1.02]"
              />
              <button
                type="submit"
                className="w-full rounded-full border border-solid border-transparent transition-all duration-300 flex items-center justify-center bg-primary text-background gap-2 hover:bg-primary/90 hover:scale-[1.02] active:scale-[0.98] text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              >
                로그인
              </button>
            </form>
            <button className="w-full mt-12 rounded-full border border-solid border-black dark:border-white transition-all duration-300 flex items-center justify-center text-foreground dark:text-white gap-2 hover:bg-black hover:text-white hover:scale-[1.02] active:scale-[0.98] dark:hover:bg-white dark:hover:text-black text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10">
              <Link to="/register">회원가입</Link>
            </button>
            <button
              className="w-full mt-4 mb-20 rounded-full border border-solid border-black dark:border-white transition-all duration-300 flex items-center justify-center text-foreground dark:text-white gap-2 hover:bg-black hover:text-white hover:scale-[1.02] active:scale-[0.98] dark:hover:bg-white dark:hover:text-black text-sm sm:text-base h-10 sm:h-12 px-2 sm:px-10"
              onClick={() => setShowEmailLogin(false)}
            >
              다른 방식으로 로그인하기
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

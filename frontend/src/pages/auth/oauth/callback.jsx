import { useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import useAuthStore from "@/store/auth";

export default function OAuthCallback() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  const handleOAuthLogin = useCallback(async () => {
    try {
      const params = new URLSearchParams(window.location.search);
      const userData = {
        accessToken: params.get("token"),
        email: params.get("email"),
        name: params.get("name"),
        profileImg: params.get("profileImg"),
      };

      if (userData.accessToken) {
        setAuth(userData.accessToken, {
          email: userData.email,
          name: userData.name,
          profileImg: userData.profileImg,
        });
        navigate("/");
      } else {
        navigate("/login");
      }
    } catch (error) {
      console.error("OAuth 로그인 에러:", error);
      navigate("/login");
    }
  }, [navigate, setAuth]);

  useEffect(() => {
    handleOAuthLogin();
  }, [handleOAuthLogin]);

  return (
    <div className="flex justify-center items-center h-screen">
      <div className="text-lg text-muted-foreground animate-pulse">
        로그인 처리중...
      </div>
    </div>
  );
}

import axios from "axios";
import useAuthStore from "@/store/auth";

const BASE_URL = import.meta.env.VITE_API_URL;

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
});

// 요청 인터셉터
instance.interceptors.request.use(
  (config) => {
    // Zustand store에서 토큰 가져오기
    const accessToken = useAuthStore.getState().accessToken;

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 에러 (인증 실패) 처리
    if (error.response?.status === 401) {
      // 현재 경로가 /login이 아닐 때만 로그아웃 및 리다이렉트 수행
      if (!window.location.pathname.includes("/login")) {
        useAuthStore.getState().logout();
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default instance;

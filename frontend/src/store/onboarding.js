import { create } from "zustand";
import instance from "@/lib/axios";
import useAuthStore from "./auth";

const useOnboardingStore = create((set) => ({
  submitOnboarding: async (formData) => {
    try {
      // 현재 인증된 사용자의 토큰 가져오기
      const accessToken = useAuthStore.getState().accessToken;

      // Authorization 헤더에 토큰을 포함하여 요청
      const response = await instance.post("/api/users/detail", formData);

      // 온보딩 성공 시 유저 정보 업데이트
      if (response.data) {
        useAuthStore.getState().updateUser(response.data);
      }

      return { success: true, data: response.data };
    } catch (error) {
      console.error("Onboarding error:", error);
      return {
        success: false,
        error:
          error.response?.data?.message || "온보딩 정보 입력에 실패했습니다.",
      };
    }
  },
}));

export default useOnboardingStore;

import { create } from "zustand";
import instance from "@/lib/axios";

const useRegisterStore = create((set) => ({
  register: async (formData) => {
    try {
      const response = await instance.post("/api/users", formData);
      return { success: true, data: response.data };
    } catch (error) {
      console.error("Register error:", error);
      return {
        success: false,
        error: error.response?.data?.message || "회원가입에 실패했습니다.",
      };
    }
  },
}));

export default useRegisterStore;

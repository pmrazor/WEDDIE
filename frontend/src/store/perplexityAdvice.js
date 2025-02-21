import instance from "@/lib/axios";
import { persist } from "zustand/middleware";
import { create } from "zustand";

const usePerplexityStore = create(
  persist(
    (set) => {
      const apiCalls = {
        async getEmotionalAdvice() {
          try {
            // const response = await instance.get("/api/timeCapsule");
            const response = await instance.post(`/api/plan/advice/true`);
            // 응답 데이터 형식에 맞게 가공
            const advice = response.data.result;

            return advice;
          } catch (error) {
            console.error("타임캡슐 조회 실패:", error);
            throw error;
          }
        },
        async getPracticalAdvice() {
          try {
            const response = await instance.post(`/api/plan/advice/false`);
            // 응답 데이터 형식에 맞게 가공
            const advice = response.data.result;

            return advice;
          } catch (error) {
            console.error("타임캡슐 조회 실패:", error);
            throw error;
          }
        },
      };

      return {
        emotionalAdvice: null,
        practicalAdvice: null,
        isLoading: false,
        error: null,
        getEmotionalAdvice: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.getEmotionalAdvice();
            set({
              emotionalAdvice: data,
              isLoading: false,
              error: null,
            });
            return data;
          } catch (error) {
            set({
              error: error.message,
              isLoading: false,
              advice: null,
            });
            throw error;
          }
        },

        getPracticalAdvice: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.getPracticalAdvice();
            set({
              practicalAdvice: data,
              isLoading: false,
              error: null,
            });
            return data;
          } catch (error) {
            set({
              error: error.message,
              isLoading: false,
              practicalAdvice: null,
            });
            throw error;
          }
        },
      };
    },
    {
      name: "perplexity-storage",
    }
  )
);

export default usePerplexityStore;

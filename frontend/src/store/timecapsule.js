import instance from "@/lib/axios";
import { persist } from "zustand/middleware";
import { create } from "zustand";

const useTimeCapsuleStore = create(
  persist(
    (set) => {
      const apiCalls = {
        async createTimeCapsule(capsuleData) {
          // FormData 대신 JSON 형식으로 직접 전송
          const response = await instance.post("/api/timeCapsule", capsuleData);
          console.log("타임캡슐 생성 api 호출", response);
          return response.data;
        },
        async getTimeCapsule() {
          const response = await instance.get("/api/timeCapsule");
          console.log("타임캡슐 조회 api 호출", response);
          return response.data;
        },
      };

      return {
        timeCapsules: [],
        currentTimeCapsule: null,
        isLoading: false,
        error: null,

        createTimeCapsule: async (capsuleData) => {
          try {
            set({ isLoading: true });
            // JSON 형식으로 데이터 전송
            const data = await apiCalls.createTimeCapsule({
              planId: capsuleData.planId,
              goodContent: capsuleData.goodContent,
              goodImage: capsuleData.goodImage,
              badContent: capsuleData.badContent,
              badImage: capsuleData.badImage,
              planGrade: capsuleData.planGrade,
            });

            set({
              currentTimeCapsule: data,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        getTimeCapsule: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.getTimeCapsule();
            set({
              timeCapsules: data,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        resetTimeCapsule: () => {
          set({
            timeCapsules: [],
            currentTimeCapsule: null,
            isLoading: false,
            error: null,
          });
        },
      };
    },
    {
      name: "time-capsule-storage",
    }
  )
);

export default useTimeCapsuleStore;

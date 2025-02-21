import instance from "@/lib/axios";
import { persist } from "zustand/middleware";
import { create } from "zustand";

const useTimeCapsuleStore = create(
  persist(
    (set) => {
      const apiCalls = {
        async createTimeCapsule(capsuleData) {
          // FormData 형식으로 변경
          const formData = new FormData();
          formData.append("planId", capsuleData.planId);
          formData.append("goodContent", capsuleData.goodContent);
          formData.append("badContent", capsuleData.badContent);
          formData.append("planGrade", capsuleData.planGrade);

          // 이미지 파일이 있는 경우에만 추가
          if (capsuleData.goodImageFile) {
            formData.append("goodImageFile", capsuleData.goodImageFile);
          }
          if (capsuleData.badImageFile) {
            formData.append("badImageFile", capsuleData.badImageFile);
          }

          const response = await instance.post("/api/timeCapsule", formData, {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          });
          console.log("타임캡슐 생성 api 호출", response);
          return response.data;
        },
        async getTimeCapsule() {
          try {
            const response = await instance.get("/api/timeCapsule");
            // 응답 데이터 형식에 맞게 가공
            const formattedData = response.data.map((capsule) => ({
              timeCapsuleId: capsule.timeCapsuleId,
              planId: capsule.planId,
              userId: capsule.userId,
              goodContent: capsule.goodContent,
              goodImage: capsule.goodImage,
              badContent: capsule.badContent,
              badImage: capsule.badImage,
              planGrade: capsule.planGrade,
            }));

            console.log("타임캡슐 조회 api 호출", formattedData);
            return formattedData;
          } catch (error) {
            console.error("타임캡슐 조회 실패:", error);
            throw error;
          }
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
            const data = await apiCalls.createTimeCapsule({
              planId: capsuleData.planId,
              goodContent: capsuleData.goodContent,
              goodImageFile: capsuleData.goodImage,
              badContent: capsuleData.badContent,
              badImageFile: capsuleData.badImage,
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
              error: null,
            });
            return data;
          } catch (error) {
            set({
              error: error.message,
              isLoading: false,
              timeCapsules: [],
            });
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

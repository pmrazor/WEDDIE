import instance from "@/lib/axios";
import { persist } from "zustand/middleware";
import { create } from "zustand";

const useVirginRoadStore = create(
  persist(
    (set) => {
      // 내부에서 사용할 API 호출 함수들을 먼저 정의
      const apiCalls = {
        async createVirginRoad(plans) {
          const response = await instance.post("/api/virginRoad", { plans });
          console.log("버진로드 생성 api 호출", response);
          return response.data;
        },
        async getRecommendVirginRoad() {
          const response = await instance.get(
            "/api/recommendation/recommendPlans"
          );
          console.log("버진로드 조회 api 호출", response);
          return response.data;
        },
        async getVirginRoad() {
          const response = await instance.get("/api/virginRoad");
          console.log("버진로드 조회 api 호출", response);
          return response.data;
        },
        async deleteVirginRoad() {
          const response = await instance.delete("/api/virginRoad");
          console.log("버진로드 삭제 api 호출", response);
          return response.data;
        },
        async updateVirginRoadPlans(plans) {
          const response = await instance.put("/api/virginRoad", { plans });
          console.log("버진로드 업데이트 api 호출", response.data);
          return response.data;
        },
        async completeVirginRoad() {
          const response = await instance.patch("/api/virginRoad");
          console.log("버진로드 완료 api 호출", response.data);
          return response.data;
        },
      };

      // store 객체 반환
      return {
        result: null,
        virginRoad: null,
        plans: [],
        couple: null,
        isLoading: false,
        error: null,

        // 버진로드 생성
        createVirginRoad: async (plans) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.createVirginRoad(plans);
            set({
              result: data.body,
              virginRoad: data.id,
              plans: data.plans,
              couple: data.couple,
              isLoading: false,
            });

            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // 버진로드 조회
        getVirginRoad: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.getVirginRoad();
            const result = data.body.result;
            set({
              result: result,
              virginRoad: result.id,
              plans: result.plans,
              couple: result.couple,
              isLoading: false,
            });
            return result;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        getRecommendVirginRoad: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.getRecommendVirginRoad();
            set({
              result: data.result,
              virginRoad: data.result.id,
              plans: data.result.plans,
              couple: data.result.couple,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // 버진로드 삭제
        deleteVirginRoad: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.deleteVirginRoad();
            console.log(data);
            const result = data;

            set({
              result: result,
              virginRoad: null,
              plans: [],
              couple: null,
              isLoading: false,
            });
            return result;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // 플랜 수정
        updatePlans: async (plans) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.updateVirginRoadPlans(plans);
            console.log("updatePlans", data);
            set({
              plans: data.plans,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // 버진로드 완료
        completeVirginRoad: async () => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.completeVirginRoad();
            set({
              plans: data.plans,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // 상태 초기화
        resetVirginRoad: () => {
          set({
            result: null,
            virginRoad: null,
            plans: [],
            couple: null,
            isLoading: false,
            error: null,
          });
        },
      };
    },
    {
      name: "virgin-road-storage",
    }
  )
);

export default useVirginRoadStore;

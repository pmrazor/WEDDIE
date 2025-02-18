import instance from "@/lib/axios";
import { persist } from "zustand/middleware";
import { create } from "zustand";

const useCoupleStore = create(
  persist(
    (set) => {
      // Internal API call functions
      const apiCalls = {
        async createCoupleRequest(receiverId, message) {
          const response = await instance.post("/api/couple/request", {
            receiverId,
            message,
          });
          console.log("커플 요청 api 호출", response);
          return response.data;
        },

        async approveCoupleRequest(requestId) {
          const response = await instance.post(
            `/api/couple/request/${requestId}/approved`
          );
          console.log("커플 요청 수락 api 호출", response);
          return response.data;
        },

        async rejectCoupleRequest(requestId) {
          const response = await instance.post(
            `/api/couple/request/${requestId}/reject`
          );
          console.log("커플 요청 거절 api 호출", response);
          return response.data;
        },

        async createCouple(couple) {
          const response = await instance.post("/api/couple", couple);
          console.log("커플 생성 api 호출", response);
          return response.data;
        },

        async getCouple() {
          const response = await instance.get("/api/couple");
          console.log("커플 조회 api 호출", response);
          return response.data;
        },

        async deleteCouple() {
          const response = await instance.delete("/api/couple");
          console.log("커플 삭제 api 호출", response);
          return response.data;
        },
      };

      // Return store object
      return {
        couple: null,
        male: null,
        female: null,
        coupleName: null,
        isLoading: false,
        error: null,

        // Create couple request
        createCoupleRequest: async (receiverId, message) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.createCoupleRequest(
              receiverId,
              message
            );
            set({ isLoading: false });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Approve couple request
        approveCoupleRequest: async (requestId) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.approveCoupleRequest(requestId);
            set({
              couple: data,
              male: data.male,
              female: data.female,
              coupleName: data.coupleName,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Reject couple request
        rejectCoupleRequest: async (requestId) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.rejectCoupleRequest(requestId);
            set({ isLoading: false });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Get couple information
        getCouple: async () => {
          try {
            set({ isLoading: true });
            const response = await apiCalls.getCouple();

            if (!response.isSuccess) {
              throw new Error(
                response.message || "커플 정보를 불러올 수 없습니다."
              );
            }

            const data = response.result;
            set({
              couple: data,
              male: data.male,
              female: data.female,
              coupleName: data.coupleName,
              marriageDate: data.marriageDate,
              loveAnniversary: data.loveAnniversary,
              budget: data.budget,
              isLoading: false,
            });
            return response; // isSuccess를 포함한 전체 응답 반환
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Get couple information
        createCouple: async (couple) => {
          try {
            set({ isLoading: true });
            const data = await apiCalls.createCouple(couple);
            set({
              couple: data,
              male: data.male,
              female: data.female,
              coupleName: data.coupleName,
              isLoading: false,
            });
            return data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Delete couple
        deleteCouple: async () => {
          try {
            set({ isLoading: true });
            await apiCalls.deleteCouple();
            set({
              couple: null,
              male: null,
              female: null,
              coupleName: null,
              isLoading: false,
            });
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        // Reset store state
        resetCouple: () => {
          set({
            couple: null,
            male: null,
            female: null,
            coupleName: null,
            isLoading: false,
            error: null,
          });
        },
      };
    },
    {
      name: "couple-storage",
    }
  )
);

export default useCoupleStore;

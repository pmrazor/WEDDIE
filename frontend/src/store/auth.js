import { create } from "zustand";
import { persist } from "zustand/middleware";
import instance from "@/lib/axios";

const useAuthStore = create(
  persist(
    (set, get) => {
      // 내부에서 사용할 API 호출 함수들을 먼저 정의
      const apiCalls = {
        async login(loginId, password) {
          const response = await instance.post("/api/auth/login", {
            loginId,
            password,
          });
          return response.data;
        },
        async checkAuth() { // login.id.response 를 넣으면 -> 추가 데이터(response.data)를 준다. 
          const response = await instance.get("/api/auth/me");
          return response.data;
        },
      };

      // store 객체 반환
      return {
        accessToken: null,
        user: null,
        isAuthenticated: false,
        showSuccessToast: false,

        setAuth: (accessToken, userData) =>
          set({
            accessToken,
            user: userData,
            isAuthenticated: true,
            showSuccessToast: true,
          }),

        clearSuccessToast: () =>
          set((state) => ({
            ...state,
            showSuccessToast: false,
          })),

        logout: () => {
          set({
            accessToken: null,
            user: null,
            isAuthenticated: false,
            showSuccessToast: false,
          });
        },

        login: async (loginId, password) => {
          try {
            const data = await apiCalls.login(loginId, password);
            const { accessToken, user } = data;
            get().setAuth(accessToken, user);
            return data;
          } catch (error) {
            throw error;
          }
        },

        checkAuth: async () => {
          try {
            const data = await apiCalls.checkAuth();
            const { user } = data;
            if (user) {
              set((state) => ({
                ...state,
                user,
                isAuthenticated: true,
              }));
            }
            return data;
          } catch (error) {
            get().logout();
            throw error;
          }
        },

        updateUser: (userData) =>
          set((state) => ({
            user: { ...state.user, ...userData },
          })),
      };
    },
    {
      name: "auth-storage",
    }
  )
);

export default useAuthStore;

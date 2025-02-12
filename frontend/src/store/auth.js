import { create } from "zustand";
import { persist } from "zustand/middleware";
import userApi from "@/utils/userApi";
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
          console.log(loginId, password);
          return response.data;
        },
        async checkAuth() {
          const response = await instance.get("/api/auth/me");
          return response.data;
        },
        async getUserDetail() {
          const response = await instance.get("/api/users/detail");
          return response.data;
        },
        async deleteUser() {
          const response = await instance.delete("/api/users");
          return response.data;
        },
      };

      // store 객체 반환
      return {
        accessToken: null,
        user: null,
        isAuthenticated: false,
        showSuccessToast: false,
        gender: "Default",

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
            gender: "Default",
          });
        },

        login: async (loginId, password) => {
          try {
            const data = await apiCalls.login(loginId, password);
            if (data && data.result) {
              const accessToken = data.result.accessToken;
              // 사용자 정보가 없다면 기본값으로 설정
              const userData = {
                email: null,
                name: null,
                profileImg: null,
              };
              get().setAuth(accessToken, userData);
              // 로그인 성공 후 사용자 상세 정보 가져오기
              get().fetchUserDetail();
            }
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
              // 인증 확인 후 사용자 상세 정보 가져오기
              get().fetchUserDetail();
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
        
        deleteUser: async () => {
          const data = await apiCalls.deleteUser()
          return data
        },

        // 새로 추가된 사용자 상세 정보 가져오는 함수
        fetchUserDetail: async () => {
          try {
            const data = await apiCalls.getUserDetail();
            if (data.isSuccess && data.result) {
              const userDetail = data.result;
              console.log("User Detail:", userDetail);
              set((state) => ({
                ...state,
                gender:
                  userDetail.gender === "MALE"
                    ? "Male"
                    : userDetail.gender === "FEMALE"
                    ? "Female"
                    : "Default",
              }));
            }
          } catch (error) {
            console.error("Failed to fetch user detail:", error);
          }
        },
      };
    },
    {
      name: "auth-storage",
    }
  )
);

export default useAuthStore;

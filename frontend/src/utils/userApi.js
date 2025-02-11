// utils/userApi.js
const BASE_URL = import.meta.env.VITE_API_URL;

const getAuthToken = () => {
  const authStorage = localStorage.getItem("auth-storage");
  if (!authStorage) return null;

  try {
    const authData = JSON.parse(authStorage);
    return authData.state.accessToken;
  } catch (error) {
    console.error("Error parsing auth storage:", error);
    return null;
  }
};

const userApi = {
  searchUsers: async (loginId) => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(
      `${BASE_URL}/api/users/search?loginId=${loginId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    const data = await response.json();

    if (!data.isSuccess) {
      throw new Error(data.message || "사용자 검색에 실패했습니다.");
    }

    return data.result.contents;
  },
};

export default userApi;

// api/coupleApi.js
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

const coupleApi = {
  // 커플 상세 정보 조회
  getCoupleDetails: async () => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}/api/couple`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();

    if (!data.isSuccess) {
      throw new Error(data.message || "커플 정보를 불러오는데 실패했습니다.");
    }

    return data.result; // result 내부의 데이터 반환
  },

  // 커플 요청 수락
  approveRequest: async (url) => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}${url}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "요청 처리 중 문제가 발생했습니다.");
    }

    return data;
  },

  // 커플 요청 거절
  rejectRequest: async (url) => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}${url}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "요청 처리 중 문제가 발생했습니다.");
    }

    return data;
  },

  deleteCouple: async () => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}/api/couple`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();

    if (!data.isSuccess) {
      throw new Error(data.message || "커플 삭제에 실패했습니다.");
    }

    return data.result;
  },
  sendCoupleRequest: async (receiverId, message) => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}/api/couple/request`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        receiverId,
        message,
      }),
    });

    const data = await response.json();

    if (!data.isSuccess) {
      throw new Error(data.message || "커플 요청 전송에 실패했습니다.");
    }

    return data.result;
  },
  updateCoupleDetails: async (details) => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    console.log("Sending details:", details); // 디버깅용 로그

    const response = await fetch(`${BASE_URL}/api/couple`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(details),
    });

    const data = await response.json();
    console.log("Response data:", data); // 디버깅용 로그

    if (!data.isSuccess) {
      throw new Error(data.message || "커플 정보 업데이트에 실패했습니다.");
    }

    return data.result;
  },
  addUserToRecommendation: async () => {
    const token = getAuthToken();
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const response = await fetch(`${BASE_URL}/api/recommendation/addUser`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();
    if (!data.isSuccess) {
      throw new Error(data.message || "추천 시스템 등록에 실패했습니다.");
    }

    return data.result;
  },
};

export default coupleApi;

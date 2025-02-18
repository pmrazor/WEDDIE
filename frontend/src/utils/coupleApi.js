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

    console.log("Sending details:", details);

    const response = await fetch(`${BASE_URL}/api/couple`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(details),
    });

    const data = await response.json();
    console.log("Response data:", data);

    // response가 정상적으로 왔고 데이터가 있다면 성공으로 간주
    if (response.ok && data) {
      return {
        isSuccess: true,
        result: data,
        message: "성공입니다.",
      };
    } else {
      throw new Error(data.message || "커플 정보 업데이트에 실패했습니다.");
    }
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

    // 204 상태 코드 체크
    if (response.status === 204) {
      return {
        isSuccess: true,
        message: "추천 시스템에 성공적으로 등록되었습니다.",
      };
    }

    // 204가 아닌 경우 에러 응답 처리
    try {
      const data = await response.json();
      throw new Error(data.message || "추천 시스템 등록에 실패했습니다.");
    } catch (error) {
      throw new Error("추천 시스템 등록에 실패했습니다.");
    }
  },
};

export default coupleApi;

import { Link } from "react-router-dom";
import { useEffect, useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";
import useSSE from "@/hooks/useSse";
import coupleApi from "@/utils/coupleApi";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import useAuthStore from "@/store/auth";
import { Bell } from "lucide-react"; // 알림 아이콘용
import logoImg from "@/assets/logo.png";

const Appbar = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const { toast } = useToast();
  const [notifications, setNotifications] = useState([]);
  const [pendingCount, setPendingCount] = useState(0);

  useEffect(() => {
    const fetchNotifications = async () => {
      if (!isAuthenticated) return;

      try {
        const authStorage = localStorage.getItem("auth-storage");
        const authData = JSON.parse(authStorage);
        const accessToken = authData.state.accessToken;

        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/api/notifications`,
          {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
          }
        );

        if (!response.ok) throw new Error("Failed to fetch notifications");

        const data = await response.json();
        // PENDING 상태인 알림만 필터링
        const pendingNotifications = data.contents.filter(
          (notification) => notification.matchingStatus === "PENDING"
        );
        setNotifications(pendingNotifications);
        setPendingCount(pendingNotifications.length);
      } catch (error) {
        console.error("Error fetching notifications:", error);
      }
    };

    fetchNotifications();
  }, [isAuthenticated]);

  const handleNotification = useCallback((data) => {
    // 새로운 알림이 PENDING 상태일 때만 추가
    if (data.matchingStatus === "PENDING") {
      setNotifications((prev) => [...prev, data]);
      setPendingCount((prev) => prev + 1);
    }
  }, []);

  // SSE 연결
  const { connected } = useSSE(isAuthenticated ? handleNotification : null);

  const handleResponse = async (url, notificationId) => {
    try {
      if (url.includes("approved")) {
        await coupleApi.approveRequest(url);
      } else {
        await coupleApi.rejectRequest(url);
      }

      // 성공 시 해당 알림 제거
      setNotifications((prev) =>
        prev.filter((notification) => notification.id !== notificationId)
      );
      setPendingCount((prev) => prev - 1);

      toast({
        title: "성공",
        description: url.includes("approved")
          ? "커플 요청을 수락했습니다."
          : "커플 요청을 거절했습니다.",
      });
    } catch (error) {
      toast({
        title: "오류",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="border-b shadow bg-white/90 backdrop-blur-sm">
      <div className="max-w-7xl mx-auto px-6 sm:px-4 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <div className="flex-shrink-0">
            <Link to="/" className="flex items-center">
              <img
                src={logoImg}
                alt="Logo"
                className="w-10 h-10 object-contain"
              />
            </Link>
          </div>

          {/* 메뉴 아이템들 */}
          <div className="hidden md:block">
            <div className="flex items-center space-x-8">
              <Link to="/menu1" className="hover:text-gray-300 text-sm">
                메뉴 1
              </Link>
              <Link to="/menu2" className="hover:text-gray-300 text-sm">
                메뉴 2
              </Link>
              <Link to="/menu3" className="hover:text-gray-300 text-sm">
                메뉴 3
              </Link>
              <Link to="/api-test" className="hover:text-gray-300 text-sm">
                API 테스트
              </Link>
            </div>
          </div>

          {/* 로그인/알림/아바타 섹션 */}
          <div className="flex items-center gap-4">
            {isAuthenticated && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="relative">
                    <Bell className="h-5 w-5" />
                    {pendingCount > 0 && (
                      <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {pendingCount}
                      </span>
                    )}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                  align="end"
                  className="w-96 max-h-[80vh] overflow-y-auto p-2"
                >
                  {notifications.length === 0 ? (
                    <div className="text-center py-4 text-gray-500">
                      대기 중인 커플 요청이 없습니다
                    </div>
                  ) : (
                    notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className="mb-4 p-4 bg-white rounded-lg shadow border"
                      >
                        <div className="flex items-start gap-3">
                          <Avatar className="w-12 h-12">
                            <AvatarImage
                              src={
                                notification.sender.profileImg || "/avatar.png"
                              }
                            />
                            <AvatarFallback>
                              {notification.sender.name[0]}
                            </AvatarFallback>
                          </Avatar>
                          <div className="flex-1">
                            <div className="font-medium mb-1">
                              {notification.sender.name}
                            </div>
                            <p className="text-sm text-gray-600 mb-3">
                              {notification.message}
                            </p>
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                className="flex-1"
                                onClick={() =>
                                  handleResponse(
                                    notification.approvedUrl,
                                    notification.id
                                  )
                                }
                              >
                                수락
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                className="flex-1"
                                onClick={() =>
                                  handleResponse(
                                    notification.rejectedUrl,
                                    notification.id
                                  )
                                }
                              >
                                거절
                              </Button>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            )}

            {isAuthenticated ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Avatar className="cursor-pointer">
                    <AvatarImage
                      src={user?.profileImg || "/avatar.png"}
                      alt="프로필"
                    />
                    <AvatarFallback>{user?.name?.[0] || "U"}</AvatarFallback>
                  </Avatar>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem>
                    <Link to="/mypage" className="w-full">
                      마이 페이지
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Link to="/couple" className="w-full">
                      커플 페이지
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={handleLogout}>
                    로그아웃
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <Button asChild size="sm" className="px-4 py-1">
                <Link to="/login">로그인</Link>
              </Button>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Appbar;

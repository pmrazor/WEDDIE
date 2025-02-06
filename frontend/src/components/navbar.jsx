import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import useAuthStore from "@/store/auth";

import logoImg from "@/assets/logo.png";

const Navbar = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);

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
            </div>
          </div>

          {/* 로그인/아바타 섹션 */}
          <div>
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
                      마이페이지
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

export default Navbar;

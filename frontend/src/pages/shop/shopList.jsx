import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom"; // 추가
import useShopStore from "@/store/shop";
import { ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

// 서비스 타입 한글 매핑
const serviceTypeMap = {
  DRESS_SHOP: "드레스샵",
  STUDIO: "스튜디오",
  WEDDING_HALL: "웨딩홀",
  MAKEUP_STUDIO: "메이크업",
  SNAP: "스냅사진",
  HANBOK: "한복",
  TAILOR_SHOP: "맞춤복",
  INVITATION: "청첩장",
  FACIAL_CARE: "피부관리",
  HONEYMOON: "신혼여행",
  WEDDING_DAY: "웨딩데이",
};

// 가격 포맷 유틸리티 함수
const formatPrice = (amount, currency = "KRW") => {
  // currency 기본값 설정
  if (!amount) {
    return "가격 정보 없음";
  }

  try {
    return new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: currency,
      maximumFractionDigits: 0,
    }).format(amount);
  } catch (error) {
    console.error("Price formatting error:", error);
    return "가격 정보 오류";
  }
};

const ShopList = () => {
  const navigate = useNavigate();
  const { shops, isLoading, error, getShops, currentPage } = useShopStore();
  const [location, setLocation] = useState("지역별");
  const [sort, setSort] = useState("가격 낮은순");

  useEffect(() => {
    console.log("Component mounted, fetching shops...");
    getShops({ page: 0, size: 10 });
  }, []);

  useEffect(() => {
    console.log("Current shops data:", shops);
  }, [shops]);

  if (isLoading) {
    console.log("Loading state...");
    return <div>Loading...</div>;
  }

  if (error) {
    console.log("Error state:", error);
    return <div>Error: {error}</div>;
  }

  console.log("Rendering shops:", shops.contents);

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">
        {shops?.contents?.[0]?.serviceType
          ? `${serviceTypeMap[shops.contents[0].serviceType]} 예약하기`
          : "예약하기"}
      </h1>
      <div className="flex justify-between mb-6">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline">
              {location} <ChevronDown className="ml-2 h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem onClick={() => setLocation("지역별")}>
              지역별
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setLocation("AI 맞춤형")}>
              AI 맞춤형
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline">
              {sort} <ChevronDown className="ml-2 h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem onClick={() => setSort("가격 낮은순")}>
              가격 낮은순
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setSort("가격 높은순")}>
              가격 높은순
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setSort("평점 높은순")}>
              평점 높은순
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setSort("리뷰 많은순")}>
              리뷰 많은순
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setSort("AI 추천")}>
              AI 추천
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      <div className="space-y-4">
        {shops?.contents?.map((shop) => (
          <div
            key={shop.id}
            className="flex items-center border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow cursor-pointer" // 스타일 추가
            onClick={() => navigate(`/shop/${shop.id}`)} // 클릭 이벤트 추가
          >
            <div className="flex-1">
              <h2 className="text-xl font-semibold">{shop.name}</h2>
              <p className="text-gray-600 mt-1">
                {shop.address.city} {shop.address.district}
              </p>
              <p className="text-primary font-bold mt-2">
                {shop.minPrice
                  ? formatPrice(shop.minPrice.amount)
                  : "가격 정보 없음"}
              </p>
            </div>
            <img
              src={shop.mainImageUrl}
              alt={shop.name}
              className="w-24 h-24 object-cover rounded-md ml-4"
            />
          </div>
        ))}
      </div>
      {shops && !shops.isLastPage && (
        <div className="mt-4 text-center">
          <Button
            variant="outline"
            onClick={() => getShops({ page: currentPage + 1, size: 10 })}
          >
            더 보기
          </Button>
        </div>
      )}
    </div>
  );
};

export default ShopList;

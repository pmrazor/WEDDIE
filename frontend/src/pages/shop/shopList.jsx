import React, { useState } from "react";
import { ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const shopData = {
  // 목업 데이터
  type: "studio",
  shops: [
    {
      id: 1,
      name: "A 스토어",
      description: "데이터1",
      price: "₩100,000",
      image: "https://via.placeholder.com/150",
    },
    {
      id: 2,
      name: "B 스토어",
      description: "데이터2",
      price: "₩110,000",
      image: "https://via.placeholder.com/150",
    },
    {
      id: 3,
      name: "C 스토어",
      description: "데이터3",
      price: "₩112,000",
      image: "https://via.placeholder.com/150",
    },
    {
      id: 4,
      name: "D 스토어",
      description: "데이터4",
      price: "₩130,000",
      image: "https://via.placeholder.com/150",
    },
    {
      id: 5,
      name: "E 스토어",
      description: "데이터5",
      price: "₩150,000",
      image: "https://via.placeholder.com/150",
    },
  ],
};

const ShopList = () => {
  const [location, setLocation] = useState("지역별");
  const [sort, setSort] = useState("가격 낮은순");

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">{`${shopData.type} 예약하기`}</h1>
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
        {shopData.shops.map((shop) => (
          <div
            key={shop.id}
            className="flex items-center border rounded-lg p-4 shadow-sm"
          >
            <div className="flex-1">
              <h2 className="text-xl font-semibold">{shop.name}</h2>
              <p className="text-gray-600 mt-1">{shop.description}</p>
              <p className="text-primary font-bold mt-2">{shop.price}</p>
            </div>
            <img
              src={shop.image}
              alt={shop.name}
              className="w-24 h-24 object-cover rounded-md ml-4"
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default ShopList;

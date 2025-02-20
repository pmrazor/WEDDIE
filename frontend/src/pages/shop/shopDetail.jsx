import "@/style/page/shopDetail.css";
import React, { useEffect, useState, useMemo } from "react";
import { useParams } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Card, CardContent } from "@/components/ui/card";
import {
  Bookmark,
  Phone,
  Instagram,
  Globe,
  MessageCircle,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import useShopStore from "@/store/shop";

const ShopDetail = () => {
  const { shopId } = useParams();
  const {
    currentShop,
    selectedItem,
    selectedOptions,
    getShopDetail,
    selectItem,
    updateOptionQuantity,
    calculateTotalPrice,
    isLoading,
  } = useShopStore();

  const [bookmarked, setBookmarked] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [isHovering, setIsHovering] = useState(null);

  useEffect(() => {
    if (shopId) {
      getShopDetail(shopId);
    }
  }, [shopId]);

  const handleTimeSelect = (time) => {
    setSelectedTime(time);
  };

  const getGenderFromLocalStorage = () => {
    const authStorage = localStorage.getItem("auth-storage");
    if (authStorage) {
      const { state } = JSON.parse(authStorage);
      return state.gender;
    }
    return null;
  };

  const getColorByGender = () => {
    const gender = getGenderFromLocalStorage();
    return gender === "Female" ? "#FFC0CC" : "#B3D8FF";
  };

  const [themeColor, setThemeColor] = useState(getColorByGender());

  useEffect(() => {
    setThemeColor(getColorByGender());
  }, []);

  const generateTimeSlots = useMemo(() => {
    if (!selectedDate) return { morning: [], afternoon: [] };

    const slots = { morning: [], afternoon: [] };
    const startTime = new Date(selectedDate);

    // 요일 가져오기 수정
    const days = [
      "SUNDAY",
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY",
      "THURSDAY",
      "FRIDAY",
      "SATURDAY",
    ];
    const dayOfWeek = days[startTime.getDay()];

    if (currentShop?.businessHours?.weeklySchedule[dayOfWeek]) {
      const schedule = currentShop.businessHours.weeklySchedule[dayOfWeek];
      const [openHour, openMinute] = schedule.openTime.split(":").map(Number);
      const [closeHour, closeMinute] = schedule.closeTime
        .split(":")
        .map(Number);

      startTime.setHours(openHour, openMinute, 0);
      const endTime = new Date(selectedDate);
      endTime.setHours(closeHour, closeMinute, 0);

      while (startTime < endTime) {
        const timeSlot = new Date(startTime);
        if (timeSlot.getHours() < 12) {
          slots.morning.push(timeSlot);
        } else {
          slots.afternoon.push(timeSlot);
        }
        startTime.setMinutes(startTime.getMinutes() + 30);
      }
    }

    return slots;
  }, [selectedDate, currentShop]);
  const renderTimeSlots = (slots) => (
    <div className="grid grid-cols-4 gap-2">
      {slots.map((time) => {
        const isSelected =
          selectedTime && selectedTime.getTime() === time.getTime();
        return (
          <Button
            key={time.toISOString()}
            onClick={() => handleTimeSelect(time)}
            variant={isSelected ? "default" : "outline"}
            className="text-sm"
            style={isSelected ? { backgroundColor: themeColor } : {}}
          >
            {time.toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
            })}
          </Button>
        );
      })}
    </div>
  );

  if (isLoading || !currentShop) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 mb-16">
      {/* 구글 지도 */}
      <div className="w-full h-48 mb-4 rounded-lg overflow-hidden">
        <iframe
          width="100%"
          height="100%"
          frameBorder="0"
          style={{ border: 0 }}
          src={`https://www.google.com/maps/embed/v1/place?key=AIzaSyDRROOu0peIxCEhacVQbO5se5IZuXtVhzE&q=${currentShop.address.latitude},${currentShop.address.longitude}&zoom=15`}
          allowFullScreen
        />
      </div>
      {/* 헤더 정보 */}
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">{currentShop.name}</h1>
        <Button
          variant="outline"
          onClick={() => setBookmarked(!bookmarked)}
          className="w-10 h-10 rounded-full p-0 flex items-center justify-center"
        >
          <Bookmark className={`h-5 w-5 ${bookmarked ? "fill-current" : ""}`} />
        </Button>
      </div>

      {/* 설명 및 주소 */}
      <div className="mb-6">
        <p className="text-gray-600">{currentShop.formattedAddress}</p>
        <p className="text-sm text-gray-500">
          영업시간: {currentShop.businessHours.weeklySchedule.MONDAY.openTime} -{" "}
          {currentShop.businessHours.weeklySchedule.MONDAY.closeTime}
        </p>
        <p className="mt-2">{currentShop.description}</p>
      </div>

      {/* 메인 이미지 */}
      <div className="relative mb-6 h-64 overflow-hidden rounded-lg">
        <img
          src={currentShop.mainImageUrl}
          alt={currentShop.name}
          className="w-full h-full object-cover"
        />
      </div>

      {/* 날짜 선택 */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-4 text-center">방문 예약</h2>
        <div className="flex justify-center">
          <DatePicker
            selected={selectedDate}
            onChange={(date) => {
              setSelectedDate(date);
              setSelectedTime(null); // 날짜 변경시 시간 초기화
            }}
            inline
            minDate={new Date()}
            className="react-datepicker"
          />
        </div>
      </div>

      {/* 시간 선택 */}
      {selectedDate && (
        <div className="mb-6">
          <h3 className="text-lg font-semibold mb-2 text-center">
            예약 가능 시간
          </h3>
          <div className="space-y-4">
            <div>
              <h4 className="font-medium mb-2">오전</h4>
              {renderTimeSlots(generateTimeSlots.morning)}
            </div>
            <div>
              <h4 className="font-medium mb-2">오후</h4>
              {renderTimeSlots(generateTimeSlots.afternoon)}
            </div>
          </div>
        </div>
      )}

      {/* 서비스 선택 - 날짜와 시간이 선택된 경우에만 표시 */}
      {selectedDate && selectedTime && (
        <>
          <div className="mb-6">
            <h2 className="text-xl font-semibold mb-4">서비스 선택</h2>
            <div className="space-y-4">
              {currentShop.items.map((item) => (
                <Card
                  key={item.id}
                  className={`cursor-pointer ${
                    selectedItem?.id === item.id ? "border-primary" : ""
                  }`}
                  onClick={() => selectItem(item.id)}
                >
                  <CardContent className="p-4">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-semibold">{item.name}</h3>
                        <p className="text-sm text-gray-600">
                          {item.description}
                        </p>
                      </div>
                      <p className="font-bold">{item.formattedPrice}</p>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* 옵션 선택 */}
          {selectedItem && (
            <Card className="mb-6">
              <CardContent className="pt-6">
                <h3 className="text-lg font-semibold mb-4">추가 옵션</h3>
                <div className="space-y-4">
                  {selectedItem.itemOptions.map((option) => (
                    <div
                      key={option.id}
                      className="flex items-center justify-between"
                    >
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id={`option-${option.id}`}
                          checked={selectedOptions[option.id] > 0}
                          onCheckedChange={(checked) =>
                            updateOptionQuantity(
                              option.id,
                              checked ? (option.required ? 1 : 0) : 0
                            )
                          }
                          style={
                            selectedOptions[option.id] > 0
                              ? {
                                  backgroundColor: themeColor,
                                  borderColor: themeColor,
                                }
                              : {}
                          }
                        />
                        <div>
                          <label
                            htmlFor={`option-${option.id}`}
                            className="text-sm font-medium leading-none"
                          >
                            {option.name}
                          </label>
                          <p className="text-xs text-gray-500">
                            {option.description}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-semibold">
                          {option.formattedPrice}
                        </p>
                        {selectedOptions[option.id] > 0 && (
                          <div className="flex items-center space-x-2 mt-1">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() =>
                                updateOptionQuantity(
                                  option.id,
                                  Math.max(
                                    (selectedOptions[option.id] || 0) - 1,
                                    option.required ? 1 : 0
                                  )
                                )
                              }
                            >
                              -
                            </Button>
                            <span>{selectedOptions[option.id]}</span>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() =>
                                updateOptionQuantity(
                                  option.id,
                                  Math.min(
                                    (selectedOptions[option.id] || 0) + 1,
                                    option.max
                                  )
                                )
                              }
                            >
                              +
                            </Button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {/* 예약 버튼 */}
      {selectedItem && selectedDate && selectedTime && (
        <Button
          className="w-full"
          style={{ backgroundColor: themeColor }}
          onClick={() => {
            console.log("예약 정보:", {
              shopId,
              itemId: selectedItem.id,
              options: selectedOptions,
              date: selectedDate,
              time: selectedTime,
              totalPrice: calculateTotalPrice(),
            });
          }}
        >
          {new Intl.NumberFormat("ko-KR", {
            style: "currency",
            currency: "KRW",
            maximumFractionDigits: 0,
          }).format(calculateTotalPrice())}{" "}
          예약하기
        </Button>
      )}
    </div>
  );
};

export default ShopDetail;

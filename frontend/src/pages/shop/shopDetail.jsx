// import "@/style/page/shopDetail.css";
import React, { useEffect, useState, useMemo } from "react";
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

const StoreDetail = () => {
  const [bookmarked, setBookmarked] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [selectedOptions, setSelectedOptions] = useState([]);
  const [isHovering, setIsHovering] = useState(null);

  const storeData = {
    name: "A 스토어",
    images: [
      "https://via.placeholder.com/800x400?text=Image+1",
      "https://via.placeholder.com/800x400?text=Image+2",
      "https://via.placeholder.com/800x400?text=Image+3",
    ],
    options: [
      { id: 1, title: "옵션 1", description: "옵션 1 설명", price: "₩10,000" },
      { id: 2, title: "옵션 2", description: "옵션 2 설명", price: "₩20,000" },
      { id: 3, title: "옵션 3", description: "옵션 3 설명", price: "₩30,000" },
    ],
  };

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

  // 성별에 따른 색상을 결정하는 함수
  const getColorByGender = () => {
    const gender = getGenderFromLocalStorage();
    return gender === "Female" ? "#FFC0CC" : "#B3D8FF";
  };

  const [themeColor, setThemeColor] = useState(getColorByGender());

  useEffect(() => {
    // 컴포넌트 마운트 시 색상 설정
    setThemeColor(getColorByGender());
  }, []);

  const handleDateSelect = (slotInfo) => {
    setSelectedDate(slotInfo.start);
    setSelectedTime(null);
  };

  const generateTimeSlots = useMemo(() => {
    if (!selectedDate) return { morning: [], afternoon: [] };
    const slots = { morning: [], afternoon: [] };
    const startTime = new Date(selectedDate);
    startTime.setHours(9, 0, 0);
    const endTime = new Date(selectedDate);
    endTime.setHours(18, 0, 0);

    while (startTime < endTime) {
      const timeSlot = new Date(startTime);
      if (timeSlot.getHours() < 12) {
        slots.morning.push(timeSlot);
      } else {
        slots.afternoon.push(timeSlot);
      }
      startTime.setMinutes(startTime.getMinutes() + 30);
    }

    return slots;
  }, [selectedDate]);

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

  const events = [
    {
      start: new Date(2023, 5, 10),
      end: new Date(2023, 5, 10),
      title: "예약 1",
    },
    {
      start: new Date(2023, 5, 15),
      end: new Date(2023, 5, 15),
      title: "예약 2",
    },
  ];

  const handleImageChange = (direction) => {
    if (direction === "next") {
      setCurrentImageIndex((prevIndex) =>
        prevIndex === storeData.images.length - 1 ? 0 : prevIndex + 1
      );
    } else {
      setCurrentImageIndex((prevIndex) =>
        prevIndex === 0 ? storeData.images.length - 1 : prevIndex - 1
      );
    }
  };

  const handleOptionToggle = (optionId) => {
    setSelectedOptions((prev) =>
      prev.includes(optionId)
        ? prev.filter((id) => id !== optionId)
        : [...prev, optionId]
    );
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 mb-16">
      <img
        src="https://via.placeholder.com/800x200?text=Map"
        alt="Map"
        className="w-full mb-4 rounded-lg"
      />
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">{storeData.name}</h1>
        <Button
          variant="outline"
          onClick={() => setBookmarked(!bookmarked)}
          className="w-10 h-10 rounded-full p-0 flex items-center justify-center"
        >
          <Bookmark className={`h-5 w-5 ${bookmarked ? "fill-current" : ""}`} />
        </Button>
      </div>
      <div className="relative mb-4 h-64 overflow-hidden rounded-lg">
        <img
          src={storeData.images[currentImageIndex]}
          alt={`Store image ${currentImageIndex + 1}`}
          className="w-full h-full object-cover"
        />
        {["prev", "next"].map((direction) => (
          <Button
            key={direction}
            className={`absolute ${
              direction === "prev" ? "left-2" : "right-2"
            } top-1/2 transform -translate-y-1/2 w-10 h-10 rounded-full bg-black bg-opacity-50 text-white p-0 flex items-center justify-center transition-colors duration-200`}
            onClick={() => handleImageChange(direction)}
            onMouseEnter={() => setIsHovering(direction)}
            onMouseLeave={() => setIsHovering(null)}
            style={
              isHovering === direction ? { backgroundColor: themeColor } : {}
            }
          >
            {direction === "prev" ? (
              <ChevronLeft className="h-5 w-5" />
            ) : (
              <ChevronRight className="h-5 w-5" />
            )}
          </Button>
        ))}
      </div>
      <div className="flex justify-center space-x-8 mb-4">
        {["Phone", "Instagram", "Globe", "MessageCircle"].map((icon) => (
          <Button
            key={icon}
            variant="ghost"
            className="w-10 h-10 rounded-full p-0 flex items-center justify-center"
          >
            {React.createElement(
              icon === "Phone"
                ? Phone
                : icon === "Instagram"
                ? Instagram
                : icon === "Globe"
                ? Globe
                : MessageCircle,
              { className: "h-5 w-5" }
            )}
          </Button>
        ))}
      </div>

      <h2 className="text-xl font-semibold mb-4 text-center">방문 예약하기</h2>
      <div className="flex justify-center mb-6">
        <DatePicker
          selected={selectedDate}
          onChange={(date) => setSelectedDate(date)}
          inline
          minDate={new Date()}
          className="react-datepicker"
        />
      </div>

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

      {selectedTime && (
        <Card className="mb-6">
          <CardContent className="pt-6">
            <h3 className="text-lg font-semibold mb-4">옵션 선택</h3>
            <div className="space-y-4">
              {storeData.options.map((option) => (
                <div
                  key={option.id}
                  className="flex items-center justify-between"
                >
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id={`option-${option.id}`}
                      checked={selectedOptions.includes(option.id)}
                      onCheckedChange={() => handleOptionToggle(option.id)}
                      style={
                        selectedOptions.includes(option.id)
                          ? {
                              backgroundColor: themeColor,
                              borderColor: themeColor,
                            }
                          : {}
                      }
                    />
                    <label
                      htmlFor={`option-${option.id}`}
                      className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                    >
                      {option.title}
                    </label>
                  </div>
                  <span className="text-sm font-semibold">{option.price}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {selectedTime && selectedOptions.length > 0 && (
        <Button className="w-full" style={{ backgroundColor: themeColor }}>
          예약하기
        </Button>
      )}
    </div>
  );
};

export default StoreDetail;

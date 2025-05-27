import "@/style/page/shopDetail.css";
import React, { useState } from "react";
import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { Button } from "@/components/ui/button";
import {
  Bookmark,
  Phone,
  Instagram,
  Globe,
  MessageCircle,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

moment.locale("ko");
const localizer = momentLocalizer(moment);

// 캘린더용 커스텀 툴바 컴포넌트
const CustomToolbar = (toolbar) => {
  return (
    <div className="rbc-toolbar custom-toolbar">
      <div className="pill-container">
        <button type="button" onClick={() => toolbar.onNavigate("PREV")}>
          ←
        </button>
        <button type="button" onClick={() => toolbar.onNavigate("TODAY")}>
          오늘
        </button>
        <button type="button" onClick={() => toolbar.onNavigate("NEXT")}>
          →
        </button>
      </div>
      <span className="rbc-toolbar-label">{toolbar.label}</span>
      <div className="pill-container">
        <button
          type="button"
          onClick={() => toolbar.onView("month")}
          className={toolbar.view === "month" ? "active" : ""}
        >
          월
        </button>
        <button
          type="button"
          onClick={() => toolbar.onView("week")}
          className={toolbar.view === "week" ? "active" : ""}
        >
          주
        </button>
        <button
          type="button"
          onClick={() => toolbar.onView("day")}
          className={toolbar.view === "day" ? "active" : ""}
        >
          일
        </button>
      </div>
    </div>
  );
};
const StoreDetail = () => {
  const [bookmarked, setBookmarked] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const storeData = {
    name: "A 스토어",
    images: [
      "https://via.placeholder.com/800x400?text=Image+1",
      "https://via.placeholder.com/800x400?text=Image+2",
      "https://via.placeholder.com/800x400?text=Image+3",
    ],
    options: [
      { title: "옵션 1", description: "옵션 1 설명", price: "₩10,000" },
      { title: "옵션 2", description: "옵션 2 설명", price: "₩20,000" },
      { title: "옵션 3", description: "옵션 3 설명", price: "₩30,000" },
    ],
  };

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

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
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
        <Button
          className="absolute left-2 top-1/2 transform -translate-y-1/2 w-10 h-10 rounded-full bg-black bg-opacity-50 text-white p-0 flex items-center justify-center"
          onClick={() => handleImageChange("prev")}
        >
          <ChevronLeft className="h-5 w-5" />
        </Button>
        <Button
          className="absolute right-2 top-1/2 transform -translate-y-1/2 w-10 h-10 rounded-full bg-black bg-opacity-50 text-white p-0 flex items-center justify-center"
          onClick={() => handleImageChange("next")}
        >
          <ChevronRight className="h-5 w-5" />
        </Button>
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
      <h2 className="text-xl font-semibold mb-2">옵션</h2>
      <div className="space-y-2 mb-4">
        {storeData.options.map((option, index) => (
          <div
            key={index}
            className="flex justify-between items-center p-4 border-b last:border-b-0"
          >
            <div>
              <h3 className="font-semibold">{option.title}</h3>
              <p className="text-sm text-gray-600">{option.description}</p>
            </div>
            <span className="font-bold">{option.price}</span>
          </div>
        ))}
      </div>
      <h2 className="text-xl font-semibold mb-2">방문 예약하기</h2>
      <Calendar
        localizer={localizer}
        events={events}
        startAccessor="start"
        endAccessor="end"
        style={{ height: 500 }}
        components={{
          toolbar: CustomToolbar,
        }}
        views={["month", "week", "day"]}
        messages={{
          month: "달",
          week: "주간",
          day: "일별",
          today: "오늘",
          previous: "이전",
          next: "다음",
        }}
        eventPropGetter={(event) => ({
          style: {
            backgroundColor: "#3182CE",
          },
        })}
        dayPropGetter={(date) => {
          const reservationCount = events.filter((event) =>
            moment(event.start).isSame(date, "day")
          ).length;
          return {
            style: {
              backgroundColor:
                reservationCount > 2
                  ? "#FED7D7"
                  : reservationCount > 0
                  ? "#FEEBC8"
                  : "inherit",
            },
          };
        }}
      />
    </div>
  );
};

export default StoreDetail;

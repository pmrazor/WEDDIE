import { useState } from "react";
import { Input } from "@/components/ui/input";

export default function Gender({ gender, setGender }) {
  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">성별</h2>
          <h2 className="text-xl font-bold">을 선택해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">본인의 성별</h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            을 선택해주세요
          </h4>
        </div>
      </div>

      <div className="flex flex-col gap-4 mt-6">
        <label className="relative flex items-center p-4 border rounded-lg cursor-pointer transition-all duration-300 hover:border-primary">
          <input
            type="radio"
            name="gender"
            value="MALE"
            checked={gender === "MALE"}
            onChange={(e) => setGender(e.target.value)}
            className="peer sr-only"
          />
          <div className="w-5 h-5 border-2 rounded-full flex items-center justify-center mr-3 transition-all duration-300 peer-checked:border-primary">
            <div className="w-3 h-3 rounded-full bg-primary scale-0 transition-transform duration-300 peer-checked:scale-100" />
          </div>
          <span className="text-lg font-medium peer-checked:text-primary">
            남성
          </span>
        </label>

        <label className="relative flex items-center p-4 border rounded-lg cursor-pointer transition-all duration-300 hover:border-primary">
          <input
            type="radio"
            name="gender"
            value="FEMALE"
            checked={gender === "FEMALE"}
            onChange={(e) => setGender(e.target.value)}
            className="peer sr-only"
          />
          <div className="w-5 h-5 border-2 rounded-full flex items-center justify-center mr-3 transition-all duration-300 peer-checked:border-primary">
            <div className="w-3 h-3 rounded-full bg-primary scale-0 transition-transform duration-300 peer-checked:scale-100" />
          </div>
          <span className="text-lg font-medium peer-checked:text-primary">
            여성
          </span>
        </label>
      </div>
    </main>
  );
}

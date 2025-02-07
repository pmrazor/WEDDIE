import { useState } from "react";
import { Slider } from "@/components/ui/slider";
import { Input } from "@/components/ui/input";

export default function Age({ age, setAge }) {
  const handleAgeChange = (value) => {
    setAge(value[0]);
  };

  const handleInputChange = (e) => {
    const value = parseInt(e.target.value) || 0;
    if (value >= 14 && value <= 100) {
      setAge(value);
    }
  };
  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">나이</h2>
          <h2 className="text-xl font-bold">를 입력해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">사용자의 나이</h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            를 입력해주세요
          </h4>
        </div>
      </div>

      <div className="flex flex-col gap-6 mt-10">
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-2">
              <Input
                type="number"
                value={age}
                onChange={handleInputChange}
                min={14}
                max={100}
                className="w-20 h-12 text-2xl font-bold text-primary text-center"
              />
              <span className="text-2xl font-bold text-primary">세</span>
            </div>
            <span className="text-sm text-gray-500">14세 ~ 100세</span>
          </div>

          <Slider
            value={[age]}
            onValueChange={handleAgeChange}
            min={14}
            max={100}
            step={1}
            className="w-full"
          />
        </div>
      </div>
    </main>
  );
}

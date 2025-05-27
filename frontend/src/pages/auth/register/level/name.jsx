import { useState } from "react";
import { Input } from "@/components/ui/input";

export default function Name({ name, setName }) {
  return (
    <main className="flex-1 flex flex-col p-6">
      <div className="space-y-2">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">닉네임</h2>
          <h2 className="text-xl font-bold">을 입력해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">사용할 닉네임</h4>
          <h4 className="text-sm text-gray-500 font-semibold">을 정해주세요</h4>
        </div>
      </div>

      <div className="flex flex-col gap-4 mt-6">
        <Input
          placeholder="이름"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full h-12 transition-all duration-300 focus:scale-[1.02]"
        />
      </div>
    </main>
  );
}

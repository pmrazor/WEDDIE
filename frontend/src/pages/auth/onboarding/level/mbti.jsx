import { useState, useEffect } from "react";

export default function Mbti({ mbti, setMbti }) {
  // 각 지표별 선택 상태 관리
  const [selections, setSelections] = useState({
    energy: mbti.charAt(0) || "", // E/I
    information: mbti.charAt(1) || "", // N/S
    decisions: mbti.charAt(2) || "", // T/F
    lifestyle: mbti.charAt(3) || "", // J/P
  });

  // 선택이 변경될 때마다 MBTI 문자열 업데이트
  useEffect(() => {
    const newMbti = Object.values(selections).join("");
    if (newMbti.length === 4) {
      setMbti(newMbti);
    }
  }, [selections]);

  const MbtiOption = ({
    title,
    description,
    value,
    selectedValue,
    onChange,
  }) => (
    <button
      onClick={() => onChange(value)}
      className={`flex flex-col items-center justify-center p-4 rounded-xl border-2 transition-all duration-300 ${
        selectedValue === value
          ? "border-primary bg-primary/10 shadow-lg scale-[1.02]"
          : "border-gray-200 hover:border-primary/30 hover:bg-gray-50"
      }`}
    >
      <span className="text-lg font-bold mb-1">{title}</span>
      <span className="text-sm text-gray-500 text-center">{description}</span>
    </button>
  );

  const MbtiSection = ({ title, description, options, value, onChange }) => (
    <div className="space-y-3">
      <div className="space-y-1">
        <h3 className="font-semibold text-primary">{title}</h3>
        <p className="text-sm text-gray-500">{description}</p>
      </div>
      <div className="grid grid-cols-2 gap-4 px-2">
        {options.map((option) => (
          <MbtiOption
            key={option.value}
            {...option}
            selectedValue={value}
            onChange={onChange}
          />
        ))}
      </div>
    </div>
  );

  const mbtiSections = [
    {
      title: "에너지 방향",
      description: "당신은 어떤 상황에서 에너지를 얻나요?",
      value: selections.energy,
      onChange: (value) =>
        setSelections((prev) => ({ ...prev, energy: value })),
      options: [
        {
          title: "외향형 (E)",
          description: "다른 사람과 교류하며 에너지를 얻어요",
          value: "E",
        },
        {
          title: "내향형 (I)",
          description: "혼자만의 시간을 통해 에너지를 얻어요",
          value: "I",
        },
      ],
    },
    {
      title: "인식 방식",
      description: "정보를 어떤 방식으로 받아들이나요?",
      value: selections.information,
      onChange: (value) =>
        setSelections((prev) => ({ ...prev, information: value })),
      options: [
        {
          title: "직관형 (N)",
          description: "가능성과 의미에 집중해요",
          value: "N",
        },
        {
          title: "감각형 (S)",
          description: "실제 경험과 사실에 집중해요",
          value: "S",
        },
      ],
    },
    {
      title: "판단 기준",
      description: "결정을 내릴 때 무엇을 중요하게 생각하나요?",
      value: selections.decisions,
      onChange: (value) =>
        setSelections((prev) => ({ ...prev, decisions: value })),
      options: [
        {
          title: "사고형 (T)",
          description: "논리적인 분석을 통해 결정해요",
          value: "T",
        },
        {
          title: "감정형 (F)",
          description: "감정과 가치를 고려해서 결정해요",
          value: "F",
        },
      ],
    },
    {
      title: "생활 방식",
      description: "일상생활에서 어떤 방식을 선호하나요?",
      value: selections.lifestyle,
      onChange: (value) =>
        setSelections((prev) => ({ ...prev, lifestyle: value })),
      options: [
        {
          title: "판단형 (J)",
          description: "계획적이고 체계적인 것을 좋아해요",
          value: "J",
        },
        {
          title: "인식형 (P)",
          description: "유연하고 즉흥적인 것을 좋아해요",
          value: "P",
        },
      ],
    },
  ];

  return (
    // overflow-hidden 제거하고 flex-1을 div로 한번 더 감싸기
    <main className="flex flex-col p-6 h-full">
      {/* 헤더 부분 - 고정 */}
      <div className="space-y-2 mb-6">
        <div className="flex">
          <h2 className="text-xl text-primary font-bold">MBTI</h2>
          <h2 className="text-xl font-bold">를 선택해주세요</h2>
        </div>
        <div className="flex">
          <h4 className="text-sm text-primary font-semibold">나의 성격 유형</h4>
          <h4 className="text-sm text-gray-500 font-semibold">
            을 선택해주세요
          </h4>
        </div>
      </div>

      {/* 스크롤 가능한 컨텐츠 영역 */}
      <div className="flex-1 overflow-y-auto min-h-0">
        <div className="space-y-6 pb-6">
          {mbtiSections.map((section, index) => (
            <MbtiSection key={index} {...section} />
          ))}

          {/* 선택된 MBTI 표시 */}
          {mbti.length === 4 && (
            <div className="mt-4 p-4 bg-primary/10 rounded-lg">
              <p className="text-center">
                <span className="text-gray-600">선택된 MBTI: </span>
                <span className="text-primary font-bold text-lg">{mbti}</span>
              </p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}

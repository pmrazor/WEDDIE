import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Star, X } from "lucide-react";
import { useState, useEffect } from "react";
import useTimeCapsuleStore from "@/store/timecapsule";

const StarRating = ({ rating, onRatingChange }) => {
  return (
    <div className="flex gap-2">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          onClick={() => onRatingChange(star)}
          className={`text-2xl ${
            star <= rating ? "text-yellow-400" : "text-gray-300"
          }`}
        >
          <Star className={star <= rating ? "fill-current" : ""} />
        </button>
      ))}
    </div>
  );
};

const TimeCapsuleModal = ({ isOpen, onClose, planId, planName, onSave }) => {
  const { createTimeCapsule } = useTimeCapsuleStore();
  const [timeCapsuleData, setTimeCapsuleData] = useState({
    goodContent: "",
    goodImage: null,
    badContent: "",
    badImage: null,
    planGrade: 0,
  });
  const [goodImagePreview, setGoodImagePreview] = useState(null);
  const [badImagePreview, setBadImagePreview] = useState(null);

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

  // 색상 상태 추가
  const [themeColor, setThemeColor] = useState(getColorByGender());

  useEffect(() => {
    // 컴포넌트 마운트 시 색상 설정
    setThemeColor(getColorByGender());
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // planId를 함께 보냄
      await createTimeCapsule({
        ...timeCapsuleData,
        planId: planId,
      });
      onClose();
    } catch (error) {
      console.error("타임캡슐 생성 실패:", error);
    }
  };

  const handleImageChange = (e, type) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        if (type === "good") {
          setGoodImagePreview(reader.result);
          setTimeCapsuleData((prev) => ({ ...prev, goodImage: file }));
        } else {
          setBadImagePreview(reader.result);
          setTimeCapsuleData((prev) => ({ ...prev, badImage: file }));
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const handleClose = () => {
    setTimeCapsuleData({
      goodContent: "",
      goodImage: null,
      badContent: "",
      badImage: null,
      planGrade: 0,
    });
    setGoodImagePreview(null);
    setBadImagePreview(null);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent
        className="
    fixed left-[50%] top-[50%] translate-x-[-50%] translate-y-[-50%]
    w-[calc(100%-2rem)] sm:w-[600px]
    max-h-[80vh]
    overflow-y-auto
    rounded-2xl
    bg-white
    p-6
    shadow-lg
    border border-gray-200
  "
      >
        {/* 내부 컨텐츠를 중앙 정렬하고 반응형으로 만듦 */}
        <div className="w-full max-w-[500px] mx-auto grid gap-6 py-4">
          <div className="text-xl font-semibold text-center">
            {planName} 타임캡슐
          </div>

          {/* 좋았던 점 섹션 */}
          <div className="w-full space-y-2">
            <Label className="text-base">좋았던 점</Label>
            <Textarea
              value={timeCapsuleData.goodContent}
              onChange={(e) =>
                setTimeCapsuleData((prev) => ({
                  ...prev,
                  goodContent: e.target.value,
                }))
              }
              placeholder="좋았던 점을 적어주세요"
              className="w-full min-h-[100px] text-sm"
            />
            <Label className="block text-base">사진 추가</Label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => handleImageChange(e, "good")}
              className="w-full text-sm"
            />
            {goodImagePreview && (
              <div className="w-full relative">
                <img
                  src={goodImagePreview}
                  alt="Good moment preview"
                  className="w-full h-auto max-h-40 object-cover rounded-md"
                />
                <Button
                  variant="ghost"
                  size="sm"
                  className="absolute top-2 right-2 p-1 bg-white/80"
                  onClick={() => {
                    setGoodImagePreview(null);
                    setTimeCapsuleData((prev) => ({
                      ...prev,
                      goodImage: null,
                    }));
                  }}
                >
                  <X className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>

          {/* 아쉬웠던 점 섹션 */}
          <div className="w-full space-y-2">
            <Label className="text-base">아쉬웠던 점</Label>
            <Textarea
              value={timeCapsuleData.badContent}
              onChange={(e) =>
                setTimeCapsuleData((prev) => ({
                  ...prev,
                  badContent: e.target.value,
                }))
              }
              placeholder="아쉬웠던 점을 적어주세요"
              className="w-full min-h-[100px] text-sm"
            />
            <Label className="block text-base">사진 추가</Label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => handleImageChange(e, "bad")}
              className="w-full text-sm"
            />
            {badImagePreview && (
              <div className="w-full relative">
                <img
                  src={badImagePreview}
                  alt="Bad moment preview"
                  className="w-full h-auto max-h-40 object-cover rounded-md"
                />
                <Button
                  variant="ghost"
                  size="sm"
                  className="absolute top-2 right-2 p-1 bg-white/80"
                  onClick={() => {
                    setBadImagePreview(null);
                    setTimeCapsuleData((prev) => ({ ...prev, badImage: null }));
                  }}
                >
                  <X className="w-4 h-4" />
                </Button>
              </div>
            )}
          </div>

          {/* 별점 섹션 */}
          <div className="w-full space-y-2">
            <Label className="text-base text-center block">전체 평가</Label>
            <div className="flex justify-center items-center w-full">
              <StarRating
                rating={timeCapsuleData.planGrade}
                onRatingChange={(rating) =>
                  setTimeCapsuleData((prev) => ({ ...prev, planGrade: rating }))
                }
              />
            </div>
          </div>

          {/* 버튼 섹션 */}
          <div className="w-full flex justify-end gap-2 mt-4">
            <Button
              variant="outline"
              onClick={handleClose}
              className="px-4 py-2"
            >
              취소
            </Button>
            <Button
              onClick={() => onSave(timeCapsuleData)}
              className="px-4 py-2"
              style={{ backgroundColor: themeColor }}
            >
              저장
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default TimeCapsuleModal;

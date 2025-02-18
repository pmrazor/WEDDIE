import { useEffect, useState } from "react";
import { useToast } from "@/hooks/use-toast";
import { useNavigate } from "react-router-dom";
import coupleApi from "@/utils/coupleApi";
import { Calendar } from "@/components/ui/calendar";
import { Input } from "@/components/ui/input";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { format } from "date-fns";
import { CalendarIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import CoupleRequestForm from "./CoupleRequestForm";

const CouplePage = () => {
  const [coupleDetails, setCoupleDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const { toast } = useToast();
  const navigate = useNavigate();
  const [budget, setBudget] = useState("");
  const [marriageDate, setmarriageDate] = useState();
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    const fetchCoupleDetails = async () => {
      try {
        const data = await coupleApi.getCoupleDetails();
        setCoupleDetails(data);
        setBudget(data.budget || "");
        setmarriageDate(data.marriageDate ? new Date(data.marriageDate) : null);
      } catch (error) {
        setCoupleDetails(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchCoupleDetails();
  }, []);

  const handleUpdateCoupleDetails = async () => {
    try {
      console.log("marriageDate", marriageDate);
      console.log("format", format(marriageDate, "yyyy-MM-dd"));

      const response = await coupleApi.updateCoupleDetails({
        budget: parseInt(budget),
        marriageDate: marriageDate ? format(marriageDate, "yyyy-MM-dd") : null,
      });

      console.log("API Response:", response); // API 응답 전체 로깅

      if (!response.isSuccess) {
        console.error("Update failed:", response); // 실패 시 상세 정보 로깅
        throw new Error(response.message || "업데이트에 실패했습니다");
      }

      toast({
        title: "커플 정보 업데이트",
        description: "커플 정보가 성공적으로 업데이트되었습니다.",
      });
      setIsEditing(false);
    } catch (error) {
      console.error("Error details:", error); // 에러 상세 정보 로깅
      toast({
        title: "오류 발생",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!coupleDetails) {
    return (
      <div className="max-w-md mx-auto p-6">
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h1 className="text-2xl font-bold text-center mb-6">커플 등록하기</h1>
          <CoupleRequestForm />
        </div>
      </div>
    );
  }

  const handleDeleteCouple = async () => {
    try {
      await coupleApi.deleteCouple();
      toast({
        title: "커플 관계 삭제",
        description: "커플 관계가 성공적으로 삭제되었습니다.",
      });
      navigate("/"); // 메인 페이지로 이동
    } catch (error) {
      toast({
        title: "오류 발생",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  const handleAddToRecommendation = async () => {
    try {
      await coupleApi.addUserToRecommendation();
      toast({
        title: "추천 시스템 등록 완료",
        description: "추천 시스템에 성공적으로 등록되었습니다.",
      });
    } catch (error) {
      toast({
        title: "오류 발생",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 relative min-h-screen">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-center mb-8 text-primary">
          {coupleDetails.coupleName}
        </h1>

        <div className="grid grid-cols-2 gap-8 mb-8">
          <div className="text-center">
            <h2 className="text-lg font-semibold mb-2">신랑</h2>
            <p className="text-gray-700">{coupleDetails.male.name}</p>
          </div>
          <div className="text-center">
            <h2 className="text-lg font-semibold mb-2">신부</h2>
            <p className="text-gray-700">{coupleDetails.female.name}</p>
          </div>
        </div>

        <div className="space-y-6 mt-8">
          {isEditing ? (
            <>
              <div className="space-y-2">
                <label className="text-sm font-medium">웨딩 예산</label>
                <Input
                  type="number"
                  value={budget}
                  onChange={(e) => setBudget(e.target.value)}
                  placeholder="예산을 입력하세요"
                  className="w-full"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">결혼 예정일</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className={cn(
                        "w-full justify-start text-left font-normal",
                        !marriageDate && "text-muted-foreground"
                      )}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {marriageDate
                        ? format(marriageDate, "PPP")
                        : "날짜를 선택하세요"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0">
                    <Calendar
                      mode="single"
                      selected={marriageDate}
                      onSelect={setmarriageDate}
                      initialFocus
                    />
                  </PopoverContent>
                </Popover>
              </div>

              <div className="flex gap-2 justify-end">
                <Button variant="outline" onClick={() => setIsEditing(false)}>
                  취소
                </Button>
                <Button onClick={handleUpdateCoupleDetails}>저장</Button>
              </div>
            </>
          ) : (
            <>
              <div className="flex justify-between items-center py-2 border-b">
                <span className="font-medium">웨딩 예산</span>
                <span>
                  {budget ? `${budget.toLocaleString()} 원` : "미설정"}
                </span>
              </div>
              <div className="flex justify-between items-center py-2 border-b">
                <span className="font-medium">결혼 예정일</span>
                <span>
                  {marriageDate ? format(marriageDate, "PPP") : "미설정"}
                </span>
              </div>
              <div className="flex flex-col gap-2 mt-4">
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => setIsEditing(true)}
                >
                  정보 수정하기
                </Button>
                <Button
                  variant="secondary"
                  className="w-full"
                  onClick={handleAddToRecommendation}
                >
                  추천 시스템 등록하기
                </Button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* 커플 삭제 버튼 및 확인 다이얼로그 */}
      <div className="absolute bottom-6 right-6 mb-20">
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button variant="destructive">커플 관계 삭제</Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>커플 관계 삭제</AlertDialogTitle>
              <AlertDialogDescription>
                정말로 커플 관계를 삭제하시겠습니까? 이 작업은 되돌릴 수
                없습니다.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>취소</AlertDialogCancel>
              <AlertDialogAction
                onClick={handleDeleteCouple}
                className="bg-red-500 hover:bg-red-600"
              >
                삭제
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    </div>
  );
};

export default CouplePage;

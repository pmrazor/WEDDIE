import { useEffect, useState } from "react";
import { useToast } from "@/hooks/use-toast";
import { useNavigate } from "react-router-dom";
import coupleApi from "@/utils/coupleApi";
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
import { Button } from "@/components/ui/button";

const CouplePage = () => {
  const [coupleDetails, setCoupleDetails] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const { toast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCoupleDetails = async () => {
      try {
        const data = await coupleApi.getCoupleDetails();
        setCoupleDetails(data);
      } catch (error) {
        setCoupleDetails(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCoupleDetails();
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!coupleDetails) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center text-gray-500">
          <p>커플 정보를 찾을 수 없습니다.</p>
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

  return (
    <div className="max-w-2xl mx-auto p-6 relative min-h-screen">
      <div className="bg-white rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold text-center mb-8 text-primary">
          {coupleDetails.coupleName}
        </h1>

        <div className="grid grid-cols-2 gap-8">
          <div className="text-center">
            <h2 className="text-lg font-semibold mb-2">신랑</h2>
            <p className="text-gray-700">{coupleDetails.male.name}</p>
          </div>

          <div className="text-center">
            <h2 className="text-lg font-semibold mb-2">신부</h2>
            <p className="text-gray-700">{coupleDetails.female.name}</p>
          </div>
        </div>
      </div>

      {/* 커플 삭제 버튼 및 확인 다이얼로그 */}
      <div className="absolute bottom-6 right-6">
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

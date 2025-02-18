// components/CoupleRequestForm.jsx
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/hooks/use-toast";
import coupleApi from "@/utils/coupleApi";
import userApi from "@/utils/userApi";

const CoupleRequestForm = () => {
  const [searchValue, setSearchValue] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [message, setMessage] = useState("");
  const [isSearching, setIsSearching] = useState(false);
  const { toast } = useToast();

  // 디바운스 검색
  useEffect(() => {
    const timer = setTimeout(async () => {
      if (searchValue.trim()) {
        setIsSearching(true);
        try {
          const results = await userApi.searchUsers(searchValue);
          setSearchResults(results);
        } catch (error) {
          toast({
            title: "검색 오류",
            description: error.message,
            variant: "destructive",
          });
          setSearchResults([]);
        } finally {
          setIsSearching(false);
        }
      } else {
        setSearchResults([]);
      }
    }, 500); // 500ms 디바운스

    return () => clearTimeout(timer);
  }, [searchValue, toast]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedUser) {
      toast({
        title: "입력 오류",
        description: "상대방을 선택해주세요.",
        variant: "destructive",
      });
      return;
    }

    try {
      await coupleApi.sendCoupleRequest(selectedUser.id, message);
      toast({
        title: "요청 전송 완료",
        description: "커플 요청이 성공적으로 전송되었습니다.",
      });
      setSelectedUser(null);
      setMessage("");
      setSearchValue("");
      setSearchResults([]);
    } catch (error) {
      toast({
        title: "오류 발생",
        description: error.message,
        variant: "destructive",
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">상대방 검색</label>
        <div className="relative">
          <Input
            type="text"
            value={searchValue}
            onChange={(e) => {
              setSearchValue(e.target.value);
              setSelectedUser(null);
            }}
            placeholder="상대방의 아이디를 입력하세요..."
            className="w-full"
          />
          {/* 검색 결과 드롭다운 */}
          {searchResults.length > 0 && searchValue && !selectedUser && (
            <div className="absolute z-10 w-full mt-1 bg-white rounded-md shadow-lg border max-h-60 overflow-auto">
              {searchResults.map((user) => (
                <div
                  key={user.id}
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => {
                    setSelectedUser(user);
                    setSearchValue(`${user.name} (${user.loginId})`);
                  }}
                >
                  {user.name} ({user.loginId})
                </div>
              ))}
            </div>
          )}
        </div>
        {isSearching && <div className="text-sm text-gray-500">검색 중...</div>}
      </div>

      <div>
        <label
          htmlFor="message"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          커플 이름
        </label>
        <Textarea
          id="message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="커플 이름을 적어주세요"
          required
        />
      </div>

      <Button
        type="submit"
        className="w-full"
        disabled={!selectedUser || !message.trim()}
      >
        커플 요청 보내기
      </Button>
    </form>
  );
};

export default CoupleRequestForm;

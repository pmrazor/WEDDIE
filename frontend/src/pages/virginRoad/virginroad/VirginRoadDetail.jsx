import Button from "@/components/common/Button";
import "@/style/page/VirginRoadDetail.css";
import { useNavigate } from "react-router-dom";
import notice from "@/assets/VirginRoadDetail/notice.png";
import more from "@/assets/VirginRoadDetail/more.svg";
import NoticeCard from "@/components/common/cards/NoticeCard";
import useAuthStore from "@/store/auth";
import CheckListItem from "@/components/CheckListItem";
import { colors } from "@/constants/colors";
import { Radius } from "lucide-react";
import instance from "@/lib/axios";
import { useEffect, useState } from "react";
import ReactMarkdown from "react-markdown";
import { useParams } from "react-router-dom";

const VirginRoadDetail = () => {
  const [planData, setPlanData] = useState(null);
  const [adviceData, setAdviceData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { accessToken } = useAuthStore();
  const { planId } = useParams();
  const nav = useNavigate();
  const [isAddingTodo, setIsAddingTodo] = useState(false);
  const [newTodoContent, setNewTodoContent] = useState("");
  const [newTodoTogether, setNewTodoTogether] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const planResponse = await instance.get(`/api/plan/${planId}`);
        setPlanData(planResponse.data.result);

        if (planResponse.data.result.serviceType) {
          // const adviceResponse = await instance.post(`/api/plan/advice/${planResponse.data.result.serviceType}`);
          setAdviceData(adviceResponse.data);
        }

        setError(null);
      } catch (error) {
        console.error("Error fetching data:", error);
        setError("데이터를 불러오는 데 실패했습니다. 다시 시도해 주세요.");
      } finally {
        setLoading(false);
      }
    };

    if (accessToken && planId) {
      fetchData();
    }
  }, [accessToken, planId]);

  const handleToggleTodo = async (todoId) => {
    try {
      const currentTodo = planData.todos.find((todo) => todo.todoId === todoId);
      const updatedIsCompleted = !currentTodo.completed;

      await instance.put(`/api/plan/${planId}/todo/${todoId}`, {
        isCompleted: updatedIsCompleted,
      });

      setPlanData((prevData) => ({
        ...prevData,
        todos: prevData.todos.map((todo) =>
          todo.todoId === todoId ? { ...todo, completed: updatedIsCompleted } : todo
        ),
      }));
    } catch (error) {
      console.error("Error toggling todo:", error);
    }
  };

  const toggleAddingTodo = () => {
    setIsAddingTodo((prev) => !prev)
  }

  const handleAddTodo = async () => {
    if (!newTodoContent.trim()) {
      alert("할 일 내용을 입력해주세요.");
      return;
    }
    try {
      await instance.post(`/api/plan/${planId}/todo`, {
        content: newTodoContent,
        together: newTodoTogether,
      });

      // Fetch updated todo list
      const response = await instance.get(`/api/plan/${planId}/todo`);
      setPlanData((prevData) => ({
        ...prevData,
        todos: response.data,
      }));

      // 입력 필드 초기화
      setNewTodoContent("");
      setNewTodoTogether(false);
      setIsAddingTodo(false);
    } catch (error) {
      console.error("Error adding todo:", error);
      alert("할 일 추가에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const handleDeleteTodo = async (todoId) => {
    try {
      await instance.delete(`/api/plan/${planId}/todo/${todoId}`);

      // 삭제 후 todo 목록 업데이트
      setPlanData((prevData) => ({
        ...prevData,
        todos: prevData.todos.filter((todo) => todo.todoId !== todoId),
      }));

      alert("할 일이 성공적으로 삭제되었습니다.");
    } catch (error) {
      console.error("Error deleting todo:", error);
      alert("할 일 삭제에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const onclickBack = () => {
    nav("/virginroad");
  };
  const onclickReservation = () => {
    nav("/reservation");
  };
  const onClickMore = () => {
    // 제어 화면이 떠야 한다.
    // 1. 이 항목을 visible하게 할지 선택하기, 2. 예산 조정하기, 3. 버진로드 종료하기, 4. 이 로드는 패스하기
  };

  const nowNode = planData ? planData.shopName : "웨딩홀 예약";

  const checkMokData = planData
    ? planData.todos.map((todo) => ({
        id: todo.todoId,
        content: todo.content,
        isCompleted: todo.completed.toString(),
      }))
    : [];

  const serviceType = planData?.serviceType;

  const gender = useAuthStore((state) => state.gender);
  console.log("Gender :" + gender);
  // const gender = "Male";

  // const gender = useAuthStore((state) => state.gender)
  // const gender = "Female"

  const genderBack = () => {
    switch (gender) {
      case "Male":
        return "male";
      case "Female":
        return "female";
      default:
        return "default";
    }
  };

  // budget 예시 데이터
  const ages = "30대 초반";
  const averageBudget = 750000;
  const budgetPercentage = "15%";

  return (
    <div className="VirginRoadDetail">
      <div className="header">
        <Button onClick={onclickBack} text={"<"} />
        <div className="text">
          {/* 이 로드의 이름 들어가기 */}
          {`${serviceType}하기`}
        </div>
        {/* <Button 
          onClick={onClickMore}
          text={"..."}
        /> */}
        <div className="hamburger">
          <img src={more} alt="more" onClick={onClickMore} />
        </div>
      </div>
      {/* <div className="budget" style={{backgroundImage: `url(${w_budget_background})`}}> */}
      <div className={`budget ${genderBack()}`}>
        <div className="text-area" style={{ color: colors.genderColors[gender].PRIMARY_EXTRADEEP }}>
          <div className="head-text1">{`우리와 같은 ${ages} 부부들은`}</div>
          <div className="head-text2">{`${serviceType}에`}</div>
          <div className="head-text3">{`평균 ${averageBudget}원,`}</div>
          <div className="head-text4">{`전체 예산의 ${budgetPercentage}를 썼어요.`}</div>
        </div>
      </div>
      <div className="tips">
        <NoticeCard
          icon={notice}
          headText={"그거 아세요?"}
          subText1={`${serviceType} 로드를 걷는 ${
            gender === "Male" ? "예랑이" : gender === "Female" ? "예신이" : "부부"
          }라면`}
          subText2={
            adviceData ? <ReactMarkdown>{adviceData.result}</ReactMarkdown> : `놓치지 말아야 할 꿀팁을 전해드려요 `
          }
          backgroundColor={colors.genderColors[gender].PRIMARY_DEEP}
        />
      </div>
      <div className="checklist-area" style={{ backgroundColor: colors.genderColors[gender].PRIMARY_LIGHT }}>
        <div className="checklist-header">
          <Button 
            text={"체크리스트"}
            style={{color : colors.BLACK}}
          />
          <Button
            onClick={toggleAddingTodo}
            text={isAddingTodo ? "돌아가기" : "추가하기"}
            style={{color : colors.BLACK}}
          />
          {/* <button onClick={(prev) => setIsAddingTodo(!prev)}>{isAddingTodo === true ? "돌아가기" : "추가"}</button> */}
        </div>
        {isAddingTodo && (
          <div className="add-todo-form">
            <input 
              type="text"
              value={newTodoContent}
              onChange={(e) => setNewTodoContent(e.target.value)}
              placeholder="새로운 할 일 입력"
            />
            <div className="check-box">
              <div className="check-box-container">
                <input 
                  type="checkbox" 
                  checked={newTodoTogether} 
                  onChange={(e) => setNewTodoTogether(e.target.checked)} 
                />
                <Button 
                  text={"우리 같이 할 일"}
                />
              </div>
              <Button 
                onClick={handleAddTodo}
                text={"추가하기"}
              /> 
            </div>
          </div>
        )}
        <div className="checklist">
          {checkMokData.map((item) => (
            <CheckListItem
              key={item.id}
              id={item.id}
              content={item.content}
              isCompleted={item.isCompleted}
              onToggle={handleToggleTodo}
              onDelete={handleDeleteTodo}
            />
          ))}
        </div>
      </div>
      <Button
        onClick={onclickReservation}
        style={{
          backgroundColor: colors.genderColors[gender].PRIMARY_DEEP,
          borderRadius: "10px",
        }}
        text={`${serviceType} 예약하러 가기`}
        stickyType={"UNDER"}
      />
    </div>
  );
};

export default VirginRoadDetail;

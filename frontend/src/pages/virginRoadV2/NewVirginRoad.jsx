import "@/style/page/NewVirginRoad.css"
import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState, useMemo } from "react";
import { colors } from "@/constants/colors";
import useVirginRoadStore from "@/store/virginroad";
import useTodoStore from "@/store/todo";
import useTimeCapsuleStore from "@/store/timecapsule";
import TimeCapsuleModal from "./TimecapsuleModal";
import useAuthStore from "@/store/auth";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { Plus, Edit2, Trash2, Check, X, Heart } from "lucide-react"; // 아이콘 추가
import { motion, AnimatePresence } from "framer-motion";
import { ChevronDown, ChevronUp } from "lucide-react";

const LoadingAnimation = () => {
  return (
    <motion.div
      className="fixed inset-0 flex flex-col items-center justify-center bg-white/80 backdrop-blur-sm z-50"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <div className="w-64 h-64 mb-8 flex items-center justify-center">
        <div className="flex gap-1">
          {[...Array(5)].map((_, i) => (
            <motion.div
              key={i}
              className="w-3 h-16 bg-primary rounded-full"
              animate={{
                height: ["64px", "24px", "64px"],
              }}
              transition={{
                duration: 1,
                repeat: Infinity,
                delay: i * 0.1,
                ease: "easeInOut",
              }}
            />
          ))}
        </div>
      </div>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{
          opacity: 1,
          y: 0,
          transition: {
            duration: 0.6,
            delay: 1,
          },
        }}
        className="flex flex-col items-center"
      >
        <div className="flex">
          <h2 className="text-xl font-bold text-primary">WEDDIE</h2>
          <h2 className="text-xl font-semibold">가 변경사항을</h2>
        </div>
        <h2 className="text-xl font-semibold">저장하고 있어요!</h2>
        <h2 className="text-xl font-semibold mt-10">잠시만 기다려 주세요!</h2>
      </motion.div>
    </motion.div>
  );
};

const VirginRoad = () => {
  const nav = useNavigate();
  const location = useLocation();
  const { createTimeCapsule } = useTimeCapsuleStore();
  const {
    plans = [],
    getVirginRoad,
    updatePlans,
    isLoading,
  } = useVirginRoadStore();
  const {
    tip,
    todos,
    getTodos,
    createTodo,
    updateTodo,
    deleteTodo,
    toggleTodoCompletion,
    isLoading: isTodoLoading, // isLoading을 isTodoLoading으로 이름 변경하여 가져오기
  } = useTodoStore();
  const [isEditing, setIsEditing] = useState(false);
  const [editablePlans, setEditablePlans] = useState([]);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState(null);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingTodoId, setEditingTodoId] = useState(null);
  const [editContent, setEditContent] = useState("");
  const [isAddingNew, setIsAddingNew] = useState(false);
  const [newTodoContent, setNewTodoContent] = useState("");
  const [newTodoStage, setNewTodoStage] = useState("ON");
  const [newTodoIsTogether, setNewTodoIsTogether] = useState(false);
  const [showTimeCapsule, setShowTimeCapsule] = useState(false);
  const [selectedServiceType, setSelectedServiceType] = useState(null);

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

  const [themeColor, setThemeColor] = useState(getColorByGender());

  const handleSaveTimeCapsule = async (data) => {
    try {
      await createTimeCapsule({
        planId: selectedPlan,
        goodContent: data.goodContent,
        goodImage: data.goodImage,
        badContent: data.badContent,
        badImage: data.badImage,
        planGrade: data.planGrade,
      });
      setShowTimeCapsule(false);
    } catch (error) {
      console.error("Failed to save time capsule:", error);
    }
  };

  

  const [isOpenTodo, setIsOpenTodo] = useState(true)

  const renderTodos = () => {
    const selectedPlanData = plans.find((plan) => plan.planId === selectedPlan);
    if (!selectedPlanData) {
      return <p>이 ID의 플랜을 찾지 못했어요.</p>;
    }

    const serviceType = selectedPlanData.serviceType;
    const selectedTodos = todos[serviceType];

    const renderTodoList = (todos, title) => {
      if (!todos || todos.length === 0) return null;

      return (
        <div 
          className="title"
          style={{backgroundColor : colors.genderColors[gender].PRIMARY_EXTRALIGHT}}
        >
          <div className="container-folder flex justify-between items-center cursor-pointer p-2">
          <div className="text-field" onClick={() => setIsOpenTodo(!isOpenTodo)}>
            <h3 className="text-lg font-semibold mb-2">{title}</h3>
            <button className="p-1">
              {isOpenTodo ? <ChevronUp className="w-4 h-4"/> : <ChevronDown className="w-4 h-4"/> }
            </button>
          </div>
          {isOpenTodo && (
            <ul className="todo-container space-y-2">
            {todos.map((todo) => (
              <li
              key={todo.todoId}
              className="todo-items flex items-center gap-2 p-2 bg-white rounded-lg shadow"
              >
                <input
                  type="checkbox"
                  checked={todo.completed}
                  onChange={() =>
                    handleTodoToggle(selectedPlan, todo.todoId, !todo.completed)
                  }
                  className="shrink-0"
                  />
                {editingTodoId === todo.todoId ? (
                  <div className="flex-1 flex gap-2 items-center min-w-0">
                    <input
                      type="text"
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                      onKeyDown={async (e) => {
                        if (e.key === "Enter" && editContent.trim()) {
                          await handleUpdateTodo(
                            selectedPlan,
                            todo.todoId,
                            editContent
                          );
                          setEditingTodoId(null);
                        }
                      }}
                      className="flex-1 min-w-0 px-2 py-0.5 border rounded-md text-sm h-7"
                      autoFocus
                      />
                    <div className="flex gap-1 shrink-0">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-6 w-6 p-0 shrink-0"
                        onClick={async () => {
                          if (editContent.trim()) {
                            await handleUpdateTodo(
                              selectedPlan,
                              todo.todoId,
                              editContent
                            );
                            setEditingTodoId(null);
                          }
                        }}
                        >
                        <Check className="w-3 h-3 text-green-500" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-6 w-6 p-0 shrink-0"
                        onClick={() => setEditingTodoId(null)}
                        >
                        <X className="w-3 h-3 text-red-500" />
                      </Button>
                    </div>
                  </div>
                ) : (
                  <span className="flex-1 min-w-0 truncate">
                    {todo.content}
                  </span>
                )}
                {isEditMode && editingTodoId !== todo.todoId && (
                  <div className="flex gap-1 shrink-0">
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 w-6 p-0"
                      onClick={() => {
                        setEditingTodoId(todo.todoId);
                        setEditContent(todo.content);
                      }}
                      >
                      <Edit2 className="w-3 h-3" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-6 w-6 p-0"
                      onClick={() =>
                        handleDeleteTodo(selectedPlan, todo.todoId)
                      }
                      >
                      <Trash2 className="w-3 h-3 text-red-500" />
                    </Button>
                  </div>
                )}
              </li>
            ))}
          </ul>
          )}
        </div>
      </div>
      );
    };

    return (
      <div>
        {!selectedTodos ||
        !selectedTodos.todos ||
        (selectedTodos.todos.pre.length === 0 &&
          selectedTodos.todos.on.length === 0 &&
          selectedTodos.todos.post.length === 0) ? (
          <p>아무런 할일이 없습니다! 오른쪽 밑 + 버튼으로 추가해보세요!</p>
        ) : (
          <>
            {renderTodoList(selectedTodos.todos.pre, "전에 할 일")}
            {renderTodoList(selectedTodos.todos.on, "지금 할 일")}
            {renderTodoList(selectedTodos.todos.post, "나중에 할 일")}
          </>
        )}

        {/* 새 Todo 추가 입력 필드 - isAddingNew가 true일 때만 표시 */}
        {isAddingNew && (
          <div className="mt-4 space-y-4">
            <input
              type="text"
              value={newTodoContent}
              onChange={(e) => setNewTodoContent(e.target.value)}
              placeholder="새로운 할 일을 입력하세요"
              className="w-full px-3 py-2 border rounded-md"
            />
            <div className="flex items-center space-x-2">
              <select
                value={newTodoStage}
                onChange={(e) => setNewTodoStage(e.target.value)}
                className="px-2 py-2 border rounded-md text-sm"
              >
                <option value="PRE">전에</option>
                <option value="ON">지금</option>
                <option value="POST">나중에</option>
              </select>
              <Button
                size="sm"
                variant={newTodoIsTogether ? "default" : "outline"}
                onClick={() => setNewTodoIsTogether(!newTodoIsTogether)}
                className="p-1"
              >
                <Heart
                  className={`h-4 w-4 ${
                    newTodoIsTogether ? "fill-current" : ""
                  }`}
                />
              </Button>
              <Button
                size="sm"
                onClick={async () => {
                  if (newTodoContent.trim()) {
                    await handleCreateTodo(selectedPlan, {
                      serviceType: selectedPlanData.serviceType,
                      stageType: newTodoStage,
                      content: newTodoContent,
                      isTogether: newTodoIsTogether,
                    });
                    setIsAddingNew(false);
                    setNewTodoContent("");
                    setNewTodoStage("ON");
                    setNewTodoIsTogether(false);
                  }
                }}
                style={{ backgroundColor: themeColor }}
              >
                저장
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={() => {
                  setIsAddingNew(false);
                  setNewTodoContent("");
                  setNewTodoStage("ON");
                  setNewTodoIsTogether(false);
                }}
              >
                취소
              </Button>
            </div>
          </div>
        )}
      </div>
    );
  };

  useEffect(() => {
    getVirginRoad();
  }, [getVirginRoad]);

  useEffect(() => {
    setThemeColor(getColorByGender());
  }, []);

  useEffect(() => {
    const initializeSelectedPlan = async () => {
      const targetServiceType =
        location.state?.serviceType ||
        new URLSearchParams(location.search).get("serviceType");

      if (targetServiceType && Array.isArray(plans)) {
        const targetPlan = plans.find(
          (plan) => plan.serviceType === targetServiceType
        );
        if (targetPlan && targetPlan.planId !== selectedPlan) {
          setSelectedPlan(targetPlan.planId);
          if (!todos[targetServiceType]) {
            await getTodos(targetServiceType);
          }
        }
      }
    };

    initializeSelectedPlan();
  }, [location, getVirginRoad]);

  useEffect(() => {
    if (Array.isArray(plans)) {
      setEditablePlans([...plans]);
    }
  }, [plans]);

  useEffect(() => {
    if (selectedPlan && todos[selectedPlan]) {
      const allCompleted = isAllTodosCompleted();
    }
  }, [todos, selectedPlan]);

  useEffect(() => {}, [plans, editablePlans]);

  useEffect(() => {
    if (selectedPlan) {
      const selectedPlanData = plans.find(
        (plan) => plan.planId === selectedPlan
      );
      if (selectedPlanData) {
        const serviceType = selectedPlanData.serviceType;
        if (!todos[serviceType]) {
          getTodos(serviceType);
        }
      }
    }
  }, [selectedPlan, plans, todos, getTodos]);

  const gender = useAuthStore((state) => state.gender);
  console.log("Gender :" + gender);
  // 이거 엠티로 나와서 색 지정이 안됨 고쳐야함
  console.log(
    getComputedStyle(document.documentElement).getPropertyValue(
      "--genderColors-Male-PRIMARY_EXTRADEEP"
    )
  );

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

  const handleCancel = () => {
    if (Array.isArray(plans)) {
      setEditablePlans([...plans]);
    }
    setIsEditing(false);
  };

  const displayPlans = useMemo(() => {
    if (!Array.isArray(editablePlans)) return [];
    return isEditing
      ? editablePlans
      : editablePlans.filter((plan) => plan.visible);
  }, [isEditing, editablePlans]);

  const getStepName = (serviceType) => {
    const stepNames = {
      DRESS_SHOP: "드레스샵",
      STUDIO: "스튜디오",
      WEDDING_HALL: "웨딩홀",
      MAKEUP_STUDIO: "메이크업",
      SNAP: "스냅사진",
      HANBOK: "한복",
      TAILOR_SHOP: "맞춤복",
      INVITATION: "청첩장",
      FACIAL_CARE: "피부관리",
      HONEYMOON: "신혼여행",
      WEDDING_DAY: "웨딩데이",
    };
    return stepNames[serviceType] || serviceType;
  };

  const getStatusStyle = (status, visible) => {
    if (isEditing) {
      return visible
        ? "bg-white border-2 border-primary shadow-lg"
        : "bg-gray-100 opacity-50";
    }
    const styles = {
      IN_PROGRESS: "bg-blue-100 hover:bg-blue-200",
      NOT_TIME_CAPSULE: "bg-purple-100 hover:bg-purple-200",
      FINISHED: "bg-green-100 hover:bg-green-200",
      BEFORE_START: "bg-gray-100 hover:bg-gray-200",
    };
    return styles[status] || "bg-gray-100";
  };

  const handleToggleVisible = (planId) => {
    if (!isEditing) return;
    setEditablePlans((prev) =>
      prev.map((plan) =>
        plan.planId === planId ? { ...plan, visible: !plan.visible } : plan
      )
    );
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      await updatePlans(editablePlans);
      await getVirginRoad(); // 저장 후 최신 데이터 다시 불러오기
      setIsEditing(false);
    } catch (error) {
      console.error("버진로드 업데이트 실패:", error);
    } finally {
      setIsSaving(false);
    }
  };

  const handlePlanClick = async (planId) => {
    if (isEditing) {
      handleToggleVisible(planId);
    } else {
      setSelectedPlan(planId);
      const selectedPlanData = plans.find((plan) => plan.planId === planId);
      if (selectedPlanData) {
        const serviceType = selectedPlanData.serviceType;
        if (!todos[serviceType]) {
          try {
            await getTodos(serviceType);
          } catch (error) {
            console.error("Error fetching todos:", error);
            // 에러 처리 로직 (예: 사용자에게 에러 메시지 표시)
          }
        }
      } else {
        console.error("No plan found for planId:", planId);
      }
    }
  };

  const handleTodoToggle = async (serviceType, todoId, newState) => {
    try {
      await toggleTodoCompletion(serviceType, todoId, newState);
    } catch (error) {
      console.error("Todo 토글 실패:", error);
    }
  };

  // Todo 관리 함수들 추가
  const handleDeleteTodo = async (serviceType, todoId) => {
    try {
      await deleteTodo(serviceType, todoId);
    } catch (error) {
      console.error("Todo 삭제 실패:", error);
    }
  };

  const handleUpdateTodo = async (serviceType, todoId, content) => {
    try {
      await updateTodo(serviceType, todoId, content);
    } catch (error) {
      console.error("Todo 수정 실패:", error);
    }
  };

  const handleCreateTodo = async (planId, todoData) => {
    try {
      await createTodo(planId, todoData);
    } catch (error) {
      console.error("Todo 생성 실패:", error);
    }
  };

  const isAllTodosCompleted = () => {
    const selectedPlanData = plans.find((plan) => plan.planId === selectedPlan);
    if (!selectedPlanData) return false;

    // planStatus가 NOT_TIME_CAPSULE일 때만 true 반환
    return selectedPlanData.planStatus === "NOT_TIME_CAPSULE";
  };

  // 떨리는 효과를 위한 애니메이션 설정
  const shakingAnimation = isEditing
    ? {
        rotate: [0, -0.5, 0.5, -0.5, 0.5, 0],
        transition: {
          duration: 0.7,
          repeat: Infinity,
          repeatType: "loop",
          ease: "linear",
        },
      }
    : {};

  return (
    <div className="p-4 mb-16 w-full">
      {isSaving && <LoadingAnimation />}

      <div className="flex justify-between items-center mb-4">
        <Button variant="ghost" className="text-2xl" onClick={() => nav(-1)}>
          &lt;
        </Button>
        {isEditing ? (
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={handleCancel}
              disabled={isSaving}
            >
              취소
            </Button>
            <Button
              variant="default"
              onClick={handleSave}
              disabled={isSaving}
              style={{ backgroundColor: themeColor }}
            >
              저장
            </Button>
          </div>
        ) : (
          <Button variant="outline" onClick={() => setIsEditing(true)}>
            편집
          </Button>
        )}
      </div>

      <ScrollArea className="w-full whitespace-nowrap rounded-md">
        <div className="flex space-x-4 p-4">
          {displayPlans && displayPlans.length > 0 ? (
            displayPlans.map((plan) => (
              <motion.div
                key={plan.planId}
                className="relative"
                animate={shakingAnimation}
                whileHover={isEditing ? { scale: 1.1 } : {}}
                transition={{ duration: 0.1 }}
              >
                <Button
                  variant="ghost"
                  className={cn(
                    "flex-none w-[100px] h-[100px] rounded-xl p-6",
                    "flex flex-col items-center justify-center",
                    "transition-colors duration-200",
                    getStatusStyle(plan.planStatus, plan.visible),
                    isEditing && "cursor-pointer"
                  )}
                  onClick={() => handlePlanClick(plan.planId)}
                  style={
                    isEditing
                      ? {
                          borderColor: themeColor,
                          borderWidth: "2px",
                          borderStyle: "solid",
                        }
                      : {}
                  }
                >
                  <div className="text-center">
                    <h3
                      className={cn(
                        "text-lg font-semibold mb-2",
                        isEditing && !plan.visible && "text-gray-500"
                      )}
                    >
                      {getStepName(plan.serviceType)}
                    </h3>
                  </div>
                </Button>
                <AnimatePresence>
                  {isEditing && plan.visible && (
                    <motion.div
                      initial={{ scale: 0, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0, opacity: 0 }}
                      className="absolute top-2 right-2 w-6 h-6 bg-primary rounded-full flex items-center justify-center"
                      style={{ backgroundColor: themeColor }}
                    >
                      <Check className="w-4 h-4 text-white" />
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            ))
          ) : (
            <div>계획이 없습니다.</div>
          )}
        </div>
        <ScrollBar orientation="horizontal" />
      </ScrollArea>
      <div 
          className="notice-area"
          style={{backgroundColor : colors.genderColors[gender].PRIMARY_EXTRALIGHT}}
        >
            <div className="notice-container">
              {/* <div className="notice-head">AI 할일 조언 </div> */}
              <div className="notice-icon">
              </div>
              <div className="notice-text">{tip}</div>
            </div>
        </div>
      
      <AnimatePresence>
        {selectedPlan && !isEditing && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="mt-4"
          >
            <h3 className="text-lg font-semibold mb-2">할 일 목록</h3>
            {isTodoLoading ? <p>Loading...</p> : renderTodos()}
          </motion.div>
        )}
      </AnimatePresence>
      <div className="fixed bottom-20 right-6 flex flex-col gap-4">
        <motion.div
          className="bottom-28"
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          <Button
            className="w-14 h-14 rounded-full bg-secondary hover:bg-secondary/90 text-white shadow-lg"
            onClick={() => setIsEditMode(!isEditMode)}
          >
            <Edit2 className="w-6 h-6" />
          </Button>
        </motion.div>

        <motion.div
          className="bottom-12"
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          <Button
            className="w-14 h-14 rounded-full bg-primary hover:bg-primary/90 text-white shadow-lg"
            onClick={() => {
              if (selectedPlan) {
                setIsAddingNew(true);
              }
            }}
            style={{ backgroundColor: themeColor }}
          >
            <Plus className="w-6 h-6" />
          </Button>
        </motion.div>
        {/* 완료 표시 버튼 - 조건부 렌더링 */}
        <AnimatePresence>
          {selectedPlan && isAllTodosCompleted() && (
            <motion.div
              initial={{ scale: 0, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0, opacity: 0 }}
              transition={{ duration: 0.2 }}
            >
              <Button
                className="w-14 h-14 rounded-full bg-green-500 hover:bg-green-600 text-white shadow-lg"
                onClick={() => setShowTimeCapsule(true)}
              >
                <Check className="w-6 h-6" />
              </Button>
            </motion.div>
          )}
        </AnimatePresence>
        <TimeCapsuleModal
          isOpen={showTimeCapsule}
          onClose={() => setShowTimeCapsule(false)}
          planId={selectedPlan} // planId 전달
          planName={getStepName(
            plans.find((p) => p.planId === selectedPlan)?.serviceType
          )}
          onSave={handleSaveTimeCapsule}
        />
      </div>
    </div>
  );
};

export default VirginRoad;

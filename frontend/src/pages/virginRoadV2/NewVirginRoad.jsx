import { useNavigate } from "react-router-dom";
import { useEffect, useState, useMemo } from "react";
import useVirginRoadStore from "@/store/virginroad";
import useTodoStore from "@/store/todo";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { Plus, Edit2, Trash2, Check, X } from "lucide-react"; // 아이콘 추가
import { motion, AnimatePresence } from "framer-motion";

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
  const {
    plans = [],
    getVirginRoad,
    updatePlans,
    isLoading,
  } = useVirginRoadStore();
  const {
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

  const renderTodos = () => {
    const selectedTodos = todos[selectedPlan];
    if (!selectedTodos || !selectedTodos.todos) {
      return <p>No todos found.</p>;
    }

    return (
      <ul className="space-y-2">
        {selectedTodos.todos.map((todo) => (
          <li
            key={todo.todoId}
            className="flex items-center gap-2 p-2 bg-white rounded-lg shadow"
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
                {" "}
                {/* min-w-0 추가 */}
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
                  {" "}
                  {/* 버튼들은 크기 고정 */}
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
              <span className="flex-1 min-w-0 truncate">{todo.content}</span>
            )}

            {isEditMode && !editingTodoId && (
              <div className="flex gap-1 shrink-0">
                {" "}
                {/* 편집/삭제 버튼도 크기 고정 */}
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
                  onClick={() => handleDeleteTodo(selectedPlan, todo.todoId)}
                >
                  <Trash2 className="w-3 h-3 text-red-500" />
                </Button>
              </div>
            )}
          </li>
        ))}

        {/* 새 Todo 추가 입력 필드 */}
        {isAddingNew && (
          <li className="flex items-center gap-2 p-2 bg-white rounded-lg shadow">
            <input type="checkbox" disabled className="shrink-0" />
            <div className="flex-1 flex gap-2 items-center min-w-0">
              <input
                type="text"
                value={newTodoContent}
                onChange={(e) => setNewTodoContent(e.target.value)}
                onKeyDown={async (e) => {
                  if (e.key === "Enter" && newTodoContent.trim()) {
                    await handleCreateTodo(selectedPlan, newTodoContent);
                    setIsAddingNew(false);
                    setNewTodoContent("");
                  }
                }}
                placeholder="새로운 할 일을 입력하세요"
                className="flex-1 min-w-0 px-2 py-0.5 border rounded-md text-sm h-7"
                autoFocus
              />
              <div className="flex gap-1 shrink-0">
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-6 w-6 p-0"
                  onClick={async () => {
                    if (newTodoContent.trim()) {
                      await handleCreateTodo(selectedPlan, newTodoContent); // content 값 전달
                      setIsAddingNew(false);
                      setNewTodoContent("");
                    }
                  }}
                >
                  <Check className="w-3 h-3 text-green-500" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-6 w-6 p-0"
                  onClick={async () => {
                    if (newTodoContent.trim()) {
                      await handleCreateTodo(selectedPlan, newTodoContent);
                      setIsAddingNew(false);
                      setNewTodoContent("");
                    }
                  }}
                >
                  <X className="w-3 h-3 text-red-500" />
                </Button>
              </div>
            </div>
          </li>
        )}
      </ul>
    );
  };

  useEffect(() => {
    const fetchVirginRoad = async () => {
      try {
        await getVirginRoad();
      } catch (error) {
        console.error("버진로드 데이터 조회 실패:", error);
      }
    };
    fetchVirginRoad();
  }, [getVirginRoad]);

  useEffect(() => {
    if (Array.isArray(plans)) {
      setEditablePlans([...plans]);
    }
  }, [plans]);

  useEffect(() => {
    console.log("Plans updated:", plans);
    console.log("EditablePlans updated:", editablePlans);
  }, [plans, editablePlans]);

  const handleCancel = () => {
    if (Array.isArray(plans)) {
      setEditablePlans([...plans]);
    }
    setIsEditing(false);
  };

  const displayPlans = useMemo(() => {
    console.log("Calculating displayPlans:", { isEditing, editablePlans });
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
      COMPLETED: "bg-green-100 hover:bg-green-200",
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
      if (!todos[planId]) {
        await getTodos(planId);
      }
    }
  };

  const handleTodoToggle = async (planId, todoId, newState) => {
    try {
      await toggleTodoCompletion(planId, todoId, newState);
    } catch (error) {
      console.error("Todo 토글 실패:", error);
    }
  };

  // Todo 관리 함수들 추가
  const handleDeleteTodo = async (planId, todoId) => {
    try {
      await deleteTodo(planId, todoId);
    } catch (error) {
      console.error("Todo 삭제 실패:", error);
    }
  };

  const handleUpdateTodo = async (planId, todoId, content) => {
    try {
      await updateTodo(planId, todoId, content);
    } catch (error) {
      console.error("Todo 수정 실패:", error);
    }
  };

  const handleCreateTodo = async (planId, content) => {
    try {
      await createTodo(planId, content, false); // content와 together 값 모두 전달
    } catch (error) {
      console.error("Todo 생성 실패:", error);
    }
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
    <div className="p-4 w-full">
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
            <Button variant="default" onClick={handleSave} disabled={isSaving}>
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
          >
            <Plus className="w-6 h-6" />
          </Button>
        </motion.div>
      </div>
    </div>
  );
};

export default VirginRoad;

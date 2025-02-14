import instance from "@/lib/axios";
import { create } from "zustand";

const useTodoStore = create(
  (set, get) => {
    // 내부에서 사용할 API 호출 함수들을 먼저 정의
    const apiCalls = {
      async getTodos(planId) {
        const response = await instance.get(`/api/plan/${planId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        console.log("할 일 목록 조회 api 호출", response);
        return response.data;
      },
      async createTodo(planId, content, together) {
        const response = await instance.post(
          `/api/plan/${planId}/todo`,
          {
            content,
            together,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        return response.data;
      },
      async updateTodo(planId, todoId, content) {
        const response = await instance.patch(
          `/api/plan/${planId}/todo/${todoId}`,
          { content }, // 요청 바디
          {
            headers: {
              "Content-Type": "application/json", // Content-Type 헤더 명시적 설정
            },
          }
        );
        console.log("할 일 수정 api 호출", response);
        return response.data;
      },
      async deleteTodo(planId, todoId) {
        const response = await instance.delete(
          `/api/plan/${planId}/todo/${todoId}`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        console.log("할 일 삭제 api 호출", response);
        return response.data;
      },
      async toggleTodoCompletion(planId, todoId, isCompleted) {
        const response = await instance.put(
          `/api/plan/${planId}/todo/${todoId}`,
          { isCompleted }, // 요청 바디
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        return response.data;
      },
    };

    // store 객체 반환
    return {
      todos: {},
      isLoading: false,
      error: null,

      // 할 일 목록 조회
      getTodos: async (planId) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.getTodos(planId);
          // 서버에서 받은 데이터로 직접 업데이트
          set((state) => ({
            todos: {
              ...state.todos,
              [planId]: data.result, // 서버 응답 그대로 저장
            },
            isLoading: false,
          }));
          return data.result;
        } catch (error) {
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 생성
      createTodo: async (planId, content, together = false) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.createTodo(planId, content, together);
          set((state) => ({
            todos: {
              ...state.todos,
              [planId]: {
                ...state.todos[planId],
                todos: [...state.todos[planId].todos, data],
              },
            },
            isLoading: false,
          }));
          return data;
        } catch (error) {
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 수정
      updateTodo: async (planId, todoId, content) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.updateTodo(planId, todoId, content);
          set((state) => ({
            todos: {
              ...state.todos,
              [planId]: {
                ...state.todos[planId],
                todos: state.todos[planId].todos.map(
                  (
                    todo // todos 배열에 접근
                  ) => (todo.todoId === todoId ? { ...todo, ...data } : todo)
                ),
              },
            },
            isLoading: false,
          }));
          return data;
        } catch (error) {
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 삭제
      deleteTodo: async (planId, todoId) => {
        try {
          set({ isLoading: true });
          await apiCalls.deleteTodo(planId, todoId);
          set((state) => ({
            todos: {
              ...state.todos,
              [planId]: {
                ...state.todos[planId],
                todos: state.todos[planId].todos.filter(
                  // todos 배열에 접근
                  (todo) => todo.todoId !== todoId
                ),
              },
            },
            isLoading: false,
          }));
        } catch (error) {
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 완료 상태 토글
      toggleTodoCompletion: async (planId, todoId, isCompleted) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.toggleTodoCompletion(
            planId,
            todoId,
            isCompleted
          );
          set((state) => ({
            todos: {
              ...state.todos,
              [planId]: {
                ...state.todos[planId],
                todos: state.todos[planId].todos.map((todo) =>
                  todo.todoId === todoId
                    ? { ...todo, completed: data.completed }
                    : todo
                ),
              },
            },
            isLoading: false,
          }));
          return data;
        } catch (error) {
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 상태 초기화
      resetTodos: () => {
        set({
          todos: {},
          isLoading: false,
          error: null,
        });
      },
    };
  },
  {
    name: "todo-storage",
  }
);

export default useTodoStore;

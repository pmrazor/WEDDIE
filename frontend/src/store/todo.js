import instance from "@/lib/axios";
import { create } from "zustand";

const useTodoStore = create(
  (set, get) => {
    // 내부에서 사용할 API 호출 함수들을 먼저 정의
    const apiCalls = {
      async getTodos(serviceType) {
        const response = await instance.get(`/api/todo/getTodos/${serviceType}`);
        return response.data;
      },
      async createTodo(planId, todoData) {
        const { serviceType, stageType, content, isTogether } = todoData;

        const requestBody = {
          planId,
          serviceType,
          stageType,
          content,
          isTogether,
        };

        const response = await instance.post(
          "/api/todo/createTodo",
          requestBody,
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
          `/api/todo/${planId}/${todoId}`,
          { content }, // 요청 바디
          {
            headers: {
              "Content-Type": "application/json", // Content-Type 헤더 명시적 설정
            },
          }
        );
        return response.data;
      },
      async deleteTodo(planId, todoId) {
        const response = await instance.delete(`/api/todo/${todoId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        return response.data;
      },
      async toggleTodoCompletion(planId, todoId, isCompleted) {
        const response = await instance.patch(
          `/api/todo/${todoId}`,
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
      getTodos: async (serviceType) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.getTodos(serviceType);
          console.log(data);
          set((state) => ({
            todos: {
              ...state.todos,
              [serviceType]: data,
            },
            tip: data.tip,
            isLoading: false,
          }));
          return data;
        } catch (error) {
          console.error("할 일 목록 조회 실패:", error);
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 생성
      createTodo: async (planId, todoData) => {
        try {
          set({ isLoading: true });
          const newTodo = await apiCalls.createTodo(planId, todoData);
          set((state) => {
            const updatedTodos = { ...state.todos };
            if (!updatedTodos[todoData.serviceType]) {
              updatedTodos[todoData.serviceType] = {
                todos: { pre: [], on: [], post: [] },
              };
            }
            updatedTodos[todoData.serviceType].todos[
              todoData.stageType.toLowerCase()
            ].push(newTodo);
            return { todos: updatedTodos, isLoading: false };
          });
          return newTodo;
        } catch (error) {
          console.error("Todo 생성 실패:", error);
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 수정
      // 할 일 수정
      updateTodo: async (planId, todoId, content) => {
        try {
          set({ isLoading: true });
          const data = await apiCalls.updateTodo(planId, todoId, content);
          set((state) => {
            const updatedTodos = { ...state.todos };
            Object.keys(updatedTodos).forEach((serviceType) => {
              ["pre", "on", "post"].forEach((stage) => {
                if (updatedTodos[serviceType].todos[stage]) {
                  updatedTodos[serviceType].todos[stage] = updatedTodos[
                    serviceType
                  ].todos[stage].map((todo) =>
                    todo.todoId === todoId ? { ...todo, content } : todo
                  );
                }
              });
            });
            return { todos: updatedTodos, isLoading: false };
          });
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
          set((state) => {
            const updatedTodos = { ...state.todos };
            Object.keys(updatedTodos).forEach((serviceType) => {
              ["pre", "on", "post"].forEach((stage) => {
                if (updatedTodos[serviceType].todos[stage]) {
                  updatedTodos[serviceType].todos[stage] = updatedTodos[
                    serviceType
                  ].todos[stage].filter((todo) => todo.todoId !== todoId);
                }
              });
            });
            return { todos: updatedTodos, isLoading: false };
          });
        } catch (error) {
          console.error("Todo 삭제 실패:", error);
          set({ error: error.message, isLoading: false });
          throw error;
        }
      },

      // 할 일 완료 상태 토글
      toggleTodoCompletion: async (planId, todoId, isCompleted) => {
        try {
          set({ isLoading: true });
          const updatedTodo = await apiCalls.toggleTodoCompletion(
            planId,
            todoId,
            isCompleted
          );
          set((state) => {
            const updatedTodos = { ...state.todos };
            Object.keys(updatedTodos).forEach((serviceType) => {
              ["pre", "on", "post"].forEach((stage) => {
                if (updatedTodos[serviceType].todos[stage]) {
                  updatedTodos[serviceType].todos[stage] = updatedTodos[
                    serviceType
                  ].todos[stage].map((todo) =>
                    todo.todoId === todoId
                      ? { ...todo, completed: isCompleted }
                      : todo
                  );
                }
              });
            });
            return { todos: updatedTodos, isLoading: false };
          });
          return updatedTodo;
        } catch (error) {
          console.error("Todo 완료 상태 토글 실패:", error);
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

import { persist } from "zustand/middleware";
import { create } from "zustand";
import axios from "axios";

const MOCK_SERVER_URL =
  "https://7bd5e385-7ad3-4453-9460-9ab5721bee57.mock.pstmn.io";

const useShopStore = create(
  persist(
    (set, get) => {
      return {
        // 여기에 return 추가
        // 상태
        shops: {
          contents: [],
          listSize: 0,
          isFirstPage: true,
          isLastPage: true,
          totalPages: 0,
          totalElements: 0,
        },
        currentShop: null,
        selectedItem: null,
        selectedOptions: {},
        isLoading: false,
        error: null,
        currentPage: 0,

        // API 호출 메서드들
        getShops: async (params) => {
          try {
            set({ isLoading: true });
            const authStorage = localStorage.getItem("auth-storage");
            const token = authStorage
              ? JSON.parse(authStorage).state.token
              : null;

            const searchParams = new URLSearchParams();
            searchParams.append("page", params.page);
            searchParams.append("size", params.size);
            if (params.sort) searchParams.append("sort", params.sort);
            if (params.location)
              searchParams.append("location", params.location);

            const response = await axios.get(
              `${MOCK_SERVER_URL}/api/shop?${searchParams.toString()}`,
              {
                headers: {
                  Authorization: `Bearer ${token}`,
                  "Content-Type": "application/json",
                },
              }
            );

            set((state) => ({
              shops: {
                ...response.data.result,
                contents:
                  params.page > 1
                    ? [
                        ...state.shops.contents,
                        ...response.data.result.contents,
                      ]
                    : response.data.result.contents,
              },
              currentPage: params.page,
              isLoading: false,
              error: null,
            }));

            return response.data;
          } catch (error) {
            set({ error: error.message, isLoading: false });
            throw error;
          }
        },

        getShopDetail: async (shopId) => {
          try {
            set({ isLoading: true });
            const authStorage = localStorage.getItem("auth-storage");
            const token = authStorage
              ? JSON.parse(authStorage).state.token
              : null;

            const response = await axios.get(
              `${MOCK_SERVER_URL}/api/shop/${shopId}`,
              {
                headers: {
                  Authorization: `Bearer ${token}`,
                  "Content-Type": "application/json",
                },
              }
            );

            if (response.data.isSuccess) {
              const shopData = response.data.result;
              const formattedItems = shopData.items.map((item) => ({
                ...item,
                formattedPrice: new Intl.NumberFormat("ko-KR", {
                  style: "currency",
                  currency: item.price.currency || "KRW",
                  maximumFractionDigits: 0,
                }).format(item.price.amount),
                itemOptions: item.itemOptions.map((option) => ({
                  ...option,
                  formattedPrice: new Intl.NumberFormat("ko-KR", {
                    style: "currency",
                    currency: option.price.currency || "KRW",
                    maximumFractionDigits: 0,
                  }).format(option.price.amount),
                })),
              }));

              const formattedData = {
                ...shopData,
                formattedAddress: `${shopData.address.city} ${shopData.address.district} ${shopData.address.detail}`,
                items: formattedItems,
              };

              set({
                currentShop: formattedData,
                isLoading: false,
                error: null,
                selectedItem: null,
                selectedOptions: {},
              });

              return formattedData;
            }
            throw new Error(
              response.data.message || "상점 정보를 불러오는데 실패했습니다."
            );
          } catch (error) {
            set({ error: error.message, isLoading: false, currentShop: null });
            throw error;
          }
        },

        // 아이템 선택
        selectItem: (itemId) => {
          const state = get();
          const selectedItem = state.currentShop.items.find(
            (item) => item.id === itemId
          );

          const defaultOptions = {};
          if (selectedItem) {
            selectedItem.itemOptions.forEach((option) => {
              defaultOptions[option.id] = option.required ? 1 : 0;
            });
          }

          set({
            selectedItem,
            selectedOptions: defaultOptions,
          });
        },

        // 옵션 수량 업데이트
        updateOptionQuantity: (optionId, quantity) => {
          const state = get();
          if (!state.selectedItem) return;

          const option = state.selectedItem.itemOptions.find(
            (opt) => opt.id === optionId
          );

          if (!option) return;

          set({
            selectedOptions: {
              ...state.selectedOptions,
              [optionId]: Math.min(
                Math.max(option.required ? 1 : 0, quantity),
                option.max
              ),
            },
          });
        },

        // 총 가격 계산
        calculateTotalPrice: () => {
          const state = get();
          if (!state.selectedItem) return 0;

          let total = state.selectedItem.price.amount;

          Object.entries(state.selectedOptions).forEach(
            ([optionId, quantity]) => {
              const option = state.selectedItem.itemOptions.find(
                (opt) => opt.id === parseInt(optionId)
              );
              if (option) {
                total += option.price.amount * quantity;
              }
            }
          );

          return total;
        },

        // 초기화
        resetShops: () => {
          set({
            shops: {
              contents: [],
              listSize: 0,
              isFirstPage: true,
              isLastPage: true,
              totalPages: 0,
              totalElements: 0,
            },
            currentShop: null,
            isLoading: false,
            error: null,
            currentPage: 1,
          });
        },
      };
    },
    {
      name: "shop-storage",
    }
  )
);

export default useShopStore;

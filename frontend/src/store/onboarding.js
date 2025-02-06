import { create } from "zustand"

const useOnBoardingStore = create((set) => ({
  isOnBoarded : false, 
  completeOnBoarding : () => set({ isOnBoarded: true})
}))

export default useOnBoardingStore
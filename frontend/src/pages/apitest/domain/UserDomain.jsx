import useAuthStore from "@/store/auth";
import useCoupleStore from "@/store/couple";
import useOnboardingStore from "@/store/onboarding";
import useRegisterStore from "@/store/register";
import { useState } from "react";

const MALE_FORM = {
  "loginId": "qwert11111",
  "password": "q!w2e3r4t5",
  "name": "박준호"
}
const { loginId: maleLoginId, password: malePassword } = MALE_FORM

const MALE_DETAIL_FORM = {
  "age": 30,
  "gender": "MALE",
  "mbti": "ENFP",
  "phone": "010-5555-5555",
}

const FEMALE_FORM = {
  "loginId": "qwerty11111",
  "password": "q!w2e3r4t5",
  "name": "여자여자"
}
const { loginId: femaleLoginId, password: femalePassword } = FEMALE_FORM

const FEMALE_DETAIL_FORM = {
  "age": 30,
  "gender": "FEMALE",
  "mbti": "ENFP",
  "phone": "010-5555-5555",
}
const COUPLE_FORM = {
  "coupleId": 0,
  "coupleName": "사랑사랑",
}

// React 컴포넌트로 변경
const UserDomain = () => {
  
  const { register } = useRegisterStore()
  const { login, deleteUser } = useAuthStore()
  const { submitOnboarding } = useOnboardingStore()
  const { createCouple } = useCoupleStore()
  const [maleId, setMaleId] = useState(null);
  const [femaleId, setFemaleId] = useState(null);
  

  return {
    name: "유저 치트",
    buttons: [
      {
        label: "✅ 남자 가입/로그인/디테일 생성",
        api: async () => {
          const registerResult = await register(MALE_FORM);
          setMaleId(registerResult.data.result.id);
          console.log('회원가입 완료:', registerResult);
    
          const loginResult = await login(maleLoginId, malePassword);
          console.log('로그인 완료:', loginResult);
    
          const onboardingResult = await submitOnboarding(MALE_DETAIL_FORM);
          console.log('온보딩 완료:', onboardingResult);
          return `${registerResult.success && loginResult.isSuccess && onboardingResult.success ? '성공' : '실패'}`;
        },
        requestData: MALE_FORM,
      },
      {
        label: "✅ 여자 가입/로그인/디테일 생성",
        api: async () => {
          const registerResult = await register(FEMALE_FORM);
          setFemaleId(registerResult.data.result.id);
          console.log('회원가입 완료:', registerResult);
    
          const loginResult = await login(femaleLoginId, femalePassword);
          console.log('로그인 완료:', loginResult);
    
          const onboardingResult = await submitOnboarding(FEMALE_DETAIL_FORM);
          console.log('온보딩 완료:', onboardingResult);
          return `${registerResult.success && loginResult.isSuccess && onboardingResult.success ? '성공' : '실패'}`;
        },
        requestData: FEMALE_FORM,
      },

      {
        label: "✅ 여자 로그인 커플 맺기",
        api: async () => {
          COUPLE_FORM.coupleId = maleId;
          console.log(maleId)
          const coupleResult = await createCouple(COUPLE_FORM);
          console.log('커플 생성 완료:', coupleResult);
          return `${coupleResult.isSuccess ? '성공' : '실패'}`;
        },
        requestData: FEMALE_DETAIL_FORM,
      },
      {
        label: "⛔ 성 전환(미구현)"
      },
      {
        label: "남자 회원 가입",
        api: async () => {
          const registerResult = await register(MALE_FORM);
          setMaleId(registerResult.data.result.id);
          console.log('회원가입 완료:', registerResult);

          return `${registerResult.success ? '성공' : '실패'}`;
        },
        requestData: MALE_FORM,
      },
      {
        label: "남자 로그인",
        api: async () => {
          const loginResult = await login(maleLoginId, malePassword);
          console.log('로그인 완료:', loginResult);

          return `${loginResult.isSuccess ? '성공' : '실패'}`;
        },
        requestData: MALE_FORM,
      },
      {
        label: "남자 디테일 생성",
        api: async () => {
          const onboardingResult = await submitOnboarding(MALE_DETAIL_FORM);
          console.log('온보딩 완료:', onboardingResult);
          return `${onboardingResult.success ? '성공' : '실패'}`;
        },
        requestData: MALE_DETAIL_FORM,
      },

      {
        label: "여자 회원 가입",
        api: async () => {
          const registerResult = await register(FEMALE_FORM);
          setFemaleId(registerResult.data.result.id);
          console.log('회원가입 완료:', registerResult);

          return `${registerResult.success ? '성공' : '실패'}`;
        },
        requestData: FEMALE_FORM,
      },
      {
        label: "여자 로그인",
        api: async () => {

          const loginResult = await login(femaleLoginId, femalePassword);
          console.log('로그인 완료:', loginResult);

          return `${ loginResult.isSuccess ? '성공' : '실패'}`;
        },
        requestData: FEMALE_FORM,
      },
      {
        label: "여자 디테일 생성",
        api: async () => {

          const onboardingResult = await submitOnboarding(FEMALE_DETAIL_FORM);
          console.log('온보딩 완료:', onboardingResult);
          return `${ onboardingResult.success ? '성공' : '실패'}`;
        },
        requestData: FEMALE_DETAIL_FORM,
      },

      {
        label: "남자 로그인 커플 맺기",
        api: async () => {
          COUPLE_FORM.coupleId = femaleId;
          console.log(femaleId)
          const coupleResult = await createCouple(COUPLE_FORM);
          console.log('커플 생성 완료:', coupleResult);
          return `${coupleResult.isSuccess ? '성공' : '실패'}`;
        },
        requestData: COUPLE_FORM,
      },

      {
        label: "현재 로그인하고 있는 유저 삭제",
        api: () => deleteUser()
      }
    ],
  };
};

export default UserDomain;
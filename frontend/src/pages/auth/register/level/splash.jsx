import { motion } from "framer-motion";
import splash from "@/assets/clap.webp";

const Splash = ({ onFinish }) => {
  return (
    <motion.div
      className="fixed inset-0 flex flex-col items-center justify-center bg-white z-10 pb-16"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <div className="w-64 h-64 mb-8">
        <img
          src={splash}
          alt="Splash Animation"
          className="w-full h-full object-cover"
        />
      </div>
      <h2 className="text-xl font-semibold">결혼을 결심한 당신!</h2>
      <h2 className="text-xl font-semibold">진심으로 축하드려요!</h2>
      <h2 className="text-xl font-semibold mt-10">기다리고 있었어요!</h2>
      <h2 className="text-xl font-semibold">우리 부부만의 AI 웨딩플래너</h2>
      <div className="flex">
        <h2 className="text-xl font-bold text-primary">WEDDIE</h2>
        <h2 className="text-xl font-semibold">에 오신걸 환영합니다!</h2>
      </div>
    </motion.div>
  );
};

export default Splash;

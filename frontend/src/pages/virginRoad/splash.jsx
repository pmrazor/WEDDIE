import { motion } from "framer-motion";
import useVirginRoadStore from "@/store/virginroad";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Splash = ({ onFinish }) => {
  const navigate = useNavigate();
  const { getRecommendVirginRoad } = useVirginRoadStore();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getRecommendVirginRoad();
        if (response.isSuccess) {
          // 1초 후에 원하는 페이지로 라우팅
          setTimeout(() => {
            navigate("/main"); // 여기에 실제 이동하고 싶은 경로를 입력하세요
          }, 1000);
        }
      } catch (error) {
        console.error("Error fetching virgin road:", error);
      }
    };

    fetchData();
  }, [navigate]);

  // 텍스트 애니메이션을 위한 variants
  const textVariants = {
    initial: { opacity: 0, y: 20 },
    animate: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.6,
        delay: 1,
      },
    },
  };

  return (
    <motion.div
      className="fixed inset-0 flex flex-col items-center justify-center bg-white z-10 pb-16"
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
        variants={textVariants}
        initial="initial"
        animate="animate"
        className="flex flex-col items-center"
      >
        <div className="flex">
          <h2 className="text-xl font-bold text-primary">WEDDIE</h2>
          <h2 className="text-xl font-semibold">가 예비부부님들에</h2>
        </div>
        <h2 className="text-xl font-semibold">어울리는 계획을</h2>
        <h2 className="text-xl font-semibold">만들고 있어요!</h2>
        <h2 className="text-xl font-semibold mt-10">잠시만 기다려 주세요!</h2>
      </motion.div>
    </motion.div>
  );
};

export default Splash;

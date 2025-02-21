import { Stage, Sprite, Text, Container, Graphics } from "@pixi/react";
import { useState, useEffect } from "react";
import backgroundImage from "@/assets/main-cy-image/backgroundstairs.jpg";
import BeforeReady from "@/assets/main-cy-image/BeforeReady.png";
import AfterReady from "@/assets/main-cy-image/AfterReady.png";
import car from "@/assets/main-cy-image/car.jpg";
import hanbok from "@/assets/main-cy-image/hanbok.jpg";
import makeup from "@/assets/main-cy-image/makeup.jpg";


const MiniHome = () => {
  const [charPosition, setCharPosition] = useState({ x: 0.3, y: 0.5 });
  const [stageSize, setStageSize] = useState({ width: window.innerWidth * 1 , height: window.innerHeight * 1  });

  useEffect(() => {
    const updateSize = () => {
      const width = window.innerWidth * 0.90;
      const height = window.innerHeight * 0.4;
      setStageSize({ width, height });
    };

    updateSize();
    window.addEventListener("resize", updateSize);
    return () => window.removeEventListener("resize", updateSize);
  }, []);

  return (

    <div className="mini-home">
      <Stage 
        width={stageSize.width} 
        height={stageSize.height} 
        // options={{ backgroundColor: 0xffffff }}
      >
        {/* 배경 */}
        <Sprite 
          image={backgroundImage} 
          x={0} y={0} 
          width={stageSize.width} 
          height={stageSize.height} 
        />

        {/* 캐릭터 */}
        <Sprite
          image={BeforeReady}
          x={charPosition.x * stageSize.width}
          y={charPosition.y * stageSize.height}
          width={stageSize.width * 0.4} // 반응형 크기 조정
          height={stageSize.height * 0.4}
        />
        {/* 캐릭터 - AfterReady */}
        {/* <Sprite
          image={AfterReady}
          x={charPosition.x * stageSize.width}
          y={charPosition.y * stageSize.height - 80}
          width={stageSize.width * 0.4} // 반응형 크기 조정
          height={stageSize.height * 0.7}
        /> */}

        {/* 장식 아이템 */}
        <Sprite
          image={hanbok}
          x={stageSize.width * 0.2}
          y={stageSize.height * 0.3}
          width={stageSize.width * 0.08}
          height={stageSize.height * 0.12}
          />
      </Stage>

      {/* 캐릭터 이동 버튼 */}
      {/* <div className="controls">
        <button onClick={() => moveCharacter("left")}>←</button>
        <button onClick={() => moveCharacter("right")}>→</button>
        <button onClick={() => moveCharacter("up")}>↑</button>
        <button onClick={() => moveCharacter("down")}>↓</button>
      </div> */}
    </div>
  )
}

export default MiniHome 
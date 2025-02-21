import Button from "./Button"
import { useState } from "react"

const ButtonGroup = ({ domain, buttons }) => {
  const [hoveredIndex, setHoveredIndex] = useState(null);

  return (
    <div className="p-6 bg-white rounded-lg shadow-lg border border-gray-200">
      <h2 className="text-2xl font-bold mb-4 text-gray-800 pb-2 border-b border-gray-200">
        {domain}
      </h2>
      <button
  onClick={() => console.clear()}
  className="mb-4 px-3 py-1 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-opacity-50 text-sm transition-colors"
>
  콘솔 로그 초기화
</button>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {buttons.map((button, index) => (
          <div 
            key={index} 
            className="relative"
            onMouseEnter={() => setHoveredIndex(index)}
            onMouseLeave={() => setHoveredIndex(null)}
          >
            <div className="transform transition-transform duration-200 hover:scale-105">
              <Button 
                label={button.label} 
                api={button.api} 
              />
            </div>
            {hoveredIndex === index && button.requestData && (
              <div className="absolute w-full mt-2 p-3 bg-gray-800 text-white rounded-md shadow-lg z-10">
                <pre className="text-xs font-mono">
                  {JSON.stringify(button.requestData, null, 2)}
                </pre>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

export default ButtonGroup
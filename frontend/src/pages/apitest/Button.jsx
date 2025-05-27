import { useState } from "react"
import PropTypes from 'prop-types'

const Button = ({ label, api }) => {
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [isInitialState, setIsInitialState] = useState(true)

  const handleClick = async () => {
    if (!isInitialState) {
      // Reset to initial state
      setResponse(null)
      setError(null)
      setIsInitialState(true)
      return
    }

    setLoading(true)
    setError(null)
    setIsInitialState(false)
    try {
      const result = await api()
      setResponse(result)
    } catch (err) {
      let errorPrefix;
      
      if (err.response) {
        console.log(err.response)
        errorPrefix = "[API 응답 에러] ";
      } else if (err.request) {
        console.log(err.request)
        errorPrefix = "[API 요청 에러] ";
      } else {
        console.log(err)
        errorPrefix = "[자바 내부 오류] ";
      }
      
      const errorDetails = err.response?.data 
        ? JSON.stringify(err.response.data, null, 2)
        : err.message || JSON.stringify(err, null, 2);
      
      setError(errorPrefix + errorDetails);
    } finally {
      setLoading(false)
    }
  }

  return (
    <button
      className="w-full px-4 py-2 m-2 bg-blue-500 text-white rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 relative"
      onClick={handleClick}
      disabled={loading}
    >
      <div className="w-full max-h-32 overflow-y-auto">
        <div className="flex items-center justify-center">
          {loading ? (
            <span className="text-sm">로딩 중...</span>
          ) : error ? (
            <div className="w-full text-center">
              <span className="text-sm text-red-200 break-words">
                {error}
              </span>
              <div className="text-xs mt-1">
                (클릭하여 초기화)
              </div>
            </div>
          ) : response ? (
            <div className="w-full text-center">
              <span className="text-sm break-words">{JSON.stringify(response, null, 2)}</span>
              <div className="text-xs mt-1">
                (클릭하여 초기화)
              </div>
            </div>
          ) : (
            <span className="text-sm">{label}</span>
          )}
        </div>
      </div>
    </button>
  )
}

Button.propTypes = {
  label: PropTypes.string.isRequired,
  api: PropTypes.func.isRequired
}

export default Button
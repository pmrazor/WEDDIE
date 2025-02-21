import "@/style/component/IconCard.css"

const IconCard = ({ type, head_text1, head_text2, sub_text1, sub_text2, icon, style, onClick }) => {

  return (
    <div className="IconCard" style={style}
      type={type} // 카드의 타입 : onBoarding
      onClick={onClick} // 클릭 시 실행할 함수 //
    >
      <div className="text-field">
        <div className="head-text">{head_text1}<br/>{head_text2}</div>
        <div className="sub-text">{sub_text1}<br/>{sub_text2}</div>
      </div>
      <div className="icon-area"> {/* icon이 있을 위치 조정 */}
        {icon} {/* icon은 어디서 가져오나?  */}
      </div>    
    </div>
  )
}

export default IconCard
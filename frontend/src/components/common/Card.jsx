import "../../style/common/Card.css"

const Card = ({ type, head_text1, head_text2, sub_text1, sub_text2, icon, style, onClick }) => {

  return (
    <div className="Card" style={style}
      type={type} // 카드의 타입 : onBoarding
      onClick={onClick} // 클릭 시 실행할 함수 //
    >
      <div className="text-field">
        <h1 className="head-text">{head_text1}<br/>{head_text2}</h1>
        <h3 className="sub-text">{sub_text1}<br/>{sub_text2}</h3>
      </div>
      <div className="icon-area"> {/* icon이 있을 위치 조정 */}
        {icon} {/* icon은 어디서 가져오나?  */}
      </div>
    </div>
  )
}

export default Card
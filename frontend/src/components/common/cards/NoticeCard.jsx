import "@/style/component/NoticeCard.css"

const NoticeCard = ({ icon, headText, subText1, subText2, backgroundColor }) => {

  return (
    <div className="NoticeCard" style={{backgroundColor: backgroundColor}}>
      <img src={icon} alt="icon" />
      <div className="text-area">
        <div className="head-text">{headText}</div>
        <div className="sub-text1">{subText1}</div>
        <div className="sub-text2">{subText2}</div>
      </div>
    </div>
  )
}


export default NoticeCard
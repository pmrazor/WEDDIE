import "@/style/component/AdvertisementCard.css"

const AdvertisementCard = ({ onClick, headText, subText1, subText2, style }) => {

  return (
    <div className="AdvertisementCard"
      style={style}
      onClick={onClick}
    >
      <div className="text-field">
        <h1 className="head-text">{headText}</h1>
        <h1 className="sub-text">{subText1}</h1>
        <h1 className="sub-text">{subText2}</h1>
      </div>
    </div>
  )
}

export default AdvertisementCard
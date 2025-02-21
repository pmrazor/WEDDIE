import "@/style/component/AdvertisementCard.css"

const AdvertisementCard = ({ onClick, headText1, headText2, subText1, subText2, subText3, style }) => {

  return (
    <div className="AdvertisementCard"
      style={style}
      onClick={onClick}
    >
      <div className="text-field">
        <h1 className="head-text1">{headText1}</h1>
        <h1 className="head-text2">{headText2}</h1>
        <h1 className="sub-text">{subText1}</h1>
        <h1 className="sub-text">{subText2}</h1>
        <h1 className="sub-text">{subText3}</h1>
      </div>
    </div>
  )
}

export default AdvertisementCard
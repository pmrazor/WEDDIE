import "../style/component/AdvertisementCard.css"

const AdvertisementCard = ({ onClick, head_text, sub_text, background_img }) => {

  return (
    <div className="AdvertisementCard"
      onClick={onClick}
    >
      <div className="text-field">
        <h1 className="head-text">{head_text}</h1>
        <h1 className="sub-text">{sub_text}</h1>
      </div>
    </div>
  )
}

export default AdvertisementCard
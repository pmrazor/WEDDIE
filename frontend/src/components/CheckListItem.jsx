import "@/style/component/CheckListItem.css"

const CheckListItem = ({ content, isFinished}) => {

  return (
    <div className="CheckListItem">
      <div className="isFinished">
        <input type="checkbox"/>
      </div>
      <div className="content">{content}</div>
      <div className="delete-button">
        <button>...</button>
      </div>
    </div>
  )
}

export default CheckListItem
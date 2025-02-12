import "@/style/component/CheckListItem.css"

const CheckListItem = ({ id, content, isCompleted, onToggle, onDelete }) => {
  return (
    <div className="CheckListItem">
      <div className="text-area">
        <input type="checkbox" checked={isCompleted === "true"} onChange={() => onToggle(id)} className="mr-2" />
        <span>{content}</span>
      </div>
      <button
        onClick={() => {
          if (window.confirm("정말로 이 항목을 삭제하시겠습니까?")) {
            onDelete(id);
          }
        }}
        className="text-red-500 hover:text-red-700 px-2 py-1 rounded"
      >
        삭제
      </button>
    </div>
  );
};

export default CheckListItem
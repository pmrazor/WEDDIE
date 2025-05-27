// components/common/Modal.jsx
import React from "react";
import "@/style/common/Modal.css";

const Modal = ({ isOpen, onClose, onConfirm, message }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <p>{message}</p>
        <div className="modal-buttons">
          <button onClick={onConfirm}>예</button>
          <button onClick={onClose}>아니오</button>
        </div>
      </div>
    </div>
  );
};

export default Modal;

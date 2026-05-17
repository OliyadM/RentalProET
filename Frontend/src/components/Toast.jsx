// Toast component will go here
import { useEffect } from "react";

export default function Toast({ message, type = "success", onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3000);
    return () => clearTimeout(t);
  }, [onClose]);

  return (
    <div className={`fixed bottom-5 right-5 z-50 px-5 py-3 rounded-lg shadow-lg text-white text-sm font-medium
      ${type === "success" ? "bg-success" : "bg-danger"}`}>
      {message}
    </div>
  );
}
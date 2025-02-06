import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import Appbar from "./components/navbar";
import { BrowserRouter } from "react-router-dom";
import { Toaster } from "@/components/ui/toaster";

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Appbar />
    <App />
    <Toaster />
  </BrowserRouter>
);

import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import Appbar from "./components/appbar.jsx";
import NavBar from "./components/common/NavBar";
import { BrowserRouter } from "react-router-dom";
import { Toaster } from "@/components/ui/toaster";

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Appbar />
    <App />
    <NavBar />
    <Toaster />
  </BrowserRouter>
);

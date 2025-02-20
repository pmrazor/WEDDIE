// import "../../style/common/NavBar.css"
import React from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Home, Bus, Store, Heart, User } from "lucide-react";

const BottomNav = () => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-background border-t border-border p-2">
      <div className="flex justify-around items-center">
        <Button variant="ghost" size="icon" asChild>
          <Link to="/">
            <Home className="h-6 w-6" />
          </Link>
        </Button>
        <Button variant="ghost" size="icon" asChild>
          <Link to="/virginroad">
            <Bus className="h-6 w-6" />
          </Link>
        </Button>
        <Button variant="ghost" size="icon" asChild>
          <Link to="/shop/list">
            <Store className="h-6 w-6" />
          </Link>
        </Button>
        <Button variant="ghost" size="icon" asChild>
          <Link to="/couple">
            <Heart className="h-6 w-6" />
          </Link>
        </Button>
        {/* <Button variant="ghost" size="icon" asChild>
          <Link to="/mypage">
            <User className="h-6 w-6" />
          </Link>
        </Button> */}
      </div>
    </nav>
  );
};

export default BottomNav;

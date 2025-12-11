'use client';

import React from "react";
import { Button } from "../ui/button";

type Props = {
  quantity: number;
  onChange: (next: number) => void;
  min?: number;
  max?: number;
  size?: "default" | "sm";
};

export const QuantitySelector: React.FC<Props> = ({ quantity, onChange, min = 1, max = 99, size = "default" }) => {
  const handleChange = (next: number) => {
    if (next < min || next > max) return;
    onChange(next);
  };

  const btnSize = size === "sm" ? "icon-sm" : "sm"; // Assuming "icon-sm" logic or just reusing "sm" if button supports it. 
  // Wait, button sizes are likely "default", "sm", "lg", "icon". 
  // Let's just pass "sm" to Button if size is "sm".
  
  return (
    <div className={`flex items-center ${size === "sm" ? "gap-1" : "gap-2"}`}>
      <Button variant="secondary" size="sm" className={size === "sm" ? "h-7 w-7 p-0" : ""} onClick={() => handleChange(quantity - 1)} disabled={quantity <= min}>
        -
      </Button>
      <div className={`text-center font-semibold ${size === "sm" ? "w-8 text-xs" : "w-10 text-sm"}`}>{quantity}</div>
      <Button variant="secondary" size="sm" className={size === "sm" ? "h-7 w-7 p-0" : ""} onClick={() => handleChange(quantity + 1)} disabled={quantity >= max}>
        +
      </Button>
    </div>
  );
};

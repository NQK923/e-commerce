'use client';

import React from "react";
import { Button } from "../ui/button";
import { Minus, Plus } from "lucide-react";

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

  const isSmall = size === "sm";
  const btnClass = `rounded-none border-0 bg-transparent hover:bg-zinc-100 text-zinc-600 disabled:opacity-30 ${isSmall ? "h-7 w-7 p-0" : "h-9 w-9"}`;
  const iconSize = isSmall ? 12 : 14;

  return (
    <div className={`flex items-center border border-zinc-200 rounded-lg overflow-hidden bg-white shadow-sm ${isSmall ? "h-7" : "h-9"}`}>
      <Button 
        variant="ghost" 
        size="icon" 
        className={btnClass} 
        onClick={() => handleChange(quantity - 1)} 
        disabled={quantity <= min}
      >
        <Minus size={iconSize} />
      </Button>
      
      <div className={`flex items-center justify-center font-semibold text-zinc-900 bg-white border-x border-zinc-100 ${isSmall ? "w-8 text-xs" : "w-10 text-sm"} h-full`}>
        {quantity}
      </div>
      
      <Button 
        variant="ghost" 
        size="icon" 
        className={btnClass} 
        onClick={() => handleChange(quantity + 1)} 
        disabled={quantity >= max}
      >
        <Plus size={iconSize} />
      </Button>
    </div>
  );
};

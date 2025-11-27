'use client';

import React from "react";
import { Button } from "../ui/button";

type Props = {
  quantity: number;
  onChange: (next: number) => void;
  min?: number;
  max?: number;
};

export const QuantitySelector: React.FC<Props> = ({ quantity, onChange, min = 1, max = 99 }) => {
  const handleChange = (next: number) => {
    if (next < min || next > max) return;
    onChange(next);
  };

  return (
    <div className="flex items-center gap-2">
      <Button variant="secondary" size="sm" onClick={() => handleChange(quantity - 1)} disabled={quantity <= min}>
        -
      </Button>
      <div className="w-10 text-center text-sm font-semibold">{quantity}</div>
      <Button variant="secondary" size="sm" onClick={() => handleChange(quantity + 1)} disabled={quantity >= max}>
        +
      </Button>
    </div>
  );
};

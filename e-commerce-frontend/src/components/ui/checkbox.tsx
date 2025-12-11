'use client';

import React from "react";
import { cx } from "../../utils/cx";

type CheckboxProps = React.InputHTMLAttributes<HTMLInputElement> & {
  onCheckedChange?: (checked: boolean) => void;
};

const baseStyles =
  "h-4 w-4 rounded border border-zinc-300 text-emerald-600 focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 cursor-pointer";

export const Checkbox: React.FC<CheckboxProps> = ({ className, onCheckedChange, onChange, ...rest }) => {
  const handleChange: React.ChangeEventHandler<HTMLInputElement> = (event) => {
    onChange?.(event);
    onCheckedChange?.(event.target.checked);
  };

  return (
    <input
      type="checkbox"
      className={cx(baseStyles, className)}
      onChange={handleChange}
      {...rest}
    />
  );
};

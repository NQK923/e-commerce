'use client';

import React from "react";
import { cx } from "../../utils/cx";

type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
};

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, className, ...props }, ref) => {
    // If a label prop is present, wrap in label tag, otherwise just return the input
    const inputElement = (
      <input
        ref={ref}
        className={cx(
          "w-full rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm focus:border-black focus:outline-none focus:ring-2 focus:ring-black/10",
          error && "border-rose-400 focus:ring-rose-200",
          className,
        )}
        {...props}
      />
    );

    if (!label && !error) {
      return inputElement;
    }

    return (
      <label className="flex flex-col gap-1 text-sm text-zinc-700">
        {label && <span className="font-medium">{label}</span>}
        {inputElement}
        {error && <span className="text-xs text-rose-500">{error}</span>}
      </label>
    );
  }
);

Input.displayName = "Input";

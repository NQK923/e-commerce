'use client';

import React from "react";
import { cx } from "../../utils/cx";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost" | "outline";
  size?: "sm" | "md" | "lg" | "icon";
  block?: boolean;
};

const baseStyles =
  "inline-flex items-center justify-center rounded-lg font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed";

const variantStyles: Record<NonNullable<ButtonProps["variant"]>, string> = {
  primary: "bg-emerald-600 text-white hover:bg-emerald-700 focus:ring-emerald-600 shadow-sm",
  secondary: "bg-white text-zinc-900 border border-zinc-200 hover:bg-zinc-50 focus:ring-zinc-200",
  ghost: "bg-transparent text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900 focus:ring-zinc-200",
  outline: "border border-zinc-200 bg-transparent text-zinc-900 hover:bg-zinc-50 focus:ring-zinc-200",
};

const sizeStyles: Record<NonNullable<ButtonProps["size"]>, string> = {
  sm: "px-3 py-2 text-sm",
  md: "px-4 py-2.5 text-sm",
  lg: "px-5 py-3 text-base",
  icon: "h-9 w-9 p-0",
};

export const Button: React.FC<ButtonProps> = ({
  variant = "primary",
  size = "md",
  block = false,
  className,
  ...props
}) => {
  return (
    <button
      className={cx(baseStyles, variantStyles[variant], sizeStyles[size], block && "w-full", className)}
      {...props}
    />
  );
};

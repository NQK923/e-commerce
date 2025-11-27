'use client';

import React from "react";
import { cx } from "../../utils/cx";

type BadgeProps = React.HTMLAttributes<HTMLSpanElement> & {
  tone?: "default" | "success" | "warning" | "danger";
};

export const Badge: React.FC<BadgeProps> = ({ tone = "default", className, ...props }) => {
  const toneClass =
    tone === "success"
      ? "bg-emerald-100 text-emerald-800"
      : tone === "warning"
        ? "bg-amber-100 text-amber-800"
        : tone === "danger"
          ? "bg-rose-100 text-rose-800"
          : "bg-zinc-100 text-zinc-800";

  return (
    <span
      className={cx("inline-flex items-center rounded-full px-2 py-1 text-xs font-semibold", toneClass, className)}
      {...props}
    />
  );
};

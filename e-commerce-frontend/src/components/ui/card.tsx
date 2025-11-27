'use client';

import React from "react";
import { cx } from "../../utils/cx";

type CardProps = React.HTMLAttributes<HTMLDivElement>;

export const Card: React.FC<CardProps> = ({ className, ...props }) => (
  <div
    className={cx(
      "rounded-xl border border-zinc-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md",
      className,
    )}
    {...props}
  />
);

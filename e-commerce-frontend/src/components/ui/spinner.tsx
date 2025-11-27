'use client';

import React from "react";
import { cx } from "../../utils/cx";

export const Spinner: React.FC<{ className?: string }> = ({ className }) => (
  <div className={cx("h-5 w-5 animate-spin rounded-full border-2 border-zinc-300 border-t-black", className)} />
);

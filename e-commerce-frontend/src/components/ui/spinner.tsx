'use client';

import React from "react";
import { cx } from "../../utils/cx";

type SpinnerProps = {
  className?: string;
  size?: 'sm' | 'default' | 'lg';
};

export const Spinner: React.FC<SpinnerProps> = ({ className, size = 'default' }) => {
  const sizeClasses = {
    sm: 'h-4 w-4 border',
    default: 'h-5 w-5 border-2',
    lg: 'h-8 w-8 border-2',
  };

  return (
    <div 
      className={cx(
        'animate-spin rounded-full border-zinc-300 border-t-black',
        sizeClasses[size], 
        className
      )} 
    />
  );
};

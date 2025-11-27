'use client';

import React from "react";

export const Footer: React.FC = () => (
  <footer className="border-t border-zinc-200 bg-white py-6">
    <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 text-sm text-zinc-600">
      <span>© {new Date().getFullYear()} eCommerce. All rights reserved.</span>
      <div className="flex gap-4">
        <a href="/privacy" className="hover:text-black">
          Privacy
        </a>
        <a href="/terms" className="hover:text-black">
          Terms
        </a>
      </div>
    </div>
  </footer>
);

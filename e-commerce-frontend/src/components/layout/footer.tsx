'use client';

import React from "react";
import Link from "next/link";
import { Facebook, Instagram, Twitter } from "lucide-react";
import { useTranslation } from "../../providers/language-provider";

export const Footer: React.FC = () => {
  const { t } = useTranslation();

  return (
    <footer className="bg-black text-sm text-zinc-400 mt-12">
      <div className="mx-auto grid max-w-7xl grid-cols-1 gap-10 px-6 pt-12 pb-8 sm:grid-cols-2 lg:grid-cols-4 lg:gap-8">
        
        {/* Brand Column */}
        <div className="flex flex-col gap-6 lg:col-span-1">
          <Link href="/" className="text-4xl font-black tracking-tighter text-white uppercase">
            EcomX.
          </Link>
          <p className="text-xs leading-relaxed max-w-xs text-zinc-500">
            Elevating your everyday lifestyle with curated, premium essentials. Designed for the modern world.
          </p>
          <div className="flex gap-5 mt-4">
            <a href="#" className="text-zinc-500 hover:text-white transition-colors"><Facebook size={20}/></a>
            <a href="#" className="text-zinc-500 hover:text-white transition-colors"><Instagram size={20}/></a>
            <a href="#" className="text-zinc-500 hover:text-white transition-colors"><Twitter size={20}/></a>
          </div>
        </div>

        {/* Column 1: About */}
        <div className="flex flex-col gap-6">
          <h3 className="font-bold uppercase text-white tracking-widest text-[10px]">{t.footer.about}</h3>
          <ul className="flex flex-col gap-4 text-xs">
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.about_us}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.careers}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.seller_centre}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.flash_sales}</Link></li>
          </ul>
        </div>

        {/* Column 2: Customer Service */}
        <div className="flex flex-col gap-6">
          <h3 className="font-bold uppercase text-white tracking-widest text-[10px]">{t.footer.customer_service}</h3>
          <ul className="flex flex-col gap-4 text-xs">
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.help_centre}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.how_to_buy}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.return_refund}</Link></li>
            <li><Link href="#" className="hover:text-white transition-colors">{t.footer.privacy_policy}</Link></li>
          </ul>
        </div>

        {/* Column 3: Logistics */}
        <div className="flex flex-col gap-6">
          <h3 className="font-bold uppercase text-white tracking-widest text-[10px]">{t.footer.payment_logistics}</h3>
          <div className="flex flex-wrap gap-2">
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-[10px] font-bold text-white hover:border-zinc-600 transition-colors cursor-pointer">VISA</div>
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-[10px] font-bold text-white hover:border-zinc-600 transition-colors cursor-pointer">MOMO</div>
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-[10px] font-bold text-white hover:border-zinc-600 transition-colors cursor-pointer">COD</div>
          </div>
          
          <h3 className="font-bold uppercase text-white tracking-widest text-[10px] mt-4">{t.footer.shipping_delivery}</h3>
          <div className="flex flex-wrap gap-2">
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-[10px] font-bold text-white hover:border-zinc-600 transition-colors cursor-pointer">GRAB</div>
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-[10px] font-bold text-white hover:border-zinc-600 transition-colors cursor-pointer">GHN</div>
            <div className="flex h-8 w-12 items-center justify-center rounded-md border border-zinc-800 bg-zinc-900 text-white hover:border-zinc-600 transition-colors cursor-pointer">
              <TruckIcon />
            </div>
          </div>
        </div>

      </div>

      {/* Subtle Brand Element instead of massive text */}
      <div className="mx-auto max-w-7xl px-6 pb-6 pt-4 flex justify-center">
        <h2 className="text-4xl font-black text-zinc-800 tracking-widest uppercase">
          EcomX
        </h2>
      </div>

      {/* Bottom Footer */}
      <div className="border-t border-zinc-900 py-6 bg-black">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-6 text-[10px] uppercase tracking-wider text-zinc-500 sm:flex-row">
          <p>© {new Date().getFullYear()} EcomX. {t.footer.rights_reserved}</p>
          <div className="flex gap-6">
            <span className="hover:text-white cursor-pointer transition-colors">{t.footer.region}</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

const TruckIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10 17h4V5H2v12h3"/><path d="M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5"/><path d="M14 17h1"/><circle cx="7.5" cy="17.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg>
);

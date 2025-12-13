'use client';

import React from "react";
import Link from "next/link";
import { Facebook, Instagram, Twitter, CreditCard } from "lucide-react";
import { useTranslation } from "../../providers/language-provider";

export const Footer: React.FC = () => {
  const { t } = useTranslation();

  return (
    <footer className="border-t border-zinc-200 bg-white text-sm text-zinc-600">
      <div className="mx-auto grid max-w-7xl grid-cols-1 gap-10 px-6 py-12 sm:grid-cols-2 lg:grid-cols-3 lg:gap-12">
        
        {/* Column 1: About & Social */}
        <div className="flex flex-col gap-8">
          <div>
            <h3 className="font-bold uppercase text-zinc-900 tracking-wider text-xs mb-4">{t.footer.about}</h3>
            <ul className="flex flex-col gap-3 text-zinc-500">
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.about_us}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.careers}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.seller_centre}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.flash_sales}</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="font-bold uppercase text-zinc-900 tracking-wider text-xs mb-4">{t.footer.follow_us}</h3>
            <div className="flex gap-3">
                <a href="#" className="h-9 w-9 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-blue-600 hover:text-white transition-all"><Facebook size={18}/></a>
                <a href="#" className="h-9 w-9 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-pink-600 hover:text-white transition-all"><Instagram size={18}/></a>
                <a href="#" className="h-9 w-9 flex items-center justify-center rounded-full bg-zinc-100 hover:bg-sky-500 hover:text-white transition-all"><Twitter size={18}/></a>
            </div>
          </div>
        </div>

        {/* Column 2: Customer Service & Policies */}
        <div className="flex flex-col gap-8">
          <div>
            <h3 className="font-bold uppercase text-zinc-900 tracking-wider text-xs mb-4">{t.footer.customer_service}</h3>
            <ul className="flex flex-col gap-3 text-zinc-500">
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.help_centre}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.how_to_buy}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.return_refund}</Link></li>
              <li><Link href="#" className="hover:text-emerald-600 transition-colors">{t.footer.privacy_policy}</Link></li>
            </ul>
          </div>
        </div>

        {/* Column 3: Payment & Shipping */}
        <div className="flex flex-col gap-8">
          <div>
            <h3 className="font-bold uppercase text-zinc-900 tracking-wider text-xs mb-4">{t.footer.payment_logistics}</h3>
            <div className="grid grid-cols-3 gap-3">
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50" title="Visa">
                    <span className="font-bold text-[10px] text-blue-700">VISA</span>
                </div>
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50" title="Mastercard">
                    <CreditCard size={20} className="text-orange-600" />
                </div>
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50" title="Momo">
                    <span className="font-bold text-[10px] text-pink-600">MOMO</span>
                </div>
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50" title="COD">
                    <span className="font-bold text-[10px] text-zinc-600">COD</span>
                </div>
            </div>
          </div>
          <div>
            <h3 className="font-bold uppercase text-zinc-900 tracking-wider text-xs mb-4">{t.footer.shipping_delivery}</h3>
            <div className="grid grid-cols-3 gap-3">
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50">
                    <TruckIcon />
                </div>
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50">
                    <span className="font-bold text-[10px] text-green-600">GRAB</span>
                </div>
                <div className="flex h-10 items-center justify-center rounded-lg border border-zinc-100 bg-zinc-50/50">
                    <span className="font-bold text-[10px] text-orange-600">GHN</span>
                </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Footer */}
      <div className="bg-zinc-50 border-t border-zinc-200 py-8">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-6 text-xs text-zinc-400 sm:flex-row">
          <p className="font-medium">© {new Date().getFullYear()} EcomX. {t.footer.rights_reserved}</p>
          <div className="flex gap-6">
            <span>{t.footer.region}</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

// Simple icon component for internal use
const TruckIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10 17h4V5H2v12h3"/><path d="M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5"/><path d="M14 17h1"/><circle cx="7.5" cy="17.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg>
);

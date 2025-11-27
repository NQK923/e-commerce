'use client';

import React from "react";
import Link from "next/link";
import { Facebook, Instagram, Twitter, CreditCard } from "lucide-react";
import { useTranslation } from "../../providers/language-provider";

export const Footer: React.FC = () => {
  const { t } = useTranslation();

  return (
    <footer className="border-t border-zinc-200 bg-zinc-50 text-sm text-zinc-600">
      {/* Top Footer: Links */}
      <div className="mx-auto grid max-w-7xl grid-cols-1 gap-8 px-4 py-12 sm:grid-cols-2 md:grid-cols-4">
        {/* Column 1 */}
        <div className="flex flex-col gap-4">
          <h3 className="font-bold uppercase text-zinc-900">{t.footer.customer_service}</h3>
          <ul className="flex flex-col gap-2">
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.help_centre}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.how_to_buy}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.payment_methods}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.shipping_delivery}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.return_refund}</Link></li>
          </ul>
        </div>

        {/* Column 2 */}
        <div className="flex flex-col gap-4">
          <h3 className="font-bold uppercase text-zinc-900">{t.footer.about}</h3>
          <ul className="flex flex-col gap-2">
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.about_us}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.careers}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.privacy_policy}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.flash_sales}</Link></li>
            <li><Link href="#" className="hover:text-emerald-600">{t.footer.seller_centre}</Link></li>
          </ul>
        </div>

        {/* Column 3: Payment & Logistics (Visual placeholders) */}
        <div className="flex flex-col gap-4">
          <h3 className="font-bold uppercase text-zinc-900">{t.footer.payment_logistics}</h3>
          <div className="flex flex-wrap gap-2">
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><CreditCard size={20}/></div>
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><span className="font-bold text-xs">VISA</span></div>
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><span className="font-bold text-xs">MOMO</span></div>
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><span className="font-bold text-xs">COD</span></div>
          </div>
          <h3 className="font-bold uppercase text-zinc-900 mt-2">{t.footer.shipping_delivery}</h3>
          <div className="flex flex-wrap gap-2">
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><TruckIcon /></div>
             <div className="flex h-8 w-12 items-center justify-center rounded border bg-white shadow-sm"><span className="font-bold text-[10px]">EXPRESS</span></div>
          </div>
        </div>

        {/* Column 4: Follow Us & App */}
        <div className="flex flex-col gap-4">
          <h3 className="font-bold uppercase text-zinc-900">{t.footer.follow_us}</h3>
          <ul className="flex flex-col gap-2">
            <li><a href="#" className="flex items-center gap-2 hover:text-emerald-600"><Facebook size={16}/> Facebook</a></li>
            <li><a href="#" className="flex items-center gap-2 hover:text-emerald-600"><Instagram size={16}/> Instagram</a></li>
            <li><a href="#" className="flex items-center gap-2 hover:text-emerald-600"><Twitter size={16}/> Twitter</a></li>
          </ul>
        </div>
      </div>

      {/* Bottom Footer */}
      <div className="bg-zinc-100 py-4">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 text-xs text-zinc-500 sm:flex-row">
          <span>© {new Date().getFullYear()} ShopeeClone. {t.footer.rights_reserved}</span>
          <div className="flex gap-4">
            <span>{t.footer.region}</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

// Simple icon component for internal use
const TruckIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10 17h4V5H2v12h3"/><path d="M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5"/><path d="M14 17h1"/><circle cx="7.5" cy="17.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg>
);

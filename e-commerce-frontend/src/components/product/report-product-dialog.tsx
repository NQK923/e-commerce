'use client';

import React, { useState } from 'react';
import { Flag, X } from 'lucide-react';
import { Button } from '../ui/button';
import { reportApi, ReportReason } from '@/src/api/reportApi';
import { useToast } from '../ui/toast-provider';
import { Spinner } from '../ui/spinner';
import { useTranslation } from "@/src/providers/language-provider";

type ReportProductDialogProps = {
  productId: string;
  productName: string;
  children?: React.ReactNode;
};

export const ReportProductDialog: React.FC<ReportProductDialogProps> = ({ productId, productName, children }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [reason, setReason] = useState<ReportReason>('FAKE');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();
  const { t } = useTranslation();

  const REASONS: { value: ReportReason; label: string }[] = [
    { value: 'FAKE', label: t.product.report_reasons.fake },
    { value: 'INAPPROPRIATE', label: t.product.report_reasons.inappropriate },
    { value: 'SCAM', label: t.product.report_reasons.scam },
    { value: 'OTHER', label: t.product.report_reasons.other },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await reportApi.create({ productId, reason, description });
      
      addToast(t.product.report_success, 'success');
      setIsOpen(false);
      setReason('FAKE');
      setDescription('');
    } catch {
      addToast(t.common.error, 'error');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) {
    if (children) {
      return <div onClick={() => setIsOpen(true)} className="cursor-pointer inline-block">{children}</div>;
    }
    return (
      <button 
        onClick={() => setIsOpen(true)}
        className="flex items-center gap-2 text-sm text-zinc-500 hover:text-rose-600 transition-colors mt-2"
      >
        <Flag size={16} />
        {t.product.report_action}
      </button>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 animate-in fade-in duration-200">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between p-4 border-b border-zinc-100">
          <h3 className="font-semibold text-zinc-900">{t.product.report_title}</h3>
          <button 
            onClick={() => setIsOpen(false)}
            className="text-zinc-400 hover:text-zinc-600 transition-colors"
          >
            <X size={20} />
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          <p className="text-sm text-zinc-600">
            {t.product.report_description.replace("{{name}}", "")} <span className="font-medium text-zinc-900">{productName}</span>. 
          </p>

          <div className="space-y-2">
            <label className="text-sm font-medium text-zinc-900">{t.product.report_reason_label}</label>
            <select
              value={reason}
              onChange={(e) => setReason(e.target.value as ReportReason)}
              className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
              {REASONS.map((r) => (
                <option key={r.value} value={r.value}>{r.label}</option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
             <label className="text-sm font-medium text-zinc-900">{t.product.report_desc_label}</label>
             <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                placeholder={t.product.report_placeholder}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
             />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button 
                type="button" 
                variant="ghost" 
                onClick={() => setIsOpen(false)}
                disabled={loading}
            >
                {t.common.cancel}
            </Button>
            <Button 
                type="submit" 
                className="bg-rose-600 hover:bg-rose-700 text-white"
                disabled={loading}
            >
                {loading ? <span className="flex items-center gap-2"><Spinner className="h-4 w-4" /> {t.common.submitting}</span> : t.product.report_submit}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

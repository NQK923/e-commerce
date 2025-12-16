'use client';

import React, { useState } from 'react';
import { Flag, X } from 'lucide-react';
import { Button } from '../ui/button';
import { reportApi, ReportReason } from '@/src/api/reportApi';
import { useToast } from '../ui/toast-provider';
import { Spinner } from '../ui/spinner';

type ReportProductDialogProps = {
  productId: string;
  productName: string;
  children?: React.ReactNode;
};

const REASONS: { value: ReportReason; label: string }[] = [
  { value: 'FAKE', label: 'Fake or Counterfeit Product' },
  { value: 'INAPPROPRIATE', label: 'Inappropriate Content' },
  { value: 'SCAM', label: 'Potential Scam' },
  { value: 'OTHER', label: 'Other Reason' },
];

export const ReportProductDialog: React.FC<ReportProductDialogProps> = ({ productId, productName, children }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [reason, setReason] = useState<ReportReason>('FAKE');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await reportApi.create({ productId, reason, description });
      
      addToast('Thank you for your report. We will investigate.', 'success');
      setIsOpen(false);
      setReason('FAKE');
      setDescription('');
    } catch {
      addToast('Failed to submit report. Please try again.', 'error');
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
        Report this product
      </button>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 animate-in fade-in duration-200">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between p-4 border-b border-zinc-100">
          <h3 className="font-semibold text-zinc-900">Report Product</h3>
          <button 
            onClick={() => setIsOpen(false)}
            className="text-zinc-400 hover:text-zinc-600 transition-colors"
          >
            <X size={20} />
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          <p className="text-sm text-zinc-600">
            You are reporting <span className="font-medium text-zinc-900">{productName}</span>. 
            Please select a reason.
          </p>

          <div className="space-y-2">
            <label className="text-sm font-medium text-zinc-900">Reason</label>
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
             <label className="text-sm font-medium text-zinc-900">Description (Optional)</label>
             <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                placeholder="Please provide more details..."
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
                Cancel
            </Button>
            <Button 
                type="submit" 
                className="bg-rose-600 hover:bg-rose-700 text-white"
                disabled={loading}
            >
                {loading ? <span className="flex items-center gap-2"><Spinner className="h-4 w-4" /> Submitting...</span> : 'Submit Report'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

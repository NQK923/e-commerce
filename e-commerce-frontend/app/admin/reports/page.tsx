'use client';

import React, { Suspense, useEffect, useState } from 'react';
import Link from 'next/link';
import { reportApi, ProductReport } from '@/src/api/reportApi';
import { Button } from '@/src/components/ui/button';
import { Badge } from '@/src/components/ui/badge';
import { Spinner } from '@/src/components/ui/spinner';
import { useToast } from '@/src/components/ui/toast-provider';
import { CheckCircle, XCircle, ExternalLink } from 'lucide-react';

function ReportsContent() {
  const [reports, setReports] = useState<ProductReport[]>([]);
  const [loading, setLoading] = useState(true);
  const { addToast } = useToast();

  useEffect(() => {
    loadReports();
  }, []);

  const loadReports = async () => {
    try {
      setLoading(true);
      const data = await reportApi.list();
      setReports(data);
    } catch (error) {
      console.error('Failed to load reports', error);
      addToast('Failed to load reports', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (id: string, action: 'resolve' | 'reject') => {
    try {
      if (action === 'resolve') {
        await reportApi.resolve(id);
        addToast('Report resolved', 'success');
      } else {
        await reportApi.reject(id);
        addToast('Report rejected', 'success');
      }
      // Update local state
      setReports(prev => prev.map(r => 
        r.id === id ? { ...r, status: action === 'resolve' ? 'RESOLVED' : 'REJECTED' } : r
      ));
    } catch (error) {
      addToast(`Failed to ${action} report`, 'error');
    }
  };

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Product Reports</h1>
        <p className="text-sm text-zinc-500">Manage user-submitted product reports.</p>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-50 text-zinc-500 font-medium">
            <tr>
              <th className="px-6 py-4">Reason</th>
              <th className="px-6 py-4">Product ID</th>
              <th className="px-6 py-4">Description</th>
              <th className="px-6 py-4">Status</th>
              <th className="px-6 py-4">Date</th>
              <th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {reports.map((report) => (
              <tr key={report.id} className="hover:bg-zinc-50/50">
                <td className="px-6 py-4">
                  <Badge tone={report.reason === 'SCAM' || report.reason === 'FAKE' ? 'danger' : 'warning'}>
                    {report.reason}
                  </Badge>
                </td>
                <td className="px-6 py-4 font-mono text-xs">
                  <Link href={`/products/${report.productId}`} target="_blank" className="flex items-center gap-1 hover:text-emerald-600">
                    {report.productId.slice(0, 8)}...
                    <ExternalLink size={12} />
                  </Link>
                </td>
                <td className="px-6 py-4 max-w-xs truncate" title={report.description}>
                  {report.description || <span className="text-zinc-400 italic">No description</span>}
                </td>
                <td className="px-6 py-4">
                  <Badge tone={report.status === 'RESOLVED' ? 'success' : report.status === 'REJECTED' ? 'default' : 'warning'}>
                    {report.status}
                  </Badge>
                </td>
                <td className="px-6 py-4 text-zinc-500">
                  {new Date(report.createdAt).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 text-right">
                  {report.status === 'PENDING' && (
                    <div className="flex justify-end gap-2">
                      <Button 
                        size="icon" 
                        variant="ghost" 
                        className="text-emerald-600 hover:bg-emerald-50 h-8 w-8"
                        onClick={() => handleAction(report.id, 'resolve')}
                        title="Resolve"
                      >
                        <CheckCircle size={18} />
                      </Button>
                      <Button 
                        size="icon" 
                        variant="ghost" 
                        className="text-red-600 hover:bg-red-50 h-8 w-8"
                        onClick={() => handleAction(report.id, 'reject')}
                        title="Reject"
                      >
                        <XCircle size={18} />
                      </Button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
            {reports.length === 0 && (
              <tr>
                <td colSpan={6} className="text-center py-8 text-zinc-500">No reports found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function ReportsPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <ReportsContent />
    </Suspense>
  );
}

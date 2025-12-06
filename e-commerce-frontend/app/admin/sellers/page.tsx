'use client';

import React, { Suspense } from "react";
import { sellerApi } from "@/src/api/sellerApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { SellerApplication } from "@/src/types/seller";
import { MoreHorizontal, Store, CheckCircle2, XCircle } from "lucide-react";
import { useToast } from "@/src/components/ui/toast-provider";

function SellersContent() {
  const { addToast } = useToast();
  const [requests, setRequests] = React.useState<SellerApplication[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const loadRequests = async () => {
      try {
        const data = await sellerApi.listApplications();
        setRequests(data);
      } catch (error) {
        console.error("Failed to load seller requests", error);
      } finally {
        setLoading(false);
      }
    };
    void loadRequests();
  }, []);

  const handleReview = async (id: string, approve: boolean) => {
    try {
      const updated = approve ? await sellerApi.approve(id) : await sellerApi.reject(id);
      setRequests(prev => prev.map(req => req.id === id ? updated : req));
      addToast(approve ? "Approved seller application" : "Rejected seller application", "success");
    } catch (error) {
        addToast("Failed to update application", "error");
        console.error(error);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Sellers</h1>
          <p className="text-sm text-zinc-500">Manage seller applications and stores.</p>
        </div>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-50 text-zinc-500 font-medium">
            <tr>
              <th className="px-6 py-4">Store</th>
              <th className="px-6 py-4">Contact</th>
              <th className="px-6 py-4">Category</th>
              <th className="px-6 py-4">Status</th>
              <th className="px-6 py-4">Date</th>
              <th className="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {requests.map((req) => (
              <tr key={req.id} className="hover:bg-zinc-50/50">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-zinc-100 flex items-center justify-center overflow-hidden">
                       {req.avatarUrl ? <img src={req.avatarUrl} alt="" className="h-full w-full object-cover"/> : <Store size={16} className="text-zinc-400"/>}
                    </div>
                    <div>
                      <div className="font-semibold text-zinc-900">{req.storeName}</div>
                      <div className="text-xs text-zinc-500">ID: {req.id.slice(0,8)}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                    <div className="text-zinc-900">{req.contactEmail}</div>
                    <div className="text-xs text-zinc-500">{req.phone}</div>
                </td>
                <td className="px-6 py-4">{req.category}</td>
                <td className="px-6 py-4">
                    <Badge tone={req.status === "APPROVED" ? "success" : req.status === "REJECTED" ? "danger" : "warning"}>
                        {req.status}
                    </Badge>
                </td>
                <td className="px-6 py-4 text-zinc-500">
                   {req.createdAt ? new Date(req.createdAt).toLocaleDateString() : 'N/A'}
                </td>
                <td className="px-6 py-4">
                   <div className="flex gap-2">
                      {req.status === "PENDING" && (
                        <>
                            <Button size="icon" variant="ghost" className="h-8 w-8 text-emerald-600" onClick={() => handleReview(req.id, true)}>
                                <CheckCircle2 size={18} />
                            </Button>
                            <Button size="icon" variant="ghost" className="h-8 w-8 text-red-600" onClick={() => handleReview(req.id, false)}>
                                <XCircle size={18} />
                            </Button>
                        </>
                      )}
                      <Button variant="ghost" size="icon" className="h-8 w-8">
                        <MoreHorizontal size={16} />
                      </Button>
                   </div>
                </td>
              </tr>
            ))}
            {requests.length === 0 && (
                <tr>
                    <td colSpan={6} className="text-center py-8 text-zinc-500">No applications found.</td>
                </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function SellersPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <SellersContent />
    </Suspense>
  );
}

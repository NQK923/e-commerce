"use client";

import React, { useEffect, useState } from "react";
import { adminApi, OutboxEvent } from "@/src/api/adminApi";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { Spinner } from "@/src/components/ui/spinner";
import { Button } from "@/src/components/ui/button";
import { useToast } from "@/src/components/ui/toast-provider";
import { Badge } from "@/src/components/ui/badge";
import { useTranslation } from "@/src/providers/language-provider";

export default function SystemDashboardPage() {
  const { user, initializing } = useRequireAuth("/login");
  const [events, setEvents] = useState<OutboxEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const { addToast } = useToast();
  const { t } = useTranslation();

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const data = await adminApi.outbox.getFailed(0, 50);
      setEvents(data.content);
    } catch (err) {
      console.error(err);
      addToast(t.admin.toast.load_failed, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!initializing && user?.roles?.includes("ADMIN")) {
      fetchEvents();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initializing, user]);

  const handleRetry = async (id: string) => {
    try {
      await adminApi.outbox.retry(id);
      addToast(t.admin.toast.retry_queued, "success");
      fetchEvents();
    } catch (err) {
      console.error(err);
      addToast(t.admin.toast.retry_failed, "error");
    }
  };

  if (initializing || loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!user?.roles?.includes("ADMIN")) {
    return <div>{t.admin.access_denied}</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight text-zinc-900">{t.admin.system_dashboard}</h1>
        <Button onClick={fetchEvents} variant="outline">
          {t.admin.refresh}
        </Button>
      </div>

      <div className="rounded-lg border border-zinc-200 bg-white">
        <div className="px-6 py-4 border-b border-zinc-200">
          <h2 className="text-lg font-semibold text-zinc-900">{t.admin.failed_outbox}</h2>
          <p className="text-sm text-zinc-500">{t.admin.failed_outbox_desc}</p>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left text-zinc-500">
            <thead className="text-xs text-zinc-700 uppercase bg-zinc-50 border-b border-zinc-200">
              <tr>
                <th scope="col" className="px-6 py-3">{t.admin.type}</th>
                <th scope="col" className="px-6 py-3">{t.admin.aggregate_id}</th>
                <th scope="col" className="px-6 py-3">{t.admin.status}</th>
                <th scope="col" className="px-6 py-3">{t.admin.attempts}</th>
                <th scope="col" className="px-6 py-3">{t.admin.last_error}</th>
                <th scope="col" className="px-6 py-3 text-right">{t.admin.actions}</th>
              </tr>
            </thead>
            <tbody>
              {events.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-zinc-500">
                    {t.admin.no_failed_events}
                  </td>
                </tr>
              ) : (
                events.map((evt) => (
                  <tr key={evt.id} className="bg-white border-b hover:bg-zinc-50">
                    <td className="px-6 py-4 font-medium text-zinc-900 whitespace-nowrap">{evt.type}</td>
                    <td className="px-6 py-4 text-sm text-zinc-500">{evt.aggregateId}</td>
                    <td className="px-6 py-4">
                      <Badge tone={evt.status === "DEAD_LETTER" ? "danger" : "default"}>
                        {evt.status}
                      </Badge>
                    </td>
                    <td className="px-6 py-4">{evt.attemptCount}</td>
                    <td className="px-6 py-4 max-w-xs truncate text-xs text-red-600" title={evt.lastError}>
                      {evt.lastError || "N/A"}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Button size="sm" onClick={() => handleRetry(evt.id)}>
                        {t.admin.retry}
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

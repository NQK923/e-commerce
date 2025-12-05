'use client';

import { Button } from "@/src/components/ui/button";

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Settings</h1>
          <p className="text-sm text-zinc-500">Configure system-wide settings.</p>
        </div>
      </div>

      <div className="grid gap-6 max-w-2xl">
        <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold mb-4">General Settings</h2>
            <div className="space-y-4">
                <div className="grid gap-2">
                    <label className="text-sm font-medium">Site Name</label>
                    <input type="text" className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:ring-2 focus:ring-emerald-500 focus:outline-none" defaultValue="E-Commerce Platform" />
                </div>
                 <div className="grid gap-2">
                    <label className="text-sm font-medium">Support Email</label>
                    <input type="email" className="w-full px-3 py-2 rounded-lg border border-zinc-300 focus:ring-2 focus:ring-emerald-500 focus:outline-none" defaultValue="support@example.com" />
                </div>
            </div>
            <div className="mt-6">
                <Button>Save Changes</Button>
            </div>
        </div>

         <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold mb-4 text-red-600">Danger Zone</h2>
             <p className="text-sm text-zinc-500 mb-4">Irreversible actions.</p>
            <Button variant="outline" className="text-red-600 border-red-200 hover:bg-red-50">Clear System Cache</Button>
        </div>
      </div>
    </div>
  );
}

'use client';

import React, { useState } from "react";
import { Button } from "@/src/components/ui/button";
import { useToast } from "@/src/components/ui/toast-provider";
import { Input } from "@/src/components/ui/input";

export default function SettingsPage() {
  const { addToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [settings, setSettings] = useState({
    siteName: "E-Commerce Platform",
    supportEmail: "support@example.com",
  });

  const handleSave = async () => {
    setLoading(true);
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 800));
    addToast("Settings saved successfully", "success");
    setLoading(false);
  };

  const handleClearCache = () => {
    if(confirm("Are you sure you want to clear the system cache? This might affect performance temporarily.")) {
        addToast("System cache cleared", "success");
    }
  };

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
                <Input 
                    label="Site Name" 
                    value={settings.siteName} 
                    onChange={(e) => setSettings({...settings, siteName: e.target.value})}
                />
                <Input 
                    label="Support Email" 
                    type="email"
                    value={settings.supportEmail} 
                    onChange={(e) => setSettings({...settings, supportEmail: e.target.value})}
                />
            </div>
            <div className="mt-6">
                <Button onClick={handleSave} disabled={loading}>
                    {loading ? "Saving..." : "Save Changes"}
                </Button>
            </div>
        </div>

         <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold mb-4 text-red-600">Danger Zone</h2>
             <p className="text-sm text-zinc-500 mb-4">Irreversible actions.</p>
            <Button 
                variant="outline" 
                className="text-red-600 border-red-200 hover:bg-red-50"
                onClick={handleClearCache}
            >
                Clear System Cache
            </Button>
        </div>
      </div>
    </div>
  );
}

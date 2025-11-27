'use client';

import React, { useEffect, useState } from "react";
import { profileApi } from "@/src/api/profileApi";
import { Button } from "@/src/components/ui/button";
import { Input } from "@/src/components/ui/input";
import { Spinner } from "@/src/components/ui/spinner";
import { useRequireAuth } from "@/src/hooks/use-require-auth";
import { useAuth } from "@/src/store/auth-store";
import { useToast } from "@/src/components/ui/toast-provider";

export default function ProfilePage() {
  const { user, setUserProfile } = useAuth();
  const { isAuthenticated, initializing } = useRequireAuth();
  const { addToast } = useToast();
  const [displayName, setDisplayName] = useState(user?.displayName ?? "");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user?.displayName) {
      setDisplayName(user.displayName);
    }
  }, [user?.displayName]);

  if (initializing || !isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-sm text-zinc-600">
        <Spinner />
        Loading profile...
      </div>
    );
  }

  if (!user) {
    return (
      <div className="mx-auto flex max-w-3xl flex-col items-center gap-3 px-4 py-10 text-center">
        <p className="text-lg font-semibold text-black">Unable to load profile.</p>
      </div>
    );
  }

  const handleUpdate = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      const updated = await profileApi.update({ displayName });
      setUserProfile(updated);
      addToast("Profile updated", "success");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Failed to update profile";
      addToast(message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6 px-4 py-10">
      <div>
        <h1 className="text-3xl font-bold text-black">Profile</h1>
        <p className="text-sm text-zinc-600">Manage your account information.</p>
      </div>
      <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <div className="text-xs uppercase text-zinc-500">Email</div>
            <div className="text-sm font-semibold text-black">{user.email}</div>
          </div>
          <div>
            <div className="text-xs uppercase text-zinc-500">Provider</div>
            <div className="text-sm font-semibold text-black">{user.provider}</div>
          </div>
        </div>
        <form className="mt-6 grid gap-4" onSubmit={handleUpdate}>
          <Input
            label="Display name"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            required
          />
          <Button type="submit" disabled={loading}>
            {loading ? "Saving..." : "Save changes"}
          </Button>
        </form>
      </div>
    </div>
  );
}

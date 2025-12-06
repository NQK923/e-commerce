'use client';

import React, { Suspense } from "react";
import { adminApi } from "@/src/api/adminApi";
import { Badge } from "@/src/components/ui/badge";
import { Button } from "@/src/components/ui/button";
import { Spinner } from "@/src/components/ui/spinner";
import { User } from "@/src/types/auth";
import { ShieldCheck, MoreHorizontal, Trash2 } from "lucide-react";
import { useToast } from "@/src/components/ui/toast-provider";

function UsersContent() {
  const { addToast } = useToast();
  const [users, setUsers] = React.useState<User[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const loadUsers = async () => {
      try {
        const data = await adminApi.users();
        setUsers(data);
      } catch (error) {
        console.error("Failed to load users", error);
      } finally {
        setLoading(false);
      }
    };
    void loadUsers();
  }, []);

  const handleDelete = async (userId: string) => {
    if (confirm("Are you sure you want to delete this user?")) {
        // Simulate API call
        setUsers(users.filter(u => u.id !== userId));
        addToast("User deleted successfully", "success");
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
          <h1 className="text-2xl font-bold text-zinc-900">Users</h1>
          <p className="text-sm text-zinc-500">Manage all registered users.</p>
        </div>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white shadow-sm overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-zinc-50 text-zinc-500 font-medium">
            <tr>
              <th className="px-6 py-4">User</th>
              <th className="px-6 py-4">Role</th>
              <th className="px-6 py-4">Status</th>
              <th className="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-zinc-50/50">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="h-8 w-8 rounded-full bg-zinc-100 flex items-center justify-center text-xs font-bold text-zinc-500">
                      {user.displayName?.charAt(0) || user.email?.charAt(0) || "U"}
                    </div>
                    <div>
                      <div className="font-semibold text-zinc-900">{user.displayName || "No Name"}</div>
                      <div className="text-xs text-zinc-500">{user.email}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex gap-1">
                    {user.roles?.map((role) => (
                      <Badge key={role} tone={role === "ADMIN" ? "danger" : role === "SELLER" ? "warning" : "default"}>
                        {role}
                      </Badge>
                    ))}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <Badge tone="success">Active</Badge>
                </td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                      <Button variant="ghost" size="icon" onClick={() => handleDelete(user.id)} className="text-red-600 hover:bg-red-50">
                        <Trash2 size={16} />
                      </Button>
                      <Button variant="ghost" size="icon">
                        <MoreHorizontal size={16} />
                      </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function UsersPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <UsersContent />
    </Suspense>
  );
}

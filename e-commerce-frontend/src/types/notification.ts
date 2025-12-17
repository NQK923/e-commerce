export type Notification = {
  id: string;
  userId: string;
  title: string;
  body: string;
  channel: "EMAIL" | "SMS" | "PUSH";
  status: "UNREAD" | "READ";
  createdAt?: string;
  readAt?: string | null;
};

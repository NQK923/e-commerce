import { supabase } from "./supabase-client";

export const uploadToBucket = async (bucket: string, file: File): Promise<string> => {
  if (!supabase) {
    throw new Error("Supabase is not configured");
  }
  const ext = file.name.split(".").pop() ?? "bin";
  const path = `${crypto.randomUUID()}.${ext}`;
  const { data, error } = await supabase.storage.from(bucket).upload(path, file, {
    cacheControl: "3600",
    upsert: false,
  });
  if (error) {
    throw error;
  }
  const { data: publicUrl } = supabase.storage.from(bucket).getPublicUrl(data.path);
  return publicUrl.publicUrl;
};

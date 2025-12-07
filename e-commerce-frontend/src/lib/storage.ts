import { supabase } from "./supabase-client";
import { config } from "../config/env";

export const uploadToBucket = async (bucket: string | undefined, file: File): Promise<string> => {
  if (!supabase) {
    throw new Error("Supabase is not configured");
  }
  const bucketName = (bucket || config.supabaseProductBucket || "product-images").toLowerCase();
  const ext = file.name.split(".").pop() ?? "bin";
  const path = `${crypto.randomUUID()}.${ext}`;
  const { data, error } = await supabase.storage.from(bucketName).upload(path, file, {
    cacheControl: "3600",
    upsert: false,
    contentType: file.type,
  });
  if (error) {
    console.error("Supabase Storage Upload Error:", error);
    const message = error.message || "Upload failed";
    if (message.toLowerCase().includes("bucket")) {
      throw new Error(`Supabase bucket "${bucketName}" is missing or misconfigured`);
    }
    throw new Error(message);
  }
  const { data: publicUrl } = supabase.storage.from(bucketName).getPublicUrl(data.path);
  return publicUrl.publicUrl;
};

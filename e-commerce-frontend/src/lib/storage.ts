import { supabase } from "./supabase-client";
import { config } from "../config/env";

export const uploadToBucket = async (bucket: string | undefined, file: File): Promise<string> => {
  const bucketName = (bucket || config.supabaseProductBucket || "product-images").toLowerCase();

  if (!supabase) {
    if (config.uploadFallbackMode === "placeholder") {
      return placeholderUploadUrl(bucketName, file);
    }
    throw new Error(
      "Supabase is not configured. Set NEXT_PUBLIC_SUPABASE_URL/NEXT_PUBLIC_SUPABASE_ANON_KEY or enable NEXT_PUBLIC_UPLOAD_FALLBACK_MODE=placeholder for local smoke tests.",
    );
  }

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

const placeholderUploadUrl = (bucketName: string, file: File): string => {
  const params = new URLSearchParams({
    bucket: bucketName,
    file: file.name || "local-upload",
  });

  return `/upload-placeholder.svg?${params.toString()}`;
};

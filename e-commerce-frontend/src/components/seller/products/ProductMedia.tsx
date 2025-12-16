import React, { useRef } from "react";
import Image from "next/image";
import { ImagePlus, RefreshCw, Trash2 } from "lucide-react";

interface ProductMediaProps {
  images: string[]; // [0] is cover, rest are gallery
  onSetCover: (file: File) => void;
  onAddGallery: (files: File[]) => void;
  onRemove: (index: number) => void;
  onReplace: (index: number, file: File) => void;
  error?: string;
  uploading?: boolean;
}

export function ProductMedia({
  images,
  onSetCover,
  onAddGallery,
  onRemove,
  onReplace,
  error,
  uploading = false,
}: ProductMediaProps) {
  const coverInputRef = useRef<HTMLInputElement>(null);
  const galleryInputRef = useRef<HTMLInputElement>(null);

  const coverImage = images[0];
  const galleryImages = images.slice(1);

  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      onSetCover(file);
    }
    if (coverInputRef.current) coverInputRef.current.value = "";
  };

  const handleGalleryAdd = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files ?? []);
    if (files.length > 0) {
      onAddGallery(files);
    }
    if (galleryInputRef.current) galleryInputRef.current.value = "";
  };

  const handleReplace = (index: number, file: File) => {
     onReplace(index, file);
  }

  return (
    <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm space-y-8">
      <div>
          <h2 className="text-lg font-bold text-zinc-900 mb-1">Hình ảnh sản phẩm</h2>
          <p className="text-sm text-zinc-500">
            Quản lý ảnh bìa và thư viện ảnh chi tiết của sản phẩm.
          </p>
      </div>

      {/* Cover Image Section */}
      <div className="space-y-3">
        <label className="text-sm font-medium text-zinc-700">Ảnh bìa <span className="text-red-500">*</span></label>
        <div className="flex items-start gap-6">
            <div className="relative group h-64 w-64 flex-shrink-0 overflow-hidden rounded-xl border-2 border-dashed border-zinc-200 bg-zinc-50">
                {coverImage ? (
                    <>
                        <Image
                            src={coverImage}
                            alt="Cover image"
                            fill
                            className="object-cover"
                        />
                        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-2">
                             <button
                                type="button"
                                onClick={() => coverInputRef.current?.click()}
                                className="flex items-center gap-2 rounded-full bg-white/90 px-4 py-2 text-xs font-medium text-zinc-900 hover:bg-white"
                             >
                                <RefreshCw size={14} /> Thay đổi
                             </button>
                             <button
                                type="button"
                                onClick={() => onRemove(0)}
                                className="flex items-center gap-2 rounded-full bg-red-500/90 px-4 py-2 text-xs font-medium text-white hover:bg-red-500"
                             >
                                <Trash2 size={14} /> Xóa ảnh
                             </button>
                        </div>
                    </>
                ) : (
                    <button
                        type="button"
                        onClick={() => coverInputRef.current?.click()}
                        className="flex h-full w-full flex-col items-center justify-center gap-2 text-zinc-400 hover:bg-zinc-100 transition-colors"
                    >
                        <ImagePlus size={32} />
                        <span className="text-sm font-medium">Tải ảnh bìa</span>
                    </button>
                )}
                <input
                    ref={coverInputRef}
                    type="file"
                    className="hidden"
                    accept="image/*"
                    onChange={handleCoverChange}
                    disabled={uploading}
                />
            </div>
            <div className="text-sm text-zinc-500 pt-2">
                <p>Ảnh bìa là hình ảnh đầu tiên khách hàng nhìn thấy.</p>
                <p className="mt-1">Kích thước khuyên dùng: 600x600px trở lên.</p>
            </div>
        </div>
      </div>

      {/* Gallery Section */}
      <div className="space-y-3 pt-4 border-t border-zinc-100">
        <label className="text-sm font-medium text-zinc-700">Thư viện ảnh ({galleryImages.length})</label>
        
        <div className="flex flex-wrap gap-4">
             {/* Gallery Grid */}
             {galleryImages.map((url, idx) => {
                 const realIndex = idx + 1; // 0 is cover
                 return (
                    <GalleryItem 
                        key={`${url}-${realIndex}`}
                        url={url}
                        index={realIndex}
                        onRemove={() => onRemove(realIndex)}
                        onReplace={(file) => handleReplace(realIndex, file)}
                    />
                 );
             })}

             {/* Add Button */}
            <label className="flex h-32 w-32 cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-zinc-300 bg-zinc-50 hover:bg-zinc-100 transition-colors">
              <span className="text-xs text-zinc-500 text-center px-1">
                {uploading ? "..." : "+ Thêm ảnh"}
              </span>
              <input
                ref={galleryInputRef}
                type="file"
                className="hidden"
                accept="image/*"
                multiple
                disabled={uploading}
                onChange={handleGalleryAdd}
              />
            </label>
        </div>
      </div>

      {error && <span className="text-sm text-red-500 bg-red-50 px-3 py-2 rounded-lg block">{error}</span>}
    </div>
  );
}

function GalleryItem({ url, index, onRemove, onReplace }: { url: string, index: number, onRemove: () => void, onReplace: (file: File) => void }) {
    const inputRef = useRef<HTMLInputElement>(null);
    
    return (
        <div className="group relative h-32 w-32 overflow-hidden rounded-xl border border-zinc-200 bg-white">
            <Image
                src={url}
                alt={`Gallery image ${index}`}
                fill
                className="object-cover"
            />
            
            {/* Overlay Actions */}
            <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                <button
                    type="button"
                    onClick={() => inputRef.current?.click()}
                    className="p-1.5 rounded-full bg-white/90 text-zinc-700 hover:text-emerald-600 hover:bg-white"
                    title="Thay đổi"
                >
                    <RefreshCw size={14} />
                </button>
                <button
                    type="button"
                    onClick={onRemove}
                    className="p-1.5 rounded-full bg-white/90 text-zinc-700 hover:text-red-600 hover:bg-white"
                    title="Xóa"
                >
                    <Trash2 size={14} />
                </button>
            </div>
             <input
                ref={inputRef}
                type="file"
                className="hidden"
                accept="image/*"
                onChange={(e) => {
                    const file = e.target.files?.[0];
                    if(file) onReplace(file);
                    // reset?
                    if(inputRef.current) inputRef.current.value = "";
                }}
            />
        </div>
    )
}
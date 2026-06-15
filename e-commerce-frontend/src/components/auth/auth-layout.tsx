import React from "react";
import Link from "next/link";
import Image from "next/image";

type AuthLayoutProps = {
  children: React.ReactNode;
  title: string;
  subtitle: string;
};

export const AuthLayout: React.FC<AuthLayoutProps> = ({ children, title, subtitle }) => {
  return (
    <div className="flex min-h-screen bg-white">
      {/* Left side: Editorial Image */}
      <div className="hidden lg:block lg:w-1/2 relative bg-zinc-100">
        <Image 
          src="/images/auth-bg-v2.png"
          alt="Auth background"
          fill
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-black/20" />
        <div className="absolute bottom-12 left-12 text-white">
          <Link href="/" className="text-3xl font-bold tracking-tight">ECOMX.</Link>
          <p className="mt-4 max-w-sm text-sm text-white/80 font-light">
            Sự tinh tế trong từng đường nét. Khám phá phong cách tối giản và nâng tầm không gian sống của bạn.
          </p>
        </div>
      </div>

      {/* Right side: Form */}
      <div className="flex w-full flex-col justify-center px-8 py-12 lg:w-1/2 sm:px-12 xl:px-24">
        <div className="mx-auto w-full max-w-sm">
          {/* Header */}
          <div className="mb-10 text-center lg:text-left">
            <Link href="/" className="lg:hidden text-2xl font-bold tracking-tight text-black mb-8 block">
              ECOMX.
            </Link>
            <h2 className="text-3xl font-bold tracking-tight text-black">{title}</h2>
            <p className="mt-2 text-sm text-zinc-500">{subtitle}</p>
          </div>

          {/* Form Content (Children) */}
          <div className="w-full">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

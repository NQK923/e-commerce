import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { cookies } from "next/headers";
import { Suspense } from "react";
import "./globals.css";
import { AppProviders } from "@/src/providers/app-providers";
import { Header } from "@/src/components/layout/header";
import { Footer } from "@/src/components/layout/footer";
import { ProtectedRouteGuard } from "@/src/components/auth/protected-route-guard";

const LANGUAGE_COOKIE = "ecommerce_lang_v2";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "E-Commerce",
  description: "Production-ready e-commerce frontend",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const cookieStore = await cookies();
  const savedLang = cookieStore.get(LANGUAGE_COOKIE)?.value;
  const initialLanguage = savedLang === "en" || savedLang === "vi" ? savedLang : "vi";

  return (
    <html lang={initialLanguage}>
      <body className={`${geistSans.variable} ${geistMono.variable} bg-zinc-50 text-zinc-900 antialiased`}>
        <AppProviders initialLanguage={initialLanguage}>
          <Suspense fallback={null}>
            <ProtectedRouteGuard />
          </Suspense>
          <Suspense fallback={null}>
            <Header />
          </Suspense>
          <main className="min-h-[70vh]">{children}</main>
          <Footer />
        </AppProviders>
      </body>
    </html>
  );
}

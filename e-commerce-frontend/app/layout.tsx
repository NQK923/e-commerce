import type { Metadata } from "next";
import { Roboto, Geist_Mono } from "next/font/google"; // Changed Inter to Roboto
import { cookies } from "next/headers";
import { Suspense } from "react";
import "./globals.css";
import { AppProviders } from "@/src/providers/app-providers";
import { Header } from "@/src/components/layout/header";
import { Footer } from "@/src/components/layout/footer";
import { ProtectedRouteGuard } from "@/src/components/auth/protected-route-guard";
import { ChatWidget } from "@/src/components/chat/chat-widget";

const LANGUAGE_COOKIE = "ecommerce_lang_v2";

const roboto = Roboto({ // Changed inter to roboto
  variable: "--font-roboto", // Changed variable name
  subsets: ["latin", "vietnamese"],
  weight: ["100", "300", "400", "500", "700", "900"], // Added weights for Roboto
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "EcomX",
  description: "Nền tảng thương mại điện tử chuyên nghiệp",
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
      <body className={`${roboto.variable} ${geistMono.variable} bg-zinc-50 text-zinc-900 antialiased`}>
        <AppProviders initialLanguage={initialLanguage}>
          <Suspense fallback={null}>
            <ProtectedRouteGuard />
          </Suspense>
        <Suspense fallback={null}>
          <Header />
        </Suspense>
        <main className="min-h-[70vh]">{children}</main>
        <Suspense fallback={null}>
          <ChatWidget />
        </Suspense>
        <Footer />
      </AppProviders>
    </body>
  </html>
  );
}

import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Link from "next/link";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Spring Boot + Redis Workshop",
  description: "Taller didáctico de Spring Boot y Redis",
};

const modules = [
  { href: "/cache", label: "Caché", ready: true },
  { href: "/ratelimit", label: "Rate limiting", ready: true },
  { href: "/leaderboard", label: "Leaderboard", ready: true },
  { label: "Sesiones", ready: false },
  { label: "Cola de trabajos", ready: false },
  { label: "Pub/Sub", ready: false },
  { label: "Lock distribuido", ready: false },
];
export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="es" className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <header className="border-b border-black/10 dark:border-white/10">
          <nav className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-4 gap-y-2 px-6 py-4">
            <Link href="/" className="font-semibold">
              Spring Boot + Redis
            </Link>
            <span className="text-black/20 dark:text-white/20">|</span>
            {modules.map((m) =>
              m.ready && m.href ? (
                <Link
                  key={m.label}
                  href={m.href}
                  className="text-sm text-black/60 hover:text-black dark:text-white/60 dark:hover:text-white"
                >
                  {m.label}
                </Link>
              ) : (
                <span
                  key={m.label}
                  title="Próximamente"
                  className="text-sm text-black/25 dark:text-white/25"
                >
                  {m.label}
                </span>
              )
            )}
          </nav>
        </header>
        <main className="mx-auto w-full max-w-5xl flex-1 px-6 py-8">{children}</main>
        <footer className="border-t border-black/10 px-6 py-4 text-center text-xs text-black/40 dark:border-white/10 dark:text-white/40">
          Taller didáctico — Redis / Spring Boot
        </footer>
      </body>
    </html>
  );
}

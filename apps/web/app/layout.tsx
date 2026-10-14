import type { Metadata } from "next";
import { VT323, Share_Tech_Mono } from "next/font/google";
import "./globals.css";
import Link from "next/link";

const vt323 = VT323({
  weight: "400",
  variable: "--font-vt323",
  subsets: ["latin"],
});

const shareTechMono = Share_Tech_Mono({
  weight: "400",
  variable: "--font-share-tech",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Spring Boot / Redis Workshop",
  description: "Taller didáctico de Spring Boot y Redis",
};

const modules = [
  { href: "/cache", label: "Caché" },
  { href: "/ratelimit", label: "Rate limiting" },
  { href: "/leaderboard", label: "Leaderboard" },
  { href: "/sessions", label: "Sesiones" },
  { href: "/jobs", label: "Cola de trabajos" },
  { href: "/pubsub", label: "Pub/Sub" },
  { href: "/lock", label: "Lock distribuido" },
];

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="es" className={`dark ${vt323.variable} ${shareTechMono.variable}`}>
      <body className="flex min-h-full flex-col">
        <header className="sticky top-0 z-40 border-b border-line bg-bg/85 backdrop-blur">
          <nav className="mx-auto flex max-w-6xl flex-wrap items-center justify-center gap-2 px-6 py-3">
            <Link
              href="/"
              className="glow-cyan mr-2 font-display text-2xl tracking-wider text-cyan"
            >
              ~/springboot-redis
            </Link>
            {modules.map((m) => (
              <Link
                key={m.href}
                href={m.href}
                className="rounded-sm border border-line px-3 py-1 text-sm text-dim transition-colors hover:border-cyan hover:text-cyan"
              >
                {m.label}
              </Link>
            ))}
          </nav>
        </header>
        <main className="relative z-10 mx-auto w-full max-w-6xl flex-1 px-6 py-8">
          {children}
        </main>
        <footer className="relative z-10 border-t border-line px-6 py-4 text-center text-xs text-dim">
          <span className="font-display tracking-[0.2em]">
            [ SPRING BOOT + REDIS — TALLER DIDÁCTICO ]
          </span>
        </footer>
      </body>
    </html>
  );
}

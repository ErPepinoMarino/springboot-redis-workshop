import Link from "next/link";

//Lista de los módulos del workshop
const modules = [
  {
    title: "Caché",
    description: "Cache-aside sobre Postgres con Redis: TTL, invalidación y hit ratio.",
    href: "/cache",
    ready: true,
  },
  {
    title: "Rate limiting",
    description: "Token bucket atómico con Redis (Lua): límite, cola y 429.",
    href: "/ratelimit",
    ready: true,
  },
  {
    title: "Leaderboard",
    description: "Rankings en tiempo real con sorted sets.",
    ready: false,
  },
  {
    title: "Sesiones",
    description: "Login con sesiones en Redis (hashes + TTL).",
    ready: false,
  },
  {
    title: "Cola de trabajos",
    description: "Encolar y procesar trabajo en background con listas.",
    ready: false,
  },
  {
    title: "Pub/Sub",
    description: "Notificaciones en vivo con canales y SSE.",
    ready: false,
  },
  {
    title: "Lock distribuido",
    description: "Exclusión mutua entre procesos con SET NX PX.",
    ready: false,
  },
];

const cardClass =
  "flex h-full flex-col gap-1 rounded-lg border border-black/10 p-4 dark:border-white/10";

export default function Home() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Módulos</h1>
        <p className="text-sm text-black/60 dark:text-white/60">
          Un mini-taller por concepto. Cada uno tiene su API y su página para trastear.
        </p>
      </section>

      <ul className="grid gap-4 sm:grid-cols-2">
        {modules.map((m) => {
          const content = (
            <>
              <span className="font-medium">{m.title}</span>
              <span className="text-sm text-black/60 dark:text-white/60">{m.description}</span>
              {!m.ready && (
                <span className="mt-1 text-xs text-black/40 dark:text-white/40">Próximamente</span>
              )}
            </>
          );

          return (
            <li key={m.title}>
              {m.ready && m.href ? (
                <Link
                  href={m.href}
                  className={`${cardClass} transition-colors hover:border-black/40 dark:hover:border-white/40`}
                >
                  {content}
                </Link>
              ) : (
                <div className={cardClass}>{content}</div>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}

import Link from "next/link";

const modules = [
  {
    title: "Caché",
    description: "Cache-aside sobre Postgres con Redis: TTL, invalidación y hit ratio.",
    href: "/cache",
  },
  {
    title: "Rate limiting",
    description: "Token bucket atómico con Redis (Lua): límite, cola y 429.",
    href: "/ratelimit",
  },
  {
    title: "Leaderboard",
    description: "Rankings en tiempo real con sorted sets.",
    href: "/leaderboard",
  },
  {
    title: "Sesiones",
    description: "Login con sesiones en Redis (hashes + TTL) y TTL en vivo.",
    href: "/sessions",
  },
  {
    title: "Cola de trabajos",
    description: "Encolar y procesar trabajo en background con streams y workers.",
    href: "/jobs",
  },
  {
    title: "Pub/Sub",
    description: "Eventos en vivo con canales de Redis y SSE.",
    href: "/pubsub",
  },
  {
    title: "Lock distribuido",
    description: "Exclusión mutua entre procesos con SET NX PX.",
    href: "/lock",
  },
];

export default function Home() {
  return (
    <div className="flex flex-col items-center gap-14">
      <section className="flex flex-col items-center gap-6 py-10 text-center">
        <h1 className="font-display leading-none">
          <span
            className="glitch glow-cyan block text-4xl text-cyan sm:text-5xl"
            data-text="Spring Boot / Redis"
          >
            Spring Boot / Redis
          </span>
          <span
            className="glitch glow-green mt-3 block text-8xl text-green sm:text-9xl"
            data-text="Workshop"
          >
            Workshop
          </span>
        </h1>

        <p className="max-w-2xl text-sm text-dim">
          Un mini-taller por concepto. Cada módulo tiene su API, sus tests y su página para
          trastear. El proyecto es la excusa; <span className="text-cyan">entender cada línea</span>{" "}
          es el resultado.
        </p>

        <span className="chip">
          <span className="text-neon-green">●</span> 7 módulos operativos
        </span>
      </section>

      <section className="flex w-full flex-col items-center gap-4">
        <h2 className="panel-title">{"// módulos"}</h2>
        <ul className="grid w-full gap-4 sm:grid-cols-2">
          {modules.map((m) => (
            <li key={m.href}>
              <Link
                href={m.href}
                className="panel group flex h-full flex-col items-center gap-1 text-center transition-colors hover:border-cyan/60"
              >
                <span className="font-display text-2xl tracking-wide text-cyan">{m.title}</span>
                <span className="text-sm text-dim">{m.description}</span>
                <span className="mt-2 font-mono text-xs text-green opacity-0 transition-opacity group-hover:opacity-100">
                  &gt; abrir_
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

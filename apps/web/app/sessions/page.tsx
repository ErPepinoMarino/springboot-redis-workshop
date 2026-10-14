"use client";

import { useCallback, useEffect, useState } from "react";
import { API_BASE, apiFetch } from "@/lib/api";

type SessionUser = { username: string; role: string };
type Counters = { public: number; authenticated: number; admin: number };

const greenButton =
  "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed";
const neutralButton =
  "rounded-md border border-black/20 px-3 py-2 text-sm font-medium transition-colors hover:bg-black/5 disabled:opacity-40 dark:border-white/20 dark:hover:bg-white/10";
const inputClass =
  "w-40 rounded-md border border-black/20 px-3 py-2 dark:border-white/20 dark:bg-transparent";

export default function SessionsPage() {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [counters, setCounters] = useState<Counters>({ public: 0, authenticated: 0, admin: 0 });
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [ttlMs, setTtlMs] = useState<number | null>(null);

  const sessionUser = user?.username ?? null;

  useEffect(() => {
    if (!sessionUser) {
      return;
    }
    const source = new EventSource(`${API_BASE}/sessions/ttl/stream`);
    source.onmessage = (event) => {
      const data = JSON.parse(event.data) as { ttl?: number; expired?: boolean };
      if (data.expired) {
        setUser(null);
        return;
      }
      setTtlMs(data.ttl ?? 0);
    };
    return () => source.close();
  }, [sessionUser]);

  const refreshMe = useCallback(async () => {
    const res = await apiFetch("/sessions/me");
    setUser(res.ok ? ((await res.json()) as SessionUser) : null);
  }, []);

  const refreshCounters = useCallback(async () => {
    const res = await apiFetch("/sessions/counters");
    if (res.ok) {
      setCounters((await res.json()) as Counters);
    }
  }, []);

  useEffect(() => {
    void (async () => {
      const me = await apiFetch("/sessions/me");
      setUser(me.ok ? ((await me.json()) as SessionUser) : null);

      const counters = await apiFetch("/sessions/counters");
      if (counters.ok) {
        setCounters((await counters.json()) as Counters);
      }
    })();
  }, []);

  async function login(u: string, p: string) {
    setBusy(true);
    setMessage(null);
    const res = await apiFetch("/sessions/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: u, password: p }),
    });
    if (res.ok) {
      setUser((await res.json()) as SessionUser);
      setTtlMs(null);
      await refreshCounters();
    } else {
      setMessage("Credenciales incorrectas.");
    }
    setBusy(false);
  }

  async function logout() {
    setBusy(true);
    await apiFetch("/sessions/logout", { method: "POST" });
    setUser(null);
    setTtlMs(null);
    setMessage(null);
    setBusy(false);
  }

  async function increment(name: keyof Counters) {
    const res = await apiFetch(`/sessions/counters/${name}/increment`, { method: "POST" });
    if (res.ok) {
      const { value } = (await res.json()) as { value: number };
      setCounters((c) => ({ ...c, [name]: value }));
    } else if (res.status === 401 || res.status === 403) {
      setMessage(res.status === 401 ? "Necesitas iniciar sesión." : "No tienes permisos.");
      await refreshMe();
    }
  }

  const isAdmin = user?.role === "ADMIN";

  const counterDefs = [
    { key: "public" as const, title: "Público", hint: "Cualquiera", enabled: true },
    {
      key: "authenticated" as const,
      title: "Autenticado",
      hint: "Requiere sesión",
      enabled: user !== null,
    },
    { key: "admin" as const, title: "Super usuario", hint: "Requiere rol ADMIN", enabled: isAdmin },
  ];

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Sesiones</h1>
        <p className="max-w-3xl text-sm text-black/60 dark:text-white/60">
          Login con la sesión guardada en <strong>Redis</strong> (hash + TTL). Tres contadores con
          distinto nivel de acceso: <strong>público</strong>, <strong>autenticado</strong> y{" "}
          <strong>super usuario</strong>. La sesión caduca sola (TTL).
        </p>
      </section>

      <section className="rounded-lg border border-black/10 p-4 dark:border-white/10">
        {user ? (
          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm">
              Conectado como <strong>{user.username}</strong>{" "}
              <span className="text-black/50 dark:text-white/50">({user.role})</span>
            </p>
            <button className={neutralButton} disabled={busy} onClick={logout}>
              Cerrar sesión
            </button>
            <div className="flex items-center gap-3 rounded-md border border-black/10 p-3 dark:border-white/10">
              <span className="text-sm">TTL de la sesión:</span>
              <span className="text-2xl font-semibold tabular-nums">
                {ttlMs === null ? "—" : `${(ttlMs / 1000).toFixed(1)}s`}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            <p className="text-sm text-black/60 dark:text-white/60">
              Inicia sesión con tus credenciales o usa los accesos rápidos (credenciales públicas).
            </p>
            <div className="flex flex-wrap items-end gap-3">
              <label className="flex flex-col gap-1 text-sm">
                Usuario
                <input
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className={inputClass}
                />
              </label>
              <label className="flex flex-col gap-1 text-sm">
                Contraseña
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={inputClass}
                />
              </label>
              <button
                className={greenButton}
                disabled={busy}
                onClick={() => login(username, password)}
              >
                Iniciar sesión
              </button>
            </div>
            <div className="flex flex-wrap gap-3">
              <button
                className={neutralButton}
                disabled={busy}
                onClick={() => {
                  setUsername("user");
                  setPassword("user");
                  void login("user", "user");
                }}
              >
                Entrar como usuario (user/user)
              </button>
              <button
                className={neutralButton}
                disabled={busy}
                onClick={() => {
                  setUsername("admin");
                  setPassword("admin");
                  void login("admin", "admin");
                }}
              >
                Entrar como super usuario (admin/admin)
              </button>
            </div>
          </div>
        )}
        {message && <p className="mt-3 text-sm text-red-600 dark:text-red-400">{message}</p>}
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {counterDefs.map((c) => (
          <div
            key={c.key}
            className="flex flex-col gap-2 rounded-lg border border-black/10 p-4 dark:border-white/10"
          >
            <h2 className="font-medium">{c.title}</h2>
            <p className="text-xs text-black/50 dark:text-white/50">{c.hint}</p>
            <div className="text-3xl font-semibold">{counters[c.key]}</div>
            <button
              className={greenButton}
              disabled={!c.enabled}
              onClick={() => increment(c.key)}
              title={c.enabled ? undefined : "Sin acceso"}
            >
              +1
            </button>
          </div>
        ))}
      </section>

      <footer className="mt-2 rounded-lg border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-black/60 dark:text-white/60">
        ⚠️ El bloqueo del botón es solo <strong>UX</strong>; el servidor <strong>siempre</strong>{" "}
        verifica (401/403) en cada clic. Credenciales de ejemplo: <code>user/user</code> y{" "}
        <code>admin/admin</code>. El <strong>contador regresivo SSE</strong> del TTL llegará en el
        Módulo 6.
      </footer>
    </div>
  );
}

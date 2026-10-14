"use client";

import { useEffect, useRef, useState } from "react";
import { API_BASE, apiFetch } from "@/lib/api";

type LockResponse = { ok: boolean; holder: string | null; counter: number };
type LockState = { holder: string | null; ttlMs: number };

const CLIENTS = [
  { id: "pepe", label: "Pepe" },
  { id: "juan", label: "Juan" },
  { id: "luis", label: "Luis" },
];

const label = (id: string) => CLIENTS.find((c) => c.id === id)?.label ?? id;

const greenButton =
  "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed";
const neutralButton =
  "rounded-md border border-black/20 px-3 py-2 text-sm font-medium transition-colors hover:bg-black/5 disabled:opacity-40 dark:border-white/20 dark:hover:bg-white/10";

function Humanoid({ hasKey }: { hasKey: boolean }) {
  return (
    <svg viewBox="0 0 80 104" className="h-24 w-20" aria-hidden>
      {hasKey && (
        <text x="40" y="16" textAnchor="middle" fontSize="20">
          🔑
        </text>
      )}
      <circle cx="40" cy="36" r="14" className="fill-black/70 dark:fill-white/70" />
      <path d="M16 100 Q16 58 40 58 Q64 58 64 100 Z" className="fill-black/70 dark:fill-white/70" />
    </svg>
  );
}

export default function LockPage() {
  const [holder, setHolder] = useState<string | null>(null);
  const [counter, setCounter] = useState(0);
  const [ttlMs, setTtlMs] = useState(0);
  const [log, setLog] = useState<string[]>([]);
  const [busy, setBusy] = useState(false);
  const prevHolder = useRef<string | null>(null);
  const consoleRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const source = new EventSource(`${API_BASE}/lock/stream`);
    source.onmessage = (event) => {
      const state = JSON.parse(event.data) as LockState;
      setHolder(state.holder);
      setTtlMs(state.ttlMs);
      if (prevHolder.current && !state.holder) {
        addLog("🔓 La llave se liberó por tiempo");
      }
      prevHolder.current = state.holder;
    };
    return () => source.close();
  }, []);

  useEffect(() => {
    const el = consoleRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [log]);

  function addLog(message: string) {
    setLog((prev) => [...prev.slice(-49), message]);
  }

  async function call(path: string, client: string) {
    setBusy(true);
    const res = await apiFetch(`${path}?client=${client}`, { method: "POST" });
    const data = (await res.json()) as LockResponse;
    setCounter(data.counter);
    setHolder(data.holder);
    prevHolder.current = data.holder;
    setBusy(false);
    return data;
  }

  async function acquire(client: string) {
    const data = await call("/lock/acquire", client);
    addLog(data.ok ? `🔑 ${label(client)} tomó la llave` : "⛔ La llave está ocupada");
  }

  async function release(client: string) {
    const data = await call("/lock/release", client);
    if (data.ok) addLog(`🔓 ${label(client)} soltó la llave`);
  }

  async function increment(client: string) {
    const data = await call("/lock/counter/increment", client);
    addLog(
      data.ok
        ? `✅ ${label(client)} accionó el contador con éxito`
        : `❌ ${label(client)} trató de accionar el contador pero no tiene la llave, mala suerte`
    );
  }

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col items-center gap-2 text-center">
        <h1 className="text-2xl font-semibold">Lock distribuido</h1>
        <p className="max-w-3xl text-sm text-black/60 dark:text-white/60">
          Un <strong>lock en Redis</strong> (<code>SET NX PX</code>) da acceso exclusivo a un
          recurso compartido. <strong>Pepe, Juan y Luis</strong> compiten por{" "}
          <strong>una llave</strong>: solo el que la tiene puede accionar el contador. Se libera al
          soltarla o al <strong>caducar el TTL</strong>.
        </p>
      </section>

      <section className="flex flex-col items-center gap-1 rounded-lg border border-black/10 p-6 dark:border-white/10">
        <span className="text-sm text-black/50 dark:text-white/50">Contador</span>
        <span className="text-5xl font-semibold tabular-nums">{counter}</span>
        <span className="text-lg font-medium tabular-nums">
          {holder ? `🔑 ${label(holder)} · ${(ttlMs / 1000).toFixed(1)}s` : "llave libre"}
        </span>
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {CLIENTS.map((c) => {
          const owns = holder === c.id;
          return (
            <div
              key={c.id}
              className={`flex flex-col items-center gap-3 rounded-lg border p-4 transition-colors ${
                owns ? "border-amber-500/60 bg-amber-500/5" : "border-black/10 dark:border-white/10"
              }`}
            >
              <span className="text-center font-medium">{c.label}</span>
              <Humanoid hasKey={owns} />
              <div className="flex w-full flex-col gap-2">
                <button
                  className={neutralButton}
                  disabled={busy || holder !== null}
                  onClick={() => acquire(c.id)}
                >
                  Tomar llave
                </button>
                <button
                  className={neutralButton}
                  disabled={busy || !owns}
                  onClick={() => release(c.id)}
                >
                  Soltar llave
                </button>
                <button className={greenButton} disabled={busy} onClick={() => increment(c.id)}>
                  Accionar contador
                </button>
              </div>
            </div>
          );
        })}
      </section>

      <section className="flex flex-col items-center gap-2 rounded-lg border border-black/10 p-4 dark:border-white/10">
        <div className="flex w-full items-center justify-between">
          <h2 className="font-medium">Consola</h2>
          <button className={neutralButton} onClick={() => setLog([])}>
            Limpiar
          </button>
        </div>
        <div
          ref={consoleRef}
          className="h-40 w-full overflow-y-auto rounded-md bg-black/5 p-3 text-center font-mono text-xs dark:bg-white/5"
        >
          {log.length === 0 ? (
            <p className="text-black/40 dark:text-white/40">Sin acciones todavía…</p>
          ) : (
            log.map((line, i) => <p key={i}>{line}</p>)
          )}
        </div>
      </section>
    </div>
  );
}

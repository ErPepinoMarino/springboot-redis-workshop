"use client";

import { useEffect, useRef, useState } from "react";
import { API_BASE, apiFetch } from "@/lib/api";

const greenButton =
  "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-500 disabled:opacity-40 disabled:cursor-not-allowed";
const neutralButton =
  "rounded-md border border-black/20 px-3 py-2 text-sm font-medium transition-colors hover:bg-black/5 disabled:opacity-40 dark:border-white/20 dark:hover:bg-white/10";

function formatTime(iso: string): string {
  const date = new Date(iso);
  const base = date.toLocaleTimeString("es-ES", { hour12: false });
  return `${base}.${String(date.getMilliseconds()).padStart(3, "0")}`;
}

export default function ClockPage() {
  const [manualTime, setManualTime] = useState<string | null>(null);
  const [liveTime, setLiveTime] = useState<string | null>(null);
  const [connected, setConnected] = useState(false);
  const sourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    return () => sourceRef.current?.close();
  }, []);

  async function refresh() {
    const res = await apiFetch("/clock");
    if (res.ok) {
      const data = (await res.json()) as { time: string };
      setManualTime(data.time);
    }
  }

  function subscribe() {
    if (sourceRef.current) return;
    const source = new EventSource(`${API_BASE}/clock/stream`);
    source.onmessage = (event) => setLiveTime(event.data);
    sourceRef.current = source;
    setConnected(true);
  }

  function unsubscribe() {
    sourceRef.current?.close();
    sourceRef.current = null;
    setConnected(false);
  }

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Pub/Sub + SSE</h1>
        <p className="max-w-3xl text-sm text-black/60 dark:text-white/60">
          La hora del servidor, de dos formas: <strong>preguntando</strong> (panel 1) y{" "}
          <strong>empujada</strong> por el servidor (panel 2). Por debajo: <code>PUBLISH</code>/
          <code>SUBSCRIBE</code> en Redis y eventos <code>text/event-stream</code>.
        </p>
      </section>

      <section className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-3 rounded-lg border border-black/10 p-4 dark:border-white/10">
          <h2 className="font-medium">1. Sin SSE (tú preguntas)</h2>
          <p className="text-4xl font-semibold tabular-nums">
            {manualTime ? formatTime(manualTime) : "--:--:--"}
          </p>
          <button className={neutralButton} onClick={refresh}>
            Actualizar
          </button>
          <p className="text-xs text-black/50 dark:text-white/50">
            Cada clic hace <code>GET /clock</code>. La hora solo cambia cuando{" "}
            <strong>pides</strong>.
          </p>
        </div>

        <div className="flex flex-col gap-3 rounded-lg border border-black/10 p-4 dark:border-white/10">
          <h2 className="font-medium">
            2. Con SSE (el servidor empuja){" "}
            {connected && (
              <span className="text-xs font-normal text-emerald-600 dark:text-emerald-400">
                ● conectado
              </span>
            )}
          </h2>
          <p className="text-4xl font-semibold tabular-nums">
            {liveTime ? formatTime(liveTime) : "--:--:--"}
          </p>
          <div className="flex gap-3">
            <button className={greenButton} disabled={connected} onClick={subscribe}>
              Suscribirse
            </button>
            <button className={neutralButton} disabled={!connected} onClick={unsubscribe}>
              Desuscribirse
            </button>
          </div>
          <p className="text-xs text-black/50 dark:text-white/50">
            <code>EventSource</code> sobre <code>/clock/stream</code>: la hora llega cada 0.5s{" "}
            <strong>sin pedirla</strong>. Al desuscribirte, se cierra la conexión.
          </p>
        </div>
      </section>

      <footer className="mt-2 rounded-lg border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-black/60 dark:text-white/60">
        <strong>Cómo funciona:</strong> un <code>@Scheduled</code> publica la hora en el canal{" "}
        <code>clock</code> de Redis cada 0.5s. El backend está suscrito y, por cada mensaje, lo{" "}
        <em>empuja</em> al navegador con <strong>SSE</strong> (eventos unidireccionales
        servidor→cliente). Mientras no estés suscrito, los tics <strong>se pierden</strong> (
        <em>fire-and-forget</em>).
      </footer>
    </div>
  );
}

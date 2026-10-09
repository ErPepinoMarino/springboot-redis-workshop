"use client";

import { useRef, useState } from "react";
import { apiFetch } from "@/lib/api";

type Point = { seconds: number; remaining: number };
type Simulation = {
  requests: number;
  capacity: number;
  rate: number;
  queue: number;
  passed: number;
  queued: number;
  dropped: number;
  drainSeconds: number;
  timeline: Point[];
};
type RealResult = {
  ok: number;
  tooMany: number;
  remaining: string | null;
  retryAfter: string | null;
};
type Phase = "idle" | "loading" | "playing" | "done";

const scenarios = [
  { label: "Tu escenario", requests: 1000, capacity: 500, rate: 50, queue: 300 },
  { label: "Sin cola", requests: 1000, capacity: 20, rate: 20, queue: 0 },
  { label: "Cubo grande", requests: 1000, capacity: 100, rate: 20, queue: 180 },
];

const PLAY_MS = 4000;
const MIN_LOADING_MS = 1000;

const greenButton =
  "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-500 disabled:opacity-40";
const presetBase = "rounded-md px-3 py-2 text-sm font-medium transition-colors disabled:opacity-40";
const presetIdle =
  "bg-black/5 text-black/70 hover:bg-black/10 dark:bg-white/10 dark:text-white/70 dark:hover:bg-white/15";
const presetSelected = "bg-amber-400 text-black";
const inputClass =
  "w-28 rounded-md border border-black/20 px-3 py-2 dark:border-white/20 dark:bg-transparent";
const hintClass = "text-xs text-black/40 dark:text-white/40";

function ServiceRateChart({
  limit,
  capacity,
  rate,
  drainSeconds,
  progress,
}: {
  limit: number;
  capacity: number;
  rate: number;
  drainSeconds: number;
  progress: number;
}) {
  const width = 680;
  const height = 240;
  const pad = 40;
  const seconds = Math.max(1, Math.ceil(drainSeconds));
  const maxY = Math.max(limit, capacity, rate, 1);
  const groupW = (width - 2 * pad) / (seconds + 1);
  const barW = groupW * 0.35;

  const [hover, setHover] = useState<{ value: number; kind: "simple" | "bucket" } | null>(null);

  const sy = (v: number) => height - pad - (v / maxY) * (height - 2 * pad);
  const revealed = progress * (seconds + 1);

  const bars = [];
  for (let i = 0; i <= seconds; i++) {
    if (i > revealed) break;
    const x = pad + i * groupW;
    const bucket = i === 0 ? capacity : i <= drainSeconds ? rate : 0;
    const noBucket = i === 0 ? limit : 0;
    bars.push(
      <g key={i}>
        <rect
          x={x}
          y={sy(noBucket)}
          width={barW}
          height={sy(0) - sy(noBucket)}
          className="cursor-pointer fill-red-500/70 hover:fill-red-500"
          onMouseEnter={() => setHover({ value: noBucket, kind: "simple" })}
          onMouseLeave={() => setHover(null)}
        />
        <rect
          x={x + barW + 2}
          y={sy(bucket)}
          width={barW}
          height={sy(0) - sy(bucket)}
          className="cursor-pointer fill-emerald-500/80 hover:fill-emerald-500"
          onMouseEnter={() => setHover({ value: bucket, kind: "bucket" })}
          onMouseLeave={() => setHover(null)}
        />
        <text
          x={x + barW}
          y={height - pad + 14}
          fill="currentColor"
          className="fill-black/50 text-[10px] dark:fill-white/50"
        >
          {i}s
        </text>
      </g>
    );
  }

  const hoverColor =
    hover?.kind === "simple"
      ? "text-red-600 dark:text-red-400"
      : hover?.kind === "bucket"
        ? "text-emerald-600 dark:text-emerald-400"
        : "";

  return (
    <div className="flex flex-col gap-2">
      <svg viewBox={`0 0 ${width} ${height}`} className="w-full text-black/60 dark:text-white/60">
        <line
          x1={pad}
          y1={sy(0)}
          x2={width - pad}
          y2={sy(0)}
          className="stroke-black/20 dark:stroke-white/20"
        />
        <line
          x1={pad}
          y1={pad}
          x2={pad}
          y2={sy(0)}
          className="stroke-black/20 dark:stroke-white/20"
        />
        {bars}
        <text
          x={pad}
          y={pad - 12}
          fill="currentColor"
          className={`text-[13px] font-medium ${hoverColor}`}
        >
          {hover ? `${hover.value} req/s` : "—"}
        </text>
        <text
          x={width - pad}
          y={pad - 12}
          textAnchor="end"
          fill="currentColor"
          className="text-[11px]"
        >
          {maxY}/s
        </text>
        <text
          x={width / 2}
          y={height - 4}
          textAnchor="middle"
          fill="currentColor"
          className="text-[12px]"
        >
          TIEMPO
        </text>
        <text
          x={pad - 26}
          y={height / 2}
          textAnchor="middle"
          fill="currentColor"
          transform={`rotate(-90 ${pad - 26} ${height / 2})`}
          className="text-[12px]"
        >
          Num. Requests
        </text>
      </svg>
      <div className="flex gap-4 text-xs">
        <span className="flex items-center gap-1">
          <span className="inline-block h-3 w-3 bg-red-500/70" /> Sin bucket
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block h-3 w-3 bg-emerald-500/80" /> Con bucket
        </span>
      </div>
    </div>
  );
}

export default function RateLimitPage() {
  const [requests, setRequests] = useState(1000);
  const [capacity, setCapacity] = useState(20);
  const [rate, setRate] = useState(20);
  const [queue, setQueue] = useState(180);
  const limit = capacity + queue;
  const [selected, setSelected] = useState<string | null>(null);
  const [sim, setSim] = useState<Simulation | null>(null);
  const [phase, setPhase] = useState<Phase>("idle");
  const [progress, setProgress] = useState(0);

  const [realRunning, setRealRunning] = useState(false);
  const [real, setReal] = useState<RealResult | null>(null);

  const cancelRef = useRef(false);
  const rafRef = useRef<number | null>(null);

  const busy = phase === "loading" || phase === "playing";

  async function simulate() {
    cancelRef.current = false;
    setPhase("loading");
    setSim(null);
    setProgress(0);

    const started = performance.now();
    const res = await apiFetch(
      `/ratelimit/simulate?requests=${requests}&capacity=${capacity}&rate=${rate}&queue=${queue}`
    );
    const data = (await res.json()) as Simulation;

    const elapsed = performance.now() - started;
    if (elapsed < MIN_LOADING_MS) {
      await new Promise((r) => setTimeout(r, MIN_LOADING_MS - elapsed));
    }
    if (cancelRef.current) {
      setPhase("idle");
      return;
    }

    setSim(data);
    setPhase("playing");

    const playStart = performance.now();
    const frame = (now: number) => {
      if (cancelRef.current) {
        setPhase("done");
        return;
      }
      const p = Math.min(1, (now - playStart) / PLAY_MS);
      setProgress(p);
      if (p < 1) {
        rafRef.current = requestAnimationFrame(frame);
      } else {
        setPhase("done");
      }
    };
    rafRef.current = requestAnimationFrame(frame);
  }

  function cancel() {
    cancelRef.current = true;
    if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
  }

  function selectScenario(s: (typeof scenarios)[number]) {
    setRequests(s.requests);
    setCapacity(s.capacity);
    setRate(s.rate);
    setQueue(s.queue);
    setSelected(s.label);
  }

  async function testReal() {
    setRealRunning(true);
    setReal(null);
    let ok = 0;
    let tooMany = 0;
    let remaining: string | null = null;
    let retryAfter: string | null = null;
    for (let i = 0; i < 25; i++) {
      const res = await apiFetch("/ratelimit/ping");
      if (res.status === 200) ok++;
      if (res.status === 429) tooMany++;
      remaining = res.headers.get("X-RateLimit-Remaining");
      retryAfter = res.headers.get("Retry-After");
    }
    setReal({ ok, tooMany, remaining, retryAfter });
    setRealRunning(false);
  }
  const simLimit = sim ? sim.capacity + sim.queue : 0;
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Rate limiting</h1>
        <p className="max-w-3xl text-sm text-black/60 dark:text-white/60">
          Un <strong>token bucket</strong>: caben <strong>C</strong> fichas (ráfaga) y se generan{" "}
          <strong>R</strong> por segundo. Un aluvión se reparte en <strong>pasan ya</strong>,{" "}
          <strong>en cola</strong> (esperan ficha) y <strong>descartadas</strong>. Simula y observa
          el goteo.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <p className="text-sm text-black/60 dark:text-white/60">
          Elige un test por defecto o ajusta los valores.
        </p>
        <div className="flex flex-wrap items-start gap-3">
          {(
            [
              ["Peticiones", requests, setRequests, "1", "1000000"],
              ["Capacidad", capacity, setCapacity, "0", "100000"],
              ["Ritmo/s", rate, setRate, "0", "100000"],
              ["Cola", queue, setQueue, "0", "1000000"],
            ] as const
          ).map(([label, value, setter, min, max]) => (
            <label key={label} className="flex flex-col gap-1 text-sm">
              {label}
              <input
                type="number"
                value={value}
                min={min}
                max={max}
                disabled={busy}
                onChange={(e) => {
                  setter(Number(e.target.value));
                  setSelected(null);
                }}
                className={inputClass}
              />
              <span className={hintClass}>
                (min. {min}, max. {max})
              </span>
            </label>
          ))}
          <p className={hintClass}>
            Para que la comparación sea justa, el <strong>Rate-Limit</strong> de la versión sin
            bucket se calcula sumando la capacidad del bucket y la de la cola de espera ({capacity}{" "}
            + {queue} = <strong>{limit}</strong>).
          </p>
          <div className="flex flex-wrap items-center gap-3 pt-6">
            {scenarios.map((s) => (
              <button
                key={s.label}
                className={`${presetBase} ${selected === s.label ? presetSelected : presetIdle}`}
                disabled={busy}
                onClick={() => selectScenario(s)}
              >
                {s.label}
              </button>
            ))}
            <button className={greenButton} disabled={busy} onClick={simulate}>
              Simular
            </button>
            {busy && (
              <button
                className="rounded-md border border-red-500/40 px-3 py-2 text-sm font-medium text-red-600 transition-colors hover:bg-red-500/10 dark:text-red-400"
                onClick={cancel}
              >
                Cancelar
              </button>
            )}
          </div>
        </div>
      </section>

      {phase === "loading" && (
        <p className="text-sm text-black/60 dark:text-white/60">
          Lanzando test con {requests.toLocaleString()} peticiones, capacidad {capacity}, ritmo{" "}
          {rate}/s y cola {queue}…
        </p>
      )}

      {sim && (
        <section className="flex flex-col gap-4">
          <div className="grid gap-4 sm:grid-cols-2">
            {[
              {
                title: "Sin bucket",
                color: "text-red-600 dark:text-red-400",
                rows: [
                  ["Pasan ya", Math.min(sim.requests, simLimit)],
                  ["En cola", 0],
                  ["Descartadas", Math.max(0, sim.requests - Math.min(sim.requests, simLimit))],
                  ["Se vacía en", "—"],
                ],
              },
              {
                title: "Con bucket",
                color: "text-emerald-600 dark:text-emerald-400",
                rows: [
                  ["Pasan ya", sim.passed],
                  ["En cola", sim.queued],
                  ["Descartadas", sim.dropped],
                  ["Se vacía en", `${sim.drainSeconds.toFixed(1)} s`],
                ],
              },
            ].map((block) => (
              <div
                key={block.title}
                className="rounded-lg border border-black/10 p-4 dark:border-white/10"
              >
                <h3 className={`mb-2 font-semibold ${block.color}`}>{block.title}</h3>
                <table className="w-full border-collapse text-sm">
                  <tbody>
                    {block.rows.map(([k, v]) => (
                      <tr key={String(k)} className="border-b border-black/5 dark:border-white/5">
                        <td className="py-1 pr-4 text-black/60 dark:text-white/60">{k}</td>
                        <td>{v}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))}
          </div>
          <div className="rounded-lg border border-black/10 p-4 dark:border-white/10">
            <h2 className="mb-2 font-medium">Evolución de la cola</h2>
            <ServiceRateChart
              limit={simLimit}
              capacity={sim.capacity}
              rate={sim.rate}
              drainSeconds={sim.drainSeconds}
              progress={progress}
            />
          </div>
        </section>
      )}

      <section className="flex flex-col gap-3 rounded-lg border border-black/10 p-4 dark:border-white/10">
        <h2 className="font-medium">Endpoint real</h2>
        <p className="text-sm text-black/60 dark:text-white/60">
          Lanza 25 peticiones a <code>/ratelimit/ping</code>. El límite lo pone el servidor
          (capacidad 20, ritmo 10/s) y es por IP. Verás cuántas pasan y cuántas reciben{" "}
          <strong>429</strong> con <code>Retry-After</code>.
        </p>
        <div>
          <button className={greenButton} disabled={realRunning} onClick={testReal}>
            Probar el limitador real
          </button>
        </div>
        {real && (
          <table className="w-full max-w-md border-collapse text-sm">
            <tbody>
              {[
                ["200 OK", real.ok],
                ["429 Too Many", real.tooMany],
                ["X-RateLimit-Remaining", real.remaining ?? "—"],
                ["Retry-After", real.retryAfter ?? "—"],
              ].map(([label, value]) => (
                <tr key={label} className="border-b border-black/5 dark:border-white/5">
                  <td className="py-1 pr-4 text-black/60 dark:text-white/60">{label}</td>
                  <td>{value}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <footer className="mt-2 rounded-lg border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-black/60 dark:text-white/60">
        ⚠️ <strong>Modelo didáctico, no observación real.</strong> Esta simulación es una{" "}
        <strong>predicción</strong> (matemáticas), y la animación es una reproducción del cálculo,
        no tiempo real. El endpoint real <strong>rechaza con 429 y no encola</strong>, así que aquí
        no hay una cola real que medir. La visualización <strong>real</strong> (estado observado en
        vivo, vía <strong>SSE</strong>) se implementará en el <strong>Módulo 6</strong>. Además,
        todas las peticiones de la demo comparten IP (la del proxy de Next).
      </footer>
    </div>
  );
}

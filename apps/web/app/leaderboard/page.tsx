"use client";

import { useState } from "react";
import { apiFetch } from "@/lib/api";

type Entry = { member: string; score: number };
type Result = {
  players: number;
  concurrency: number;
  durationSeconds: number;
  redisSuccesses: number;
  postgresSuccesses: number;
  redisTop: Entry[];
  postgresTop: Entry[];
};

const PLAYERS = 1000;
const DURATION = 3;

const scenarios = [
  { label: "Poca carga", concurrency: 1 },
  { label: "Carga media", concurrency: 16 },
  { label: "Carga alta", concurrency: 100 },
];

const greenButton =
  "rounded-md bg-emerald-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-500 disabled:opacity-40";
const presetBase = "rounded-md px-3 py-2 text-sm font-medium transition-colors disabled:opacity-40";
const presetIdle =
  "bg-black/5 text-black/70 hover:bg-black/10 dark:bg-white/10 dark:text-white/70 dark:hover:bg-white/15";
const presetSelected = "bg-amber-400 text-black";
const inputClass =
  "w-32 rounded-md border border-black/20 px-3 py-2 dark:border-white/20 dark:bg-transparent";
const hintClass = "text-xs text-black/40 dark:text-white/40";

export default function LeaderboardPage() {
  const [concurrency, setConcurrency] = useState(10);
  const [selected, setSelected] = useState<string | null>(null);
  const [result, setResult] = useState<Result | null>(null);
  const [running, setRunning] = useState(false);

  async function run() {
    setRunning(true);
    setResult(null);
    const res = await apiFetch(
      `/leaderboard/benchmark?players=${PLAYERS}&concurrency=${concurrency}&duration=${DURATION}`,
      { method: "POST" }
    );
    setResult((await res.json()) as Result);
    setRunning(false);
  }

  function selectScenario(s: (typeof scenarios)[number]) {
    setConcurrency(s.concurrency);
    setSelected(s.label);
  }

  const opsPerSecond = (successes: number) =>
    result ? Math.round(successes / result.durationSeconds) : 0;
  const speedup =
    result && result.postgresSuccesses > 0
      ? result.redisSuccesses / result.postgresSuccesses
      : null;

  const sides = result
    ? [
        {
          title: "Redis",
          accent: "text-emerald-600 dark:text-emerald-400",
          successes: result.redisSuccesses,
          top: result.redisTop,
        },
        {
          title: "Postgres",
          accent: "text-sky-600 dark:text-sky-400",
          successes: result.postgresSuccesses,
          top: result.postgresTop,
        },
      ]
    : [];

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-2">
        <h1 className="text-2xl font-semibold">Leaderboard</h1>
        <p className="max-w-3xl text-sm text-black/60 dark:text-white/60">
          <strong>Test de carga.</strong> Lanzamos <strong>{concurrency}</strong> peticiones
          concurrentes que puntúan sin parar durante <strong>{DURATION} segundos</strong> sobre un
          ranking de <strong>{PLAYERS.toLocaleString()} jugadores</strong>. La{" "}
          <strong>misma carga</strong> se aplica a <strong>Redis</strong> (sorted set,{" "}
          <code>ZINCRBY</code>) y a <strong>Postgres</strong> (<code>UPDATE</code> en transacción,
          con índice). Medimos <strong>cuántas operaciones COMPLETA cada uno</strong> en esos{" "}
          {DURATION} s, no cuánto tarda una. Postgres se techa por su{" "}
          <strong>pool de conexiones</strong> (10) y el coste por transacción; Redis suma en memoria
          y multiplexa.
        </p>
      </section>

      <section className="flex flex-col gap-3">
        <p className="text-sm text-black/60 dark:text-white/60">
          Elige un escenario por defecto o ajusta la concurrencia.
        </p>
        <div className="flex flex-wrap items-start gap-3">
          <label className="flex flex-col gap-1 text-sm">
            Concurrencia (C)
            <input
              type="number"
              min={1}
              max={200}
              value={concurrency}
              disabled={running}
              onChange={(e) => {
                setConcurrency(Number(e.target.value));
                setSelected(null);
              }}
              className={inputClass}
            />
            <span className={hintClass}>(min. 1, max. 200)</span>
            <span className={hintClass}>
              Peticiones simultáneas. Más → más presión sobre el pool (10) de Postgres.
            </span>
          </label>

          <div className="flex flex-wrap items-center gap-3 pt-6">
            {scenarios.map((s) => (
              <button
                key={s.label}
                className={`${presetBase} ${selected === s.label ? presetSelected : presetIdle}`}
                disabled={running}
                onClick={() => selectScenario(s)}
              >
                {s.label}
              </button>
            ))}
            <button className={greenButton} disabled={running} onClick={run}>
              Ejecutar test
            </button>
          </div>
        </div>
      </section>

      {running && (
        <p className="text-sm text-black/60 dark:text-white/60">
          Lanzando {concurrency} peticiones concurrentes durante {DURATION} s en ambos bandos…
        </p>
      )}

      {result && (
        <section className="grid gap-4 sm:grid-cols-2">
          {sides.map((side) => (
            <div
              key={side.title}
              className="rounded-lg border border-black/10 p-4 dark:border-white/10"
            >
              <h2 className={`mb-2 font-semibold ${side.accent}`}>{side.title}</h2>
              <div className="mb-3 text-sm">
                Éxitos: <strong>{side.successes.toLocaleString()}</strong>{" "}
                <span className="text-black/50 dark:text-white/50">
                  (~{opsPerSecond(side.successes).toLocaleString()} ops/s)
                </span>
              </div>
              <div className="max-h-80 overflow-auto">
                <table className="w-full border-collapse text-sm">
                  <thead>
                    <tr className="border-b border-black/10 text-left dark:border-white/10">
                      <th className="py-1 pr-3 font-medium">#</th>
                      <th className="py-1 pr-3 font-medium">Jugador</th>
                      <th className="py-1 font-medium">Puntos</th>
                    </tr>
                  </thead>
                  <tbody>
                    {side.top.map((entry, index) => (
                      <tr
                        key={entry.member}
                        className="border-b border-black/5 dark:border-white/5"
                      >
                        <td className="py-1 pr-3 text-black/40 dark:text-white/40">{index + 1}</td>
                        <td className="py-1 pr-3">{entry.member}</td>
                        <td className="py-1">{entry.score}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </section>
      )}

      {speedup !== null && result && (
        <section className="rounded-lg border border-emerald-600/30 bg-emerald-600/5 p-4 text-sm">
          <p>
            Con C={result.concurrency}, Redis completó{" "}
            <span className="font-bold text-emerald-600 dark:text-emerald-400">
              {speedup.toFixed(1)}×
            </span>{" "}
            más operaciones que Postgres en los mismos {result.durationSeconds} s (
            {result.redisSuccesses.toLocaleString()} frente a{" "}
            {result.postgresSuccesses.toLocaleString()}).
          </p>
        </section>
      )}

      <footer className="mt-2 rounded-lg border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-black/60 dark:text-white/60">
        ⚠️ <strong>Honesto y medido en backend.</strong> Cada evento es una transacción/petición, en
        ambos bandos por igual. Lo que medimos es <strong>throughput</strong> (operaciones
        completadas en {DURATION} s), no latencia de una sola. Postgres no falla, simplemente{" "}
        <strong>completa muchas menos</strong> por su pool (10) y el coste por transacción. El
        número de jugadores no se expone porque, con reparto uniforme,{" "}
        <strong>no mueve la aguja</strong>: el techo lo pone el pool, no las filas.
      </footer>
    </div>
  );
}

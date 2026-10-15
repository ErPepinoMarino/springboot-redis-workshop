"use client";

import { useRef, useState } from "react";
import { apiFetch } from "@/lib/api";

type BenchmarkResult = {
  mode: string;
  iterations: number;
  concurrency: number;
  rounds: number;
  count: number;
  totalMs: number;
  avgMs: number;
  minMs: number;
  maxMs: number;
  p95Ms: number;
  throughput: number;
};

const scenarios = [
  { label: "Ligero", iterations: 1000, concurrency: 8 },
  { label: "Medio", iterations: 2000, concurrency: 20 },
  { label: "Fuerte", iterations: 3000, concurrency: 30 },
];

const greenButton = "btn btn-primary";
const presetBase = "btn";
const presetIdle = "btn-ghost";
const presetSelected = "border-cyan bg-cyan/10 text-cyan";
const inputClass = "input";
const hintClass = "text-xs text-dim";

export default function CachePage() {
  const [iterations, setIterations] = useState(500);
  const [concurrency, setConcurrency] = useState(16);
  const [selectedScenario, setSelectedScenario] = useState<string | null>(null);
  const [running, setRunning] = useState(false);
  const [noCache, setNoCache] = useState<BenchmarkResult | null>(null);
  const [withCache, setWithCache] = useState<BenchmarkResult | null>(null);
  const abort = useRef<AbortController | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function bench(cached: boolean): Promise<BenchmarkResult> {
    const controller = new AbortController();
    abort.current = controller;
    const res = await apiFetch(
      `/cache/products/bench?iterations=${iterations}&cached=${cached}&concurrency=${concurrency}`,
      { signal: controller.signal }
    );
    if (!res.ok) throw new Error(`bench(cached=${cached}) → ${res.status}`);
    return (await res.json()) as BenchmarkResult;
  }

  async function clearCache() {
    await apiFetch("/cache/products", { method: "DELETE" });
  }

  async function run() {
    if (running) return;
    setRunning(true);
    setError(null);
    setNoCache(null);
    setWithCache(null);

    const startedAt = performance.now();
    try {
      const noCacheResult = await bench(false);
      await clearCache();
      const withCacheResult = await bench(true);
      await clearCache();

      const elapsed = performance.now() - startedAt;
      if (elapsed < 1000) {
        await new Promise((resolve) => setTimeout(resolve, 1000 - elapsed));
      }

      setNoCache(noCacheResult);
      setWithCache(withCacheResult);
    } catch (e) {
      const aborted = e instanceof DOMException && e.name === "AbortError";
      if (!aborted) {
        setError("Error al contactar con el servidor. Inténtalo de nuevo.");
      }
    } finally {
      setRunning(false);
    }
  }

  function selectScenario(s: (typeof scenarios)[number]) {
    setIterations(s.iterations);
    setConcurrency(s.concurrency);
    setSelectedScenario(s.label);
  }

  const speedup =
    noCache && withCache && noCache.throughput > 0
      ? withCache.throughput / noCache.throughput
      : null;

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col items-center gap-2 text-center">
        <h1 className="font-display text-3xl tracking-wide text-cyan">Caché</h1>
        <p className="max-w-3xl text-sm text-dim">
          Partimos de una base de datos con <strong>20.000 productos</strong>. El test lanza N{" "}
          <strong>búsquedas recurrentes</strong> por nombre (<code>{"LIKE '%...%'"}</code>, que{" "}
          <strong>escanea la tabla</strong>), <strong>C a la vez</strong>, primero{" "}
          <strong>sin caché</strong> y luego <strong>con caché</strong>. Como la búsqueda es{" "}
          <strong>costosa y se repite</strong>, la caché marca la diferencia.
        </p>
      </section>
      {error && (
        <p className="rounded-sm border border-red-500/40 bg-red-500/10 p-3 text-center text-sm text-red-400">
          ⚠️ {error}
        </p>
      )}
      <section className="flex flex-col gap-3">
        <p className="text-center text-sm text-dim">
          Ejecuta uno de los tests por defecto o pon tus propios valores.
        </p>
        <p className="text-center text-xs text-dim">
          ⚠️ Valores muy altos = muchas consultas costosas = <strong>timeout</strong>. Si el test
          falla, baja las peticiones o la concurrencia.
        </p>

        <div className="flex flex-wrap items-start justify-center gap-3">
          <label className="flex flex-col gap-1 text-sm">
            Peticiones (N)
            <input
              type="number"
              min={1}
              max={20000}
              value={iterations}
              disabled={running}
              onChange={(e) => {
                setIterations(Number(e.target.value));
                setSelectedScenario(null);
              }}
              className={inputClass}
            />
            <span className={hintClass}>(min. 1, max. 20000)</span>
          </label>

          <label className="flex flex-col gap-1 text-sm">
            Concurrencia (C)
            <input
              type="number"
              min={1}
              max={100}
              value={concurrency}
              disabled={running}
              onChange={(e) => {
                setConcurrency(Number(e.target.value));
                setSelectedScenario(null);
              }}
              className={inputClass}
            />
            <span className={hintClass}>(min. 1, max. 100)</span>
          </label>

          <div className="flex flex-wrap items-center justify-center gap-3 pt-6">
            {scenarios.map((s) => (
              <button
                key={s.label}
                className={`${presetBase} ${selectedScenario === s.label ? presetSelected : presetIdle}`}
                disabled={running}
                onClick={() => selectScenario(s)}
              >
                {s.label}
              </button>
            ))}

            <button className={greenButton} disabled={running} onClick={run}>
              Ejecutar
            </button>

            {running && (
              <button
                className="rounded-sm border border-red-500/40 px-3 py-2 text-sm font-medium text-red-600 transition-colors hover:bg-red-500/10 dark:text-red-400"
                onClick={() => abort.current?.abort()}
              >
                Cancelar
              </button>
            )}
          </div>
        </div>
      </section>

      {running && (
        <p className="text-sm text-dim">
          Ejecutando benchmark con <strong>{iterations.toLocaleString()}</strong> peticiones y una
          concurrencia de <strong>{concurrency}</strong>…
        </p>
      )}

      {(noCache || withCache) && (
        <section className="flex flex-col gap-3">
          <h2 className="text-center text-sm font-medium">
            TEST: con <strong>{(noCache ?? withCache)?.iterations.toLocaleString()}</strong>{" "}
            peticiones y una concurrencia de <strong>{(noCache ?? withCache)?.concurrency}</strong>{" "}
            — mediana de <strong>{(noCache ?? withCache)?.rounds}</strong> rondas:
          </h2>

          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-sm">
              <thead>
                <tr className="border-b border-line text-left">
                  <th className="py-2 pr-4 font-medium">Métrica</th>
                  <th className="py-2 pr-4 font-medium">Sin caché</th>
                  <th className="py-2 pr-4 font-medium">Con caché</th>
                </tr>
              </thead>
              <tbody>
                {(
                  [
                    ["Total (ms)", (r: BenchmarkResult) => r.totalMs.toFixed(0)],
                    ["Media (ms)", (r: BenchmarkResult) => r.avgMs.toFixed(2)],
                    ["p95 (ms)", (r: BenchmarkResult) => r.p95Ms.toFixed(2)],
                    ["Máx (ms)", (r: BenchmarkResult) => r.maxMs.toFixed(2)],
                    ["Throughput (req/s)", (r: BenchmarkResult) => r.throughput.toFixed(0)],
                    ["Completadas", (r: BenchmarkResult) => `${r.count}/${r.iterations}`],
                  ] as const
                ).map(([label, fn]) => (
                  <tr key={label} className="border-b border-line/40">
                    <td className="py-2 pr-4 text-dim">{label}</td>
                    <td className="py-2 pr-4">{noCache ? fn(noCache) : "—"}</td>
                    <td className="py-2 pr-4">{withCache ? fn(withCache) : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {speedup !== null && noCache && withCache && (
        <section
          className={`rounded-sm border p-4 text-sm ${
            speedup >= 1
              ? "border-emerald-600/30 bg-emerald-600/5"
              : "border-amber-500/30 bg-amber-500/5"
          }`}
        >
          <p>
            ✅ ¡Prueba finalizada!{" "}
            {speedup >= 1 ? (
              <>
                Con caché el throughput fue{" "}
                <span className="font-bold text-emerald-600 dark:text-emerald-400">
                  {speedup.toFixed(1)}×
                </span>{" "}
                mayor ({withCache.throughput.toFixed(0)} frente a {noCache.throughput.toFixed(0)}{" "}
                req/s).
              </>
            ) : (
              <>
                Redis tuvo un rendimiento de{" "}
                <span className="font-bold text-amber-600 dark:text-amber-400">
                  {speedup.toFixed(1)}×
                </span>{" "}
                respecto a la base de datos ({withCache.throughput.toFixed(0)} frente a{" "}
                {noCache.throughput.toFixed(0)} req/s).
              </>
            )}
          </p>
        </section>
      )}
      <footer className="mt-2 rounded-sm border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-dim">
        ⚠️ <strong>Metodología.</strong> Se descarta un <strong>warmup</strong> (para que la JVM
        compile en caliente) y se reporta la <strong>mediana de 3 rondas</strong>. Medimos una{" "}
        <strong>búsqueda por subcadena</strong> (<code>{"LIKE '%...%'"}</code>) sobre 20.000 filas:
        al no poder usar índice, Postgres <strong>escanea</strong> la tabla. Es una consulta{" "}
        <strong>real y recurrente</strong> (un catálogo que se busca muchas veces) — justo lo que
        una caché debe acelerar. Aun así, tómalo como ilustrativo, no como un benchmark riguroso.
      </footer>
    </div>
  );
}

"use client";

import { useEffect, useRef, useState } from "react";
import { apiFetch } from "@/lib/api";

type JobType = "QUICK" | "SLOW" | "FAILING" | "CRASH";
type JobStatus = "PENDING" | "PROCESSING" | "DONE" | "FAILED" | "ORPHANED";

type Job = {
  id: string;
  type: JobType;
  status: JobStatus;
  workerId: string | null;
  durationMs: number;
  error: string | null;
  attempts: number;
};

type QueueItem = { localId: number; type: JobType };
type LaunchedJob = { localId: number; id: string; job: Job };

const MAX_TASKS = 10;

const TYPES: { value: JobType; label: string }[] = [
  { value: "QUICK", label: "Rápida (2s)" },
  { value: "SLOW", label: "Lenta (5s)" },
  { value: "FAILING", label: "Fallida (3s)" },
  { value: "CRASH", label: "Caída sin ack (3s)" },
];

const statusColor: Record<JobStatus, string> = {
  PENDING: "text-dim",
  PROCESSING: "text-amber-600 dark:text-amber-400",
  ORPHANED: "text-purple-600 dark:text-purple-400",
  DONE: "text-emerald-600 dark:text-emerald-400",
  FAILED: "text-red-600 dark:text-red-400",
};

const greenButton = "btn btn-primary";
const neutralButton = "btn btn-ghost";
const inputClass = "input";

const typeLabel = (type: JobType) => TYPES.find((t) => t.value === type)?.label ?? type;

function describe(job: Job): string {
  switch (job.status) {
    case "PENDING":
      return "en cola";
    case "PROCESSING":
      return job.attempts > 1
        ? `reintentando (worker ${job.workerId})`
        : `en proceso (worker ${job.workerId})`;
    case "ORPHANED":
      return `huérfana · esperando rescate (${job.workerId})`;
    case "DONE":
      return `terminada en ${(job.durationMs / 1000).toFixed(1)}s por ${job.workerId}${
        job.attempts > 1 ? " (tras rescate)" : ""
      }`;
    case "FAILED":
      return `fallida por ${job.workerId}: ${job.error ?? "error"}`;
  }
}

export default function JobsPage() {
  const [selected, setSelected] = useState<JobType>("QUICK");
  const [queue, setQueue] = useState<QueueItem[]>([]);
  const [jobs, setJobs] = useState<LaunchedJob[]>([]);
  const [busy, setBusy] = useState(false);
  const nextId = useRef(1);

  const jobsRef = useRef(jobs);
  useEffect(() => {
    jobsRef.current = jobs;
  }, [jobs]);

  useEffect(() => {
    const timer = setInterval(async () => {
      const active = jobsRef.current.filter(
        (j) => j.job.status !== "DONE" && j.job.status !== "FAILED"
      );
      if (active.length === 0) return;

      const results = await Promise.all(
        active.map(async (j) => {
          const res = await apiFetch(`/jobs/${j.id}`);
          if (!res.ok) return null;
          return { localId: j.localId, job: (await res.json()) as Job };
        })
      );

      setJobs((prev) =>
        prev.map((j) => {
          const update = results.find((r) => r?.localId === j.localId);
          return update ? { ...j, job: update.job } : j;
        })
      );
    }, 500);
    return () => clearInterval(timer);
  }, []);

  function addToQueue() {
    if (queue.length >= MAX_TASKS) return;
    setQueue((q) => [...q, { localId: nextId.current++, type: selected }]);
  }

  function removeFromQueue(localId: number) {
    setQueue((q) => q.filter((i) => i.localId !== localId));
  }

  async function launch() {
    setBusy(true);
    const launched: LaunchedJob[] = [];
    for (const item of queue) {
      const res = await apiFetch("/jobs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ type: item.type }),
      });
      const job = (await res.json()) as Job;
      launched.push({ localId: item.localId, id: job.id, job });
    }
    setJobs(launched);
    setQueue([]);
    setBusy(false);
  }

  function reset() {
    setJobs([]);
    setQueue([]);
  }

  const tracking = jobs.some((j) => j.job.status !== "DONE" && j.job.status !== "FAILED");

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col items-center gap-2 text-center">
        <h1 className="font-display text-3xl tracking-wide text-cyan">Cola de trabajos</h1>
        <p className="max-w-3xl text-sm text-dim">
          Encola tareas y deja que <strong>3 workers</strong> las procesen en paralelo desde un{" "}
          <strong>stream</strong> de Redis (<code>XREADGROUP</code> + <code>XACK</code>). Cada tarea
          se procesa en un <strong>handler</strong>. La tarea «caída» no confirma (<em>sin ack</em>)
          y el <strong>reclaimer</strong> la rescata. El estado se sigue por{" "}
          <strong>polling</strong>.
        </p>
      </section>

      <section className="flex flex-col gap-3 rounded-sm border border-line p-4 border-line">
        <h2 className="font-medium">1. Elige una tarea y añádela a la lista</h2>
        <div className="flex flex-wrap items-end justify-center gap-3">
          <label className="flex flex-col gap-1 text-sm">
            Tipo de tarea
            <select
              value={selected}
              onChange={(e) => setSelected(e.target.value as JobType)}
              className={inputClass}
            >
              {TYPES.map((t) => (
                <option
                  key={t.value}
                  value={t.value}
                  className="bg-white text-black dark:bg-neutral-900 dark:text-white"
                >
                  {t.label}
                </option>
              ))}
            </select>
          </label>
          <button
            className={neutralButton}
            disabled={queue.length >= MAX_TASKS}
            onClick={addToQueue}
          >
            Añadir a la lista
          </button>
          <span className="text-sm text-dim">
            {queue.length}/{MAX_TASKS}
          </span>
        </div>
      </section>

      <section className="flex flex-col gap-3 rounded-sm border border-line p-4 border-line">
        <h2 className="font-medium">2. Lanza las tareas</h2>
        {queue.length === 0 ? (
          <p className="text-sm text-dim">La lista está vacía.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {queue.map((item, index) => (
              <li
                key={item.localId}
                className="flex items-center justify-between gap-3 rounded-sm border border-line px-3 py-2 text-sm border-line"
              >
                <span>
                  Tarea {index + 1} · {typeLabel(item.type)}
                </span>
                <button
                  className="text-black/40 hover:text-red-600 dark:text-white/40"
                  onClick={() => removeFromQueue(item.localId)}
                >
                  Quitar
                </button>
              </li>
            ))}
          </ul>
        )}
        <div className="flex justify-center gap-3">
          <button className={greenButton} disabled={busy || queue.length === 0} onClick={launch}>
            {busy ? "Lanzando..." : `Lanzar tareas (${queue.length})`}
          </button>
          <button
            className={neutralButton}
            disabled={busy || (queue.length === 0 && jobs.length === 0)}
            onClick={reset}
          >
            Limpiar
          </button>
        </div>
      </section>

      <section className="flex flex-col gap-3 rounded-sm border border-line p-4 border-line">
        <h2 className="font-medium">
          3. Resultados{" "}
          {tracking && (
            <span className="text-xs text-dim">(trackeando…)</span>
          )}
        </h2>
        {jobs.length === 0 ? (
          <p className="text-sm text-dim">Aún no has lanzado tareas.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {jobs.map((j, index) => (
              <li
                key={j.localId}
                className="flex items-center justify-between gap-3 rounded-sm border border-line px-3 py-2 text-sm border-line"
              >
                <span>
                  Tarea {index + 1} · {typeLabel(j.job.type)}
                </span>
                <span className={`text-right ${statusColor[j.job.status]}`}>{describe(j.job)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <footer className="mt-2 rounded-sm border border-amber-500/30 bg-amber-500/5 p-4 text-xs text-dim">
        ⚠️ El estado se refresca con <strong>polling</strong> cada 0.5s. En el{" "}
        <strong>Módulo 6</strong> lo cambiaremos por <strong>SSE</strong> para recibir cada cambio
        al instante. La tarea «caída» no hace <code>XACK</code>: queda pendiente y el reclaimer la
        reencola (la verás <em>reintentar</em> y terminar <strong>tras rescate</strong>).
      </footer>
    </div>
  );
}

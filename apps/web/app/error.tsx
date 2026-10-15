"use client";

import Link from "next/link";

export default function Error({ reset }: { error: Error; reset: () => void }) {
  return (
    <div className="flex flex-col items-center gap-5 py-24 text-center">
      <p className="font-display text-7xl text-red-400">500</p>
      <p className="text-dim">Algo falló al cargar esta página.</p>
      <div className="flex justify-center gap-3">
        <button className="btn btn-primary" onClick={() => reset()}>
          Reintentar
        </button>
        <Link href="/" className="btn btn-ghost">
          $ cd ~
        </Link>
      </div>
    </div>
  );
}

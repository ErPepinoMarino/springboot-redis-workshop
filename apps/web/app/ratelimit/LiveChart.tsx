"use client";

export type LivePoint = { plain: number; bucket: number };

export function LiveChart({ points, maxY }: { points: LivePoint[]; maxY: number }) {
  const width = 680;
  const height = 240;
  const pad = 40;
  const n = points.length;
  const safeMax = Math.max(maxY, 1);
  const gridLines = 4;
  const ticksPerSecond = 4; // 0.25s por tick

  const sx = (i: number) => pad + (n <= 1 ? 0 : (i / (n - 1)) * (width - 2 * pad));
  const sy = (v: number) => height - pad - (v / safeMax) * (height - 2 * pad);

  const line = (get: (p: LivePoint) => number) =>
    points.map((p, i) => `${sx(i)},${sy(get(p))}`).join(" ");

  const yTicks = Array.from({ length: gridLines + 1 }, (_, k) => (safeMax * k) / gridLines);

  return (
    <div className="flex flex-col gap-2">
      <svg viewBox={`0 0 ${width} ${height}`} className="w-full text-black/60 dark:text-white/60">
        {/* rejilla horizontal + etiquetas Y */}
        {yTicks.map((v, k) => (
          <g key={`y${k}`}>
            <line
              x1={pad}
              y1={sy(v)}
              x2={width - pad}
              y2={sy(v)}
              className="stroke-black/10 dark:stroke-white/10"
            />
            <text
              x={pad - 6}
              y={sy(v) + 4}
              textAnchor="end"
              fill="currentColor"
              className="text-[10px]"
            >
              {Math.round(v)}
            </text>
          </g>
        ))}
        {/* rejilla vertical + etiquetas X (segundos) */}
        {points.map((_, i) =>
          i % ticksPerSecond === 0 ? (
            <g key={`x${i}`}>
              <line
                x1={sx(i)}
                y1={pad}
                x2={sx(i)}
                y2={sy(0)}
                className="stroke-black/5 dark:stroke-white/5"
              />
              <text
                x={sx(i)}
                y={height - pad + 14}
                textAnchor="middle"
                fill="currentColor"
                className="text-[10px]"
              >
                {i / ticksPerSecond}s
              </text>
            </g>
          ) : null
        )}
        {/* ejes */}
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
        {/* series */}
        <polyline
          points={line((p) => p.plain)}
          fill="none"
          className="stroke-red-500"
          strokeWidth={2}
        />
        <polyline
          points={line((p) => p.bucket)}
          fill="none"
          className="stroke-emerald-500"
          strokeWidth={2}
        />
        {/* títulos de ejes */}
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
          x={pad - 28}
          y={height / 2}
          textAnchor="middle"
          fill="currentColor"
          transform={`rotate(-90 ${pad - 28} ${height / 2})`}
          className="text-[12px]"
        >
          Req/tick
        </text>
      </svg>
      <div className="flex gap-4 text-xs">
        <span className="flex items-center gap-1">
          <span className="inline-block h-1 w-4 bg-red-500" /> Sin bucket
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block h-1 w-4 bg-emerald-500" /> Con bucket
        </span>
      </div>
    </div>
  );
}

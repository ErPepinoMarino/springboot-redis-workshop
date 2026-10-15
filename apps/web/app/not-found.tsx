import Link from "next/link";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center gap-5 py-24 text-center">
      <p className="glow-cyan font-display text-7xl text-cyan">404</p>
      <p className="text-dim">Ruta no encontrada.</p>
      <Link href="/" className="btn btn-ghost">
        $ cd ~
      </Link>
    </div>
  );
}

import type { NextConfig } from "next";

// En local apunta a localhost; dentro de Compose, a "backend:8000".
const backendUrl = process.env.BACKEND_URL || "http://localhost:8000";

const nextConfig: NextConfig = {
  //Con compress: false evitamos el buffering que impide
  //que el ejemplo del reloj con sse se congele.
  //no es lo primera vez que me pasa. Maldito next.js.
  compress: false,

  // Salida "standalone": servidor mínimo para Docker (sin node_modules entero).
  output: "standalone",

  async rewrites() {
    return [
      {
        //apaño para que el front pueda hacer peticiones al backend sin problemas de CORS
        source: "/api/:path*",
        destination: `${backendUrl}/:path*`,
      },
    ];
  },
};

export default nextConfig;

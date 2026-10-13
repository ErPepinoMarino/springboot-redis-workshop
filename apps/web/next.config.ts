import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  //Con compress: false evitamos el buffering que impide
  //que el ejemplo del reloj con sse se congele.
  //no es lo primera vez que me pasa. Maldito next.js.
  compress: false,
  async rewrites() {
    return [
      {
        //apaño para que el front pueda hacer peticiones al backend sin problemas de CORS
        source: "/api/:path*",
        destination: "http://localhost:8000/:path*",
      },
    ];
  },
};

export default nextConfig;

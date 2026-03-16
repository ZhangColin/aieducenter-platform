/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  transpilePackages: ['@aieducenter/ui', '@aieducenter/api-client', '@aieducenter/shared'],
  output: 'standalone', // Docker 部署需要

  // API 代理到后端
  rewrites: async () => {
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080'
    return [
      {
        source: '/api/:path*',
        destination: `${backendUrl}/api/:path*`,
      },
    ]
  },
}

export default nextConfig

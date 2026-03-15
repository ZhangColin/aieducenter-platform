import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  reactStrictMode: true,
  transpilePackages: ['@aieducenter/ui', '@aieducenter/api-client', '@aieducenter/shared'],
}

export default nextConfig

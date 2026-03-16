import { HealthCheckCard } from '@/components/health-check-card'

export default function HomePage() {
  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">海创元智研云平台 - 用户端</h1>
      <HealthCheckCard />
    </main>
  )
}

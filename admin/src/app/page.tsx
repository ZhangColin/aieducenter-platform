import { AdminHealthCheck } from '@/components/admin-health-check'

export default function HomePage() {
  return (
    <main style={{ padding: '2rem' }}>
      <h1>运营后台</h1>
      <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>平台运营管理</p>
      <AdminHealthCheck />
    </main>
  )
}

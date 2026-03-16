'use client'

import { useEffect, useState } from 'react'

type HealthStatus = 'loading' | 'healthy' | 'error'

interface HealthResponse {
  code: number
  message: string
  data: {
    status: 'ok'
    timestamp: string
  }
}

export function AdminHealthCheck() {
  const [status, setStatus] = useState<HealthStatus>('loading')
  const [lastChecked, setLastChecked] = useState<string>('-')
  const [errorMessage, setErrorMessage] = useState<string>('')

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await fetch('/api/health')

        if (!response.ok) {
          setStatus('error')
          setErrorMessage(`HTTP ${response.status}`)
          return
        }

        const result: HealthResponse = await response.json()

        if (result.data?.status === 'ok') {
          setStatus('healthy')
          const timestamp = new Date(result.data.timestamp).toLocaleString('zh-CN', {
            hour12: false,
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
          })
          setLastChecked(timestamp)
        }
      } catch (err) {
        setStatus('error')
        setErrorMessage(err instanceof Error ? err.message : '连接失败')
      }
    }

    checkHealth()
  }, [])

  return (
    <div style={{ border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1rem', maxWidth: '400px', background: 'white' }}>
      <h3 style={{ fontSize: '1.125rem', fontWeight: '600', marginBottom: '0.5rem' }}>后端连接</h3>

      {status === 'loading' && (
        <p style={{ color: '#6b7280' }}>检查中...</p>
      )}

      {status === 'healthy' && (
        <div>
          <p style={{ color: '#16a34a', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span>✅</span>
            <span>正常</span>
          </p>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem' }}>
            最后检查: {lastChecked}
          </p>
        </div>
      )}

      {status === 'error' && (
        <div>
          <p style={{ color: '#dc2626', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span>❌</span>
            <span>异常</span>
          </p>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem' }}>
            {errorMessage}
          </p>
        </div>
      )}
    </div>
  )
}

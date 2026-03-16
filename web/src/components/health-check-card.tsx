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

export function HealthCheckCard() {
  const [status, setStatus] = useState<HealthStatus>('loading')
  const [lastChecked, setLastChecked] = useState<string>('-')
  const [errorMessage, setErrorMessage] = useState<string>('')

  useEffect(() => {
    const checkHealth = async () => {
      try {
        // 使用相对路径，由 Next.js rewrites 代理到后端
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
    <div className="border rounded-lg p-4 max-w-sm bg-white dark:bg-gray-800">
      <h3 className="text-lg font-semibold mb-2">后端连接</h3>

      {status === 'loading' && (
        <p className="text-gray-500">检查中...</p>
      )}

      {status === 'healthy' && (
        <div>
          <p className="text-green-600 flex items-center gap-2">
            <span>✅</span>
            <span>正常</span>
          </p>
          <p className="text-sm text-gray-500 mt-1">
            最后检查: {lastChecked}
          </p>
        </div>
      )}

      {status === 'error' && (
        <div>
          <p className="text-red-600 flex items-center gap-2">
            <span>❌</span>
            <span>异常</span>
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {errorMessage}
          </p>
        </div>
      )}
    </div>
  )
}

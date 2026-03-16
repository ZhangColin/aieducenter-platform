import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080'

export function middleware(request: NextRequest) {
  // 只代理 /api/* 请求
  if (request.nextUrl.pathname.startsWith('/api/')) {
    const url = request.nextUrl.clone()
    const apiUrl = `${BACKEND_URL}${url.pathname}${url.search}`

    // 代理请求到后端
    return fetch(apiUrl, {
      method: request.method,
      headers: request.headers,
      body: request.body,
      // @ts-ignore
      duplex: 'half',
    }).then(async (response) => {
      const data = await response.arrayBuffer()
      const headers = new Headers()

      // 复制响应头（排除一些不需要的）
      response.headers.forEach((value, key) => {
        if (!['content-encoding', 'transfer-encoding'].includes(key)) {
          headers.set(key, value)
        }
      })

      return new NextResponse(data, {
        status: response.status,
        headers,
      })
    })
  }

  return NextResponse.next()
}

export const config = {
  matcher: '/api/:path*',
}

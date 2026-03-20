import './globals.css'

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="zh-CN">
      <head>
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet" />
      </head>
      <body className="bg-background-light text-slate-900" style={{ fontFamily: 'Inter, Noto Sans SC, PingFang SC, Microsoft YaHei, sans-serif' }}>
        {children}
      </body>
    </html>
  )
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="zh-CN">
      <body>
        <div style={{ display: 'flex', minHeight: '100vh' }}>
          <aside style={{ width: '240px', padding: '1rem', borderRight: '1px solid #e5e5e5' }}>
            运营后台
          </aside>
          <main style={{ flex: 1 }}>{children}</main>
        </div>
      </body>
    </html>
  )
}

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <header style={{ padding: '1rem', borderBottom: '1px solid #e5e5e5' }}>
        AI 研云
      </header>
      <main>{children}</main>
    </>
  )
}

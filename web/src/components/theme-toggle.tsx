'use client'

import { useTheme } from '@aieducenter/ui'
import { Button } from '@aieducenter/ui'

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()

  return (
    <Button
      onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
      variant="outline"
      size="sm"
    >
      切换主题 ({theme === 'dark' ? '暗' : '亮'})
    </Button>
  )
}

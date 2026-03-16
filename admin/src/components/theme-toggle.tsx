'use client'

import { useTheme } from '@aieducenter/ui'
import { Button } from '@aieducenter/ui'
import { Moon, Sun } from 'lucide-react'

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  const isDark = theme === 'dark'

  return (
    <Button
      onClick={() => setTheme(isDark ? 'light' : 'dark')}
      variant="outline"
      size="sm"
      className="gap-2"
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
      {isDark ? '切换到亮色' : '切换到暗色'}
    </Button>
  )
}

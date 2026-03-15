import {
  Button,
  Input,
  Label,
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '@aieducenter/ui'
import { ThemeToggle } from '@/components/theme-toggle'

export default function ShowcasePage() {
  return (
    <div className="container mx-auto py-8 space-y-4">
      <ThemeToggle />

      <Card>
        <CardHeader>
          <CardTitle>组件展示</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label>输入框</Label>
            <Input placeholder="测试输入" />
          </div>
          <div className="flex gap-2">
            <Button>默认按钮</Button>
            <Button variant="outline">轮廓按钮</Button>
            <Button variant="secondary">次要按钮</Button>
            <Button variant="destructive">危险按钮</Button>
            <Button variant="ghost">幽灵按钮</Button>
            <Button variant="link">链接按钮</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

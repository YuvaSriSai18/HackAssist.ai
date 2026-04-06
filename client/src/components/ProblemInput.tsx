import { Wand2 } from 'lucide-react'
import { Button } from './ui/button'
import { Textarea } from './ui/textarea'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'

type ProblemInputProps = {
  problem: string
  onChange: (value: string) => void
  onGenerate: () => void
  loading: boolean
}

export function ProblemInput({ problem, onChange, onGenerate, loading }: ProblemInputProps) {
  const isDisabled = loading || !problem.trim()

  return (
    <Card className="bg-white/4">
      <CardHeader>
        <CardTitle>Problem Intake</CardTitle>
        <CardDescription>Describe the challenge, outcomes, and constraints.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Textarea value={problem} onChange={(event) => onChange(event.target.value)} />
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-white/50">{problem.length} characters</p>
          <Button onClick={onGenerate} disabled={isDisabled} variant="accent">
            <Wand2 size={18} />
            {loading ? 'Generating...' : 'Generate Tasks'}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

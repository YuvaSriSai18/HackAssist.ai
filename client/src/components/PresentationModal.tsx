import { useMemo, useState } from 'react'
import { Copy, Presentation, Sparkles } from 'lucide-react'
import { Button } from './ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from './ui/dialog'
import type { Task } from '../models/types'

type PresentationModalProps = {
  problem: string
  tasks: Task[]
}

export function PresentationModal({ problem, tasks }: PresentationModalProps) {
  const [copied, setCopied] = useState(false)

  const summary = useMemo(() => {
    const features = tasks.slice(0, 4).map((task) => `- ${task.title}`).join('\n')
    const taskLines = tasks.map((task) => `• ${task.title} (${task.status})`).join('\n')

    return `Problem Statement:\n${problem}\n\nKey Features:\n${features}\n\nTask Plan:\n${taskLines}`
  }, [problem, tasks])

  const handleCopy = async () => {
    await navigator.clipboard.writeText(summary)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="accent">
          <Presentation size={18} />
          Generate Summary
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Presentation Summary</DialogTitle>
          <DialogDescription>Copy into your pitch deck or demo script.</DialogDescription>
        </DialogHeader>
        <div className="space-y-3 text-sm text-white/70">
          <div className="rounded-xl border border-white/10 bg-white/5 p-4 whitespace-pre-line">
            {summary}
          </div>
          <Button variant="outline" onClick={handleCopy}>
            <Copy size={16} />
            {copied ? 'Copied!' : 'Copy to Clipboard'}
          </Button>
          <div className="flex items-center gap-2 text-xs text-white/50">
            <Sparkles size={14} />
            Summary uses current tasks and status snapshots.
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

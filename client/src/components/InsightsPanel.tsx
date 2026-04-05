import { Activity, Gauge, Users } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'
import { Progress } from './ui/progress'

type InsightsPanelProps = {
  progress: number
  total: number
  completed: number
  contributors: { name: string; tasks: number; velocity: string }[]
}

export function InsightsPanel({ progress, total, completed, contributors }: InsightsPanelProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Insights</CardTitle>
        <CardDescription>Momentum snapshot for the team.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-white/60">Progress</span>
            <span className="text-white">{progress}%</span>
          </div>
          <Progress value={progress} />
          <div className="flex items-center justify-between text-xs text-white/50">
            <span>{completed} completed</span>
            <span>{total} total</span>
          </div>
        </div>

        <div className="space-y-3">
          <div className="flex items-center gap-2 text-white/70">
            <Gauge size={16} />
            Velocity
          </div>
          <div className="rounded-xl border border-white/10 p-3 text-sm text-white/60">
            Sprint is tracking 14% above baseline with reduced blockers.
          </div>
        </div>

        <div className="space-y-3">
          <div className="flex items-center gap-2 text-white/70">
            <Users size={16} />
            Contributors
          </div>
          <div className="space-y-2">
            {contributors.map((contributor) => (
              <div key={contributor.name} className="flex items-center justify-between text-sm">
                <div>
                  <p className="text-white">{contributor.name}</p>
                  <p className="text-xs text-white/50">{contributor.velocity}</p>
                </div>
                <span className="text-white/70">{contributor.tasks} tasks</span>
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-2 text-xs text-white/50">
          <Activity size={14} />
          Next sync in 1h 12m
        </div>
      </CardContent>
    </Card>
  )
}

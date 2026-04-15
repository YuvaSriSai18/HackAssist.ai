import { Badge } from './ui/badge'
import type { Task } from '../models/types'
import { TaskPriority } from '../models/types'

type TaskCardProps = {
  task: Task
}

const badgeMap: Record<TaskPriority, 'high' | 'medium' | 'low'> = {
  [TaskPriority.HIGH]: 'high',
  [TaskPriority.MEDIUM]: 'medium',
  [TaskPriority.LOW]: 'low',
}

export function TaskCard({ task }: TaskCardProps) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div className="flex items-center justify-between">
        <h4 className="text-sm font-semibold text-white">{task.title}</h4>
        <Badge variant={badgeMap[task.priority]}>{task.priority}</Badge>
      </div>
      <p className="mt-2 text-xs text-white/60">{task.description}</p>
      <div className="mt-3 flex flex-wrap gap-2 text-xs text-white/50">
        <span>Owner: {task.assignee ?? 'Unassigned'}</span>
        <span>Status: {task.status}</span>
      </div>
      {task.commits.length > 0 && (
        <div className="mt-3 space-y-1 text-xs text-white/50">
          <p className="text-[11px] uppercase tracking-[0.2em] text-white/40">Latest commits</p>
          {task.commits.map((commit) => (
            <div key={commit.hash} className="flex items-center justify-between">
              <span className="font-mono text-[10px]">{commit.hash}</span>
              <span className="text-white/60">{commit.message}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

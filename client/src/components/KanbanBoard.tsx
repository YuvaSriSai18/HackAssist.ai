import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'
import type { Task } from '../models/types'
import { TaskStatus } from '../models/types'
import { TaskCard } from './TaskCard'

type KanbanBoardProps = {
  tasks: Task[]
}

const columns: TaskStatus[] = [TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE]

export function KanbanBoard({ tasks }: KanbanBoardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Execution Kanban</CardTitle>
        <CardDescription>Tasks automatically progress through phases as AI evaluates the codebase.</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4 lg:grid-cols-3">
        {tasks.length === 0 && (
          <div className="col-span-full rounded-2xl border border-dashed border-white/20 p-6 text-center text-sm text-white/60">
            No tasks yet. Generate tasks to populate the board.
          </div>
        )}
        {columns.map((status) => (
          <div key={status} className="space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-semibold text-white">{status.split('_').join(' ')}</h4>
              <span className="text-xs text-white/50">
                {tasks.filter((task) => task.status === status).length}
              </span>
            </div>
            <div className="space-y-3">
              {tasks
                .filter((task) => task.status === status)
                .map((task) => (
                  <TaskCard key={task.id} task={task} />
                ))}
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}

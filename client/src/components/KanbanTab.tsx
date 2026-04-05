import { DashboardLayout } from './DashboardLayout'
import { KanbanBoard } from './KanbanBoard'
import { RiskAlerts } from './RiskAlerts'
import type { Task } from '../models/types'
import { TaskStatus } from '../models/types'

type KanbanTabProps = {
  tasks: Task[]
  onMove: (id: string, status: TaskStatus) => void
  warnings: string[]
}

export function KanbanTab({ tasks, onMove, warnings }: KanbanTabProps) {
  return (
    <DashboardLayout
      left={<KanbanBoard tasks={tasks} onMove={onMove} />}
      right={<RiskAlerts warnings={warnings} />}
    />
  )
}

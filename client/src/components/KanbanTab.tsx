import { DashboardLayout } from './DashboardLayout'
import { KanbanBoard } from './KanbanBoard'
import { RiskAlerts } from './RiskAlerts'
import type { Task } from '../models/types'

type KanbanTabProps = {
  tasks: Task[]
  warnings: string[]
}

export function KanbanTab({ tasks, warnings }: KanbanTabProps) {
  return (
    <DashboardLayout
      left={<KanbanBoard tasks={tasks} />}
      right={<RiskAlerts warnings={warnings} />}
    />
  )
}

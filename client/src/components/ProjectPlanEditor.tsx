import { useMemo, useState } from 'react'
import { BadgeCheck, Pencil, Plus, Save, Trash2 } from 'lucide-react'
import type { PlanModule, PlanTask, ProjectPlan } from '../models/types'
import { TaskPriority, TaskStatus } from '../models/types'
import { Badge } from './ui/badge'
import { Button } from './ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'

const priorityMap: Record<TaskPriority, 'high' | 'medium' | 'low'> = {
  [TaskPriority.HIGH]: 'high',
  [TaskPriority.MEDIUM]: 'medium',
  [TaskPriority.LOW]: 'low',
}

type NewTaskState = {
  title: string
  description: string
  priority: TaskPriority
  status: TaskStatus
  moduleKey: string
}

type ProjectPlanEditorProps = {
  plan: ProjectPlan
  onPlanChange: (plan: ProjectPlan) => void
  onSave: () => void
  saving: boolean
  error?: string | null
}

export function ProjectPlanEditor({ plan, onPlanChange, onSave, saving, error }: ProjectPlanEditorProps) {
  const [editingTask, setEditingTask] = useState<PlanTask | null>(null)
  const [newTask, setNewTask] = useState<NewTaskState>({
    title: '',
    description: '',
    priority: TaskPriority.MEDIUM,
    status: TaskStatus.TODO,
    moduleKey: '',
  })

  const modules = plan.modules
  const moduleMap = useMemo(() => new Map(modules.map((mod) => [mod.key, mod])), [modules])

  const groupedTasks = useMemo(() => {
    const groups: Record<string, PlanTask[]> = {}
    for (const task of plan.tasks) {
      const key = task.moduleKey ?? 'unassigned'
      if (!groups[key]) groups[key] = []
      groups[key].push(task)
    }
    return groups
  }, [plan.tasks])

  const nextExternalId = () => {
    const ids = plan.tasks.map((task) => task.externalId)
    const max = ids
      .map((id) => Number(id.replace('TSK-', '')))
      .filter((n) => !Number.isNaN(n))
      .sort((a, b) => b - a)[0]
    const next = Number.isFinite(max) ? (max as number) + 1 : 1
    return `TSK-${String(next).padStart(3, '0')}`
  }

  const updateTask = (updated: PlanTask) => {
    onPlanChange({
      ...plan,
      tasks: plan.tasks.map((task) => (task.externalId === updated.externalId ? updated : task)),
    })
  }

  const addTask = () => {
    if (!newTask.title.trim()) return
    const task: PlanTask = {
      externalId: nextExternalId(),
      title: newTask.title,
      description: newTask.description,
      priority: newTask.priority,
      status: newTask.status,
      moduleKey: newTask.moduleKey || undefined,
      dependsOn: [],
    }
    onPlanChange({ ...plan, tasks: [task, ...plan.tasks] })
    setNewTask({
      title: '',
      description: '',
      priority: TaskPriority.MEDIUM,
      status: TaskStatus.TODO,
      moduleKey: '',
    })
  }

  const deleteTask = (externalId: string) => {
    onPlanChange({
      ...plan,
      tasks: plan.tasks.filter((task) => task.externalId !== externalId),
    })
  }

  const renderModuleLabel = (moduleKey: string, module?: PlanModule) => {
    if (module) return module.name
    if (moduleKey === 'unassigned') return 'Unassigned'
    return moduleKey
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>AI Task Plan Review</CardTitle>
        <CardDescription>Edit tasks, assign modules, and finalize the plan.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-white/70">
          <p className="text-xs uppercase tracking-[0.2em] text-white/40">Tech Stack</p>
          <p className="mt-2">Backend: {plan.techStack.backend}</p>
          <p>Frontend: {plan.techStack.frontend}</p>
          <p>Database: {plan.techStack.database}</p>
          <p>Architecture: {plan.techStack.architecture}</p>
        </div>

        <div className="grid gap-3 md:grid-cols-[1.4fr_1fr_1fr_auto]">
          <Input
            placeholder="New task title"
            value={newTask.title}
            onChange={(event) => setNewTask((prev) => ({ ...prev, title: event.target.value }))}
          />
          <select
            className="h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white"
            value={newTask.priority}
            onChange={(event) =>
              setNewTask((prev) => ({ ...prev, priority: event.target.value as TaskPriority }))
            }
          >
            <option value={TaskPriority.HIGH}>High</option>
            <option value={TaskPriority.MEDIUM}>Medium</option>
            <option value={TaskPriority.LOW}>Low</option>
          </select>
          <select
            className="h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white"
            value={newTask.moduleKey}
            onChange={(event) => setNewTask((prev) => ({ ...prev, moduleKey: event.target.value }))}
          >
            <option value="">Unassigned</option>
            {modules.map((module) => (
              <option key={module.key} value={module.key}>
                {module.name}
              </option>
            ))}
          </select>
          <Button variant="outline" onClick={addTask}>
            <Plus size={16} />
            Add Task
          </Button>
          <div className="md:col-span-4">
            <Textarea
              placeholder="Describe the task"
              value={newTask.description}
              onChange={(event) => setNewTask((prev) => ({ ...prev, description: event.target.value }))}
            />
          </div>
        </div>

        <div className="space-y-5">
          {Object.entries(groupedTasks).map(([moduleKey, tasks]) => {
            const module = moduleMap.get(moduleKey)
            return (
              <div key={moduleKey} className="space-y-3">
                <h4 className="text-sm font-semibold text-white">
                  {renderModuleLabel(moduleKey, module)}
                </h4>
                {tasks.map((task) => (
                  <div key={task.externalId} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <h4 className="text-base font-semibold text-white">{task.title}</h4>
                          <Badge variant={priorityMap[task.priority]}>{task.priority}</Badge>
                        </div>
                        <p className="text-sm text-white/60">{task.description}</p>
                        <div className="flex flex-wrap gap-2 text-xs text-white/40">
                          <span>ID: {task.externalId}</span>
                          <span>Status: {task.status}</span>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button variant="ghost" onClick={() => setEditingTask(task)}>
                              <Pencil size={16} />
                              Edit
                            </Button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle>Edit Task</DialogTitle>
                              <DialogDescription>Update the AI task details.</DialogDescription>
                            </DialogHeader>
                            {editingTask && (
                              <div className="space-y-3">
                                <Input
                                  value={editingTask.title}
                                  onChange={(event) =>
                                    setEditingTask({ ...editingTask, title: event.target.value })
                                  }
                                />
                                <Textarea
                                  value={editingTask.description}
                                  onChange={(event) =>
                                    setEditingTask({
                                      ...editingTask,
                                      description: event.target.value,
                                    })
                                  }
                                />
                                <select
                                  className="h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white"
                                  value={editingTask.priority}
                                  onChange={(event) =>
                                    setEditingTask({
                                      ...editingTask,
                                      priority: event.target.value as TaskPriority,
                                    })
                                  }
                                >
                                  <option value={TaskPriority.HIGH}>High</option>
                                  <option value={TaskPriority.MEDIUM}>Medium</option>
                                  <option value={TaskPriority.LOW}>Low</option>
                                </select>
                                <select
                                  className="h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white"
                                  value={editingTask.status}
                                  onChange={(event) =>
                                    setEditingTask({
                                      ...editingTask,
                                      status: event.target.value as TaskStatus,
                                    })
                                  }
                                >
                                  <option value={TaskStatus.TODO}>Todo</option>
                                  <option value={TaskStatus.IN_PROGRESS}>In Progress</option>
                                  <option value={TaskStatus.DONE}>Done</option>
                                </select>
                                <select
                                  className="h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white"
                                  value={editingTask.moduleKey ?? ''}
                                  onChange={(event) =>
                                    setEditingTask({
                                      ...editingTask,
                                      moduleKey: event.target.value || undefined,
                                    })
                                  }
                                >
                                  <option value="">Unassigned</option>
                                  {modules.map((mod) => (
                                    <option key={mod.key} value={mod.key}>
                                      {mod.name}
                                    </option>
                                  ))}
                                </select>
                                <Button
                                  variant="accent"
                                  onClick={() => {
                                    updateTask(editingTask)
                                    setEditingTask(null)
                                  }}
                                >
                                  <BadgeCheck size={16} />
                                  Save Changes
                                </Button>
                              </div>
                            )}
                          </DialogContent>
                        </Dialog>
                        <Button variant="ghost" onClick={() => deleteTask(task.externalId)}>
                          <Trash2 size={16} />
                          Delete
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )
          })}
        </div>

        {error ? <p className="text-xs text-red-300">{error}</p> : null}

        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="text-sm text-white/50">Review and finalize to save into the database.</div>
          <Button variant="accent" onClick={onSave} disabled={saving}>
            <Save size={18} />
            {saving ? 'Saving...' : 'Finalize Plan'}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

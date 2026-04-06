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
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from './ui/accordion'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select'

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
    moduleKey: 'unassigned',
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
      problemStatement: plan.problemStatement,  // Explicitly preserve
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
      moduleKey: newTask.moduleKey !== 'unassigned' ? newTask.moduleKey : undefined,
      dependsOn: [],
    }
    onPlanChange({ 
      ...plan, 
      problemStatement: plan.problemStatement,  // Explicitly preserve
      tasks: [task, ...plan.tasks] 
    })
    setNewTask({
      title: '',
      description: '',
      priority: TaskPriority.MEDIUM,
      status: TaskStatus.TODO,
      moduleKey: 'unassigned',
    })
  }

  const deleteTask = (externalId: string) => {
    onPlanChange({
      ...plan,
      problemStatement: plan.problemStatement,  // Explicitly preserve
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
        {/* <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
          <p className="text-xs uppercase tracking-[0.2em] text-white/40 mb-3">Problem Summary</p>
          <p className="text-sm text-white/70">{plan.problemStatement}</p>
        </div> */}

        <div className="rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-white/70">
          <p className="text-xs uppercase tracking-[0.2em] text-white/40 mb-3">Suggesterd Tech Stack</p>
          {plan.techStack.backend &&<p className="mt-2">Backend: {plan.techStack.backend}</p>}
          {plan.techStack.frontend && <p>Frontend: {plan.techStack.frontend}</p>}
          { plan.techStack.database && <p>Database: {plan.techStack.database}</p>}
          {plan.techStack.architecture && <p>Architecture: {plan.techStack.architecture}</p>}
        </div>

        <div className="grid gap-3 md:grid-cols-[1.4fr_1fr_1fr_auto]">
          <Input
            placeholder="New task title"
            value={newTask.title}
            onChange={(event) => setNewTask((prev) => ({ ...prev, title: event.target.value }))}
          />
          <Select value={newTask.priority} onValueChange={(value) => setNewTask((prev) => ({ ...prev, priority: value as TaskPriority }))}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TaskPriority.HIGH}>{TaskPriority.HIGH}</SelectItem>
              <SelectItem value={TaskPriority.MEDIUM}>{TaskPriority.MEDIUM}</SelectItem>
              <SelectItem value={TaskPriority.LOW}>{TaskPriority.LOW}</SelectItem>
            </SelectContent>
          </Select>
          <Select value={newTask.moduleKey} onValueChange={(value) => setNewTask((prev) => ({ ...prev, moduleKey: value }))}>
            <SelectTrigger>
              <SelectValue placeholder="Select module" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="unassigned">Unassigned</SelectItem>
              {modules.map((module) => (
                <SelectItem key={module.key} value={module.key}>
                  {module.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
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

        <div className="space-y-4">
          <h3 className="text-sm font-semibold text-white uppercase tracking-[0.2em]">Tasks by Module</h3>
          <Accordion type="single" collapsible className='space-y-3'>
            {Object.entries(groupedTasks).map(([moduleKey, tasks]) => {
              const module = moduleMap.get(moduleKey)
              return (
                <AccordionItem key={moduleKey} value={moduleKey}>
                  <AccordionTrigger>
                    <span className="font-semibold">
                      {renderModuleLabel(moduleKey, module)} ({tasks.length})
                    </span>
                  </AccordionTrigger>
                  <AccordionContent>
                    <div className="space-y-3">
                      {tasks.map((task) => (
                        <div key={task.externalId} className="rounded-lg border border-white/10 bg-white/5 p-4">
                          <div className="flex flex-wrap items-start justify-between gap-4">
                            <div className="space-y-2 flex-1">
                              <div className="flex items-center gap-2">
                                <h4 className="text-base font-semibold text-white">{task.title}</h4>
                                <Badge variant={priorityMap[task.priority]}>{task.priority}</Badge>
                              </div>
                              <p className="text-sm text-white/60">{task.description}</p>
                              <div className="flex flex-wrap gap-3 text-xs text-white/40">
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
                              <div className="space-y-4">
                                <Input
                                  placeholder="Task title"
                                  value={editingTask.title}
                                  onChange={(event) =>
                                    setEditingTask({ ...editingTask, title: event.target.value })
                                  }
                                />
                                <Textarea
                                  placeholder="Task description"
                                  value={editingTask.description}
                                  onChange={(event) =>
                                    setEditingTask({
                                      ...editingTask,
                                      description: event.target.value,
                                    })
                                  }
                                />
                                <div className="space-y-2">
                                  <label className="text-xs text-white/60">Priority</label>
                                  <Select value={editingTask.priority} onValueChange={(value) => setEditingTask({ ...editingTask, priority: value as TaskPriority })}>
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent className=''>
                                      <SelectItem value={TaskPriority.HIGH}>{TaskPriority.HIGH}</SelectItem>
                                      <SelectItem value={TaskPriority.MEDIUM}>{TaskPriority.MEDIUM}</SelectItem>
                                      <SelectItem value={TaskPriority.LOW}>{TaskPriority.LOW}</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div className="space-y-2">
                                  <label className="text-xs text-white/60">Status</label>
                                  <Select value={editingTask.status} onValueChange={(value) => setEditingTask({ ...editingTask, status: value as TaskStatus })}>
                                    <SelectTrigger>
                                      <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value={TaskStatus.TODO}>{TaskStatus.TODO}</SelectItem>
                                      <SelectItem value={TaskStatus.IN_PROGRESS}>{TaskStatus.IN_PROGRESS}</SelectItem>
                                      <SelectItem value={TaskStatus.DONE}>{TaskStatus.DONE}</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div className="space-y-2">
                                  <label className="text-xs text-white/60">Module</label>
                                  <Select value={editingTask.moduleKey ?? 'unassigned'} onValueChange={(value) => setEditingTask({ ...editingTask, moduleKey: value === 'unassigned' ? undefined : value })}>
                                    <SelectTrigger>
                                      <SelectValue placeholder="Select module" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="unassigned">Unassigned</SelectItem>
                                      {modules.map((mod) => (
                                        <SelectItem key={mod.key} value={mod.key}>
                                          {mod.name}
                                        </SelectItem>
                                      ))}
                                    </SelectContent>
                                  </Select>
                                </div>
                                <Button
                                  variant="accent"
                                  className="w-full"
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
              </AccordionContent>
            </AccordionItem>
              )
            })}
          </Accordion>
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

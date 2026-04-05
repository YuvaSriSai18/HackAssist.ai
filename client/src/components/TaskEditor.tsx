import { useMemo, useState } from 'react'
import { BadgeCheck, Pencil, Plus, Save, Trash2 } from 'lucide-react'
import { Button } from './ui/button'
import { Badge } from './ui/badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'
import type { Task } from '../models/types'
import { TaskPriority, TaskStatus } from '../models/types'

type TaskEditorProps = {
  tasks: Task[]
  onUpdate: (tasks: Task[]) => void
  onSave: () => void
}

const priorityMap: Record<TaskPriority, 'high' | 'medium' | 'low'> = {
  [TaskPriority.HIGH]: 'high',
  [TaskPriority.MEDIUM]: 'medium',
  [TaskPriority.LOW]: 'low',
}

export function TaskEditor({ tasks, onUpdate, onSave }: TaskEditorProps) {
  const [editingTask, setEditingTask] = useState<Task | null>(null)
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    priority: TaskPriority.MEDIUM,
  })

  const stats = useMemo(() => {
    const total = tasks.length
    const done = tasks.filter((task) => task.status === TaskStatus.DONE).length
    return { total, done }
  }, [tasks])

  const updateTask = (updated: Task) => {
    onUpdate(tasks.map((task) => (task.id === updated.id ? updated : task)))
  }

  const addTask = () => {
    if (!newTask.title.trim()) return
    const task: Task = {
      id: `task_${Date.now()}`,
      title: newTask.title,
      description: newTask.description,
      priority: newTask.priority,
      status: TaskStatus.TODO,
      assignee: 'Unassigned',
      commits: [],
    }
    onUpdate([task, ...tasks])
    setNewTask({ title: '', description: '', priority: TaskPriority.MEDIUM })
  }

  const deleteTask = (id: string) => {
    onUpdate(tasks.filter((task) => task.id !== id))
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Task Generator & Editor</CardTitle>
        <CardDescription>
          {stats.done} of {stats.total} tasks completed. Refine priority, assign owners, and export.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-3 md:grid-cols-[1.4fr_1fr_auto]">
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
          <Button variant="outline" onClick={addTask}>
            <Plus size={16} />
            Add Task
          </Button>
          <div className="md:col-span-3">
            <Textarea
              placeholder="Describe the task"
              value={newTask.description}
              onChange={(event) => setNewTask((prev) => ({ ...prev, description: event.target.value }))}
            />
          </div>
        </div>

        <div className="space-y-4">
          {tasks.length === 0 && (
            <div className="rounded-2xl border border-dashed border-white/20 p-6 text-center text-sm text-white/60">
              No tasks yet. Generate tasks or add a new one.
            </div>
          )}
          {tasks.map((task) => (
            <div key={task.id} className="rounded-2xl border border-white/10 bg-white/5 p-4">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <h4 className="text-base font-semibold text-white">{task.title}</h4>
                    <Badge variant={priorityMap[task.priority]}>{task.priority}</Badge>
                  </div>
                  <p className="text-sm text-white/60">{task.description}</p>
                  <div className="flex flex-wrap gap-2 text-xs text-white/40">
                    <span>Status: {task.status}</span>
                    <span>Owner: {task.assignee ?? 'Unassigned'}</span>
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
                        <DialogDescription>Update the details for this work item.</DialogDescription>
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
                              setEditingTask({ ...editingTask, description: event.target.value })
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
                  <Button variant="ghost" onClick={() => deleteTask(task.id)}>
                    <Trash2 size={16} />
                    Delete
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="text-sm text-white/50">Changes are stored locally.</div>
          <Button variant="accent" onClick={onSave}>
            <Save size={18} />
            Save Tasks
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

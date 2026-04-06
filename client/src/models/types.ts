export type Project = {
  id: string
  name: string
  description: string
  owner: string
  members: string[]
  createdAt: string
}

export const TaskStatus = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
} as const

export const TaskPriority = {
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW',
} as const

export type TaskStatus = typeof TaskStatus[keyof typeof TaskStatus]
export type TaskPriority = typeof TaskPriority[keyof typeof TaskPriority]

export type Task = {
  id: string
  title: string
  description: string
  priority: TaskPriority
  status: TaskStatus
  assignee?: string
  commits: { hash: string; message: string }[]
}

export type Member = {
  id: string
  name: string
  role: string
}

export type User = {
  id: string
  name: string
  email: string
}

export type GithubState = {
  connected: boolean
  verified: boolean
}

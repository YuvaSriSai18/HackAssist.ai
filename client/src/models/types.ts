export type Project = {
  id: string
  backendId?: number
  projectId?: string
  name: string
  description: string
  owner: string
  members: string[]
  createdAt: string
  githubRepoUrl?: string
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
  module?: string
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

export type PlanFeature = {
  key: string
  name: string
  description: string
  priority: TaskPriority
}

export type PlanModule = {
  key: string
  name: string
  description: string
}

export type PlanRisk = {
  title: string
  impact: string
  mitigation: string
}

export type PlanTask = {
  externalId: string
  title: string
  description: string
  priority: TaskPriority
  status: TaskStatus
  estimatedHours?: number
  moduleKey?: string
  dependsOn?: string[]
}

export type TechStack = {
  backend: string
  frontend: string
  database: string
  architecture: string
}

export type ProjectPlan = {
  projectId: string
  problemStatement: string
  techStack: TechStack
  features: PlanFeature[]
  modules: PlanModule[]
  tasks: PlanTask[]
  risks: PlanRisk[]
}

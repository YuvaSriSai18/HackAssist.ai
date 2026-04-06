import type React from 'react'
import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { GithubState, Member, Project, Task, User } from '../models/types'
import { TaskPriority, TaskStatus } from '../models/types'
import {
  createProject as createProjectApi,
  deleteProject as deleteProjectApi,
  fetchMyProjects,
  linkProjectRepo as linkProjectRepoApi,
  updateProject as updateProjectApi,
} from '../apis'

type TasksByProject = Record<string, Task[]>

type MembersByProject = Record<string, Member[]>

type ProblemByProject = Record<string, string>

type AppState = {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  projects: Project[]
  sharedProjects: Project[]
  selectedProjectId: string | null
  tasks: TasksByProject
  membersByProject: MembersByProject
  problemByProject: ProblemByProject
  github: GithubState
  login: (email: string, password: string) => void
  setSession: (user: User, token: string) => void
  logout: () => void
  selectProject: (projectId: string) => void
  createProject: (request: { name: string; description: string }) => Promise<Project>
  loadProjects: () => Promise<void>
  updateProject: (projectId: string, request: { name?: string; description?: string }) => Promise<Project>
  deleteProject: (projectId: string) => Promise<void>
  linkProjectRepo: (projectId: string, repo: { repoUrl?: string; repoFullName?: string }) => Promise<Project>
  updateTasks: (projectId: string, tasks: Task[]) => void
  addMember: (projectId: string, member: Member) => void
  setProblem: (projectId: string, problem: string) => void
  updateGithub: (connected: boolean) => void
  setGithubConnected: (connected: boolean, verified?: boolean) => void
}

const AppStateContext = createContext<AppState | undefined>(undefined)

const mockProjects: Project[] = [
  {
    id: 'proj-01',
    name: 'HackAssist Command Center',
    description: 'AI-assisted workflow for hackathon operations.',
    owner: 'Ari Nakamura',
    members: ['Ari', 'Leo', 'Mina'],
    createdAt: '2026-04-01',
  },
  {
    id: 'proj-02',
    name: 'Pitch Flow Builder',
    description: 'Guided pitch creation with task automation.',
    owner: 'Ari Nakamura',
    members: ['Ari', 'Jay'],
    createdAt: '2026-03-28',
  },
]

const mockSharedProjects: Project[] = [
  {
    id: 'proj-03',
    name: 'Mobile Demo Sprint',
    description: 'Shared mobile MVP with new team members.',
    owner: 'Sana Patel',
    members: ['Sana', 'Ari', 'Mina'],
    createdAt: '2026-03-25',
  },
]

const mockTasks: Task[] = [
  {
    id: 'task-1',
    title: 'AI intake & problem framing',
    description: 'Summarize the hackathon problem and success criteria.',
    priority: TaskPriority.HIGH,
    status: TaskStatus.TODO,
    assignee: 'Ari',
    commits: [{ hash: 'a3c1f', message: 'Drafted problem intake template' }],
  },
  {
    id: 'task-2',
    title: 'Generate sprint tasks',
    description: 'Create the initial backlog from the AI engine.',
    priority: TaskPriority.HIGH,
    status: TaskStatus.IN_PROGRESS,
    assignee: 'Leo',
    commits: [
      { hash: 'f9b2d', message: 'Added backlog generation logic' },
      { hash: 'c7f11', message: 'Aligned priorities with scope' },
    ],
  },
  {
    id: 'task-3',
    title: 'Repo insight overlay',
    description: 'Mock GitHub repo selection and commits timeline.',
    priority: TaskPriority.MEDIUM,
    status: TaskStatus.IN_PROGRESS,
    assignee: 'Mina',
    commits: [{ hash: '9e1d4', message: 'Stubbed repo selector UI' }],
  },
  {
    id: 'task-4',
    title: 'Risk alert automation',
    description: 'Highlight blockers and stale activity warnings.',
    priority: TaskPriority.LOW,
    status: TaskStatus.DONE,
    assignee: 'Jay',
    commits: [{ hash: '4c91a', message: 'Added risk alert card design' }],
  },
]

const mockMembers: Member[] = [
  { id: 'm1', name: 'Ari Nakamura', role: 'Product Lead' },
  { id: 'm2', name: 'Leo Santos', role: 'Full-stack Engineer' },
  { id: 'm3', name: 'Mina Shah', role: 'UX Researcher' },
]

export function AppStateProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const raw = window.localStorage.getItem('authUser')
    if (!raw) return null
    try {
      return JSON.parse(raw) as User
    } catch {
      return null
    }
  })
  const [token, setToken] = useState<string | null>(() => window.localStorage.getItem('authToken'))
  const [isAuthenticated, setIsAuthenticated] = useState(Boolean(window.localStorage.getItem('authToken')))
  const [projects, setProjects] = useState<Project[]>(mockProjects)
  const [sharedProjects] = useState<Project[]>(mockSharedProjects)
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null)
  const [tasksByProject, setTasksByProject] = useState<TasksByProject>({
    [mockProjects[0].id]: mockTasks,
    [mockProjects[1].id]: [],
    [mockSharedProjects[0].id]: [],
  })
  const [membersByProject, setMembersByProject] = useState<MembersByProject>({
    [mockProjects[0].id]: mockMembers,
    [mockProjects[1].id]: mockMembers.slice(0, 2),
    [mockSharedProjects[0].id]: mockMembers,
  })
  const [problemByProject, setProblemByProject] = useState<ProblemByProject>({
    [mockProjects[0].id]: 'Build an AI-powered hackathon assistant that guides teams from idea to demo.',
    [mockProjects[1].id]: 'Create a pitch builder that turns outlines into polished demos.',
    [mockSharedProjects[0].id]: 'Ship a collaborative mobile demo for the hackathon finals.',
  })
  const [github, setGithub] = useState<GithubState>(() => ({
    connected: false,
    verified: false,
  }))

  const mapProjectResponse = useCallback(
    (item: {
      id?: number
      projectId?: string
      name?: string
      description?: string
      createdAt?: string
      githubRepoUrl?: string
    }) => {
      const ownerName = user?.name ?? 'You'
      return {
        id: item.projectId ?? String(item.id ?? `proj-${Date.now()}`),
        backendId: item.id,
        projectId: item.projectId,
        name: item.name ?? 'Untitled Project',
        description: item.description ?? '',
        owner: ownerName,
        members: ownerName ? [ownerName] : [],
        createdAt: item.createdAt ? item.createdAt.split('T')[0] : new Date().toISOString().split('T')[0],
        githubRepoUrl: item.githubRepoUrl,
      } satisfies Project
    },
    [user?.name]
  )

  const persistSession = useCallback((nextUser: User, nextToken: string) => {
    setUser(nextUser)
    setToken(nextToken)
    setIsAuthenticated(true)
    window.localStorage.setItem('authUser', JSON.stringify(nextUser))
    window.localStorage.setItem('authToken', nextToken)
  }, [])

  const persistGithubStatus = useCallback((connected: boolean, verified?: boolean) => {
    setGithub((prev) => ({
      connected,
      verified: verified ?? prev.verified,
    }))
  }, [])

  const setGithubConnected = useCallback(
    (connected: boolean, verified?: boolean) => {
      persistGithubStatus(connected, verified)
    },
    [persistGithubStatus]
  )

  const updateGithub = useCallback(
    (connected: boolean) => {
      persistGithubStatus(connected)
    },
    [persistGithubStatus]
  )

  const loadProjects = useCallback(async () => {
    if (!token) {
      setProjects([])
      return
    }
    const data = await fetchMyProjects(token)
    setProjects(data.map(mapProjectResponse))
  }, [mapProjectResponse, token])

  const createProject = useCallback(
    async (request: { name: string; description: string }) => {
      if (!token) {
        throw new Error('Authentication required')
      }
      const data = await createProjectApi(token, request)
      const project = mapProjectResponse(data)
      setProjects((prev) => [project, ...prev])
      setTasksByProject((prev) => ({ ...prev, [project.id]: [] }))
      setMembersByProject((prev) => ({ 
        ...prev, 
        [project.id]: project.members.map((name, idx) => ({
          id: `m-${project.id}-${idx}`,
          name,
          role: 'Member',
        }))
      }))
      setProblemByProject((prev) => ({ ...prev, [project.id]: '' }))
      return project
    },
    [mapProjectResponse, token]
  )

  const updateProject = useCallback(
    async (projectId: string, request: { name?: string; description?: string }) => {
      if (!token) {
        throw new Error('Authentication required')
      }
      const data = await updateProjectApi(token, projectId, request)
      const updated = mapProjectResponse(data)
      setProjects((prev) => prev.map((item) => (item.id === projectId ? { ...item, ...updated } : item)))
      return updated
    },
    [mapProjectResponse, token]
  )

  const deleteProject = useCallback(
    async (projectId: string) => {
      if (!token) {
        throw new Error('Authentication required')
      }
      await deleteProjectApi(token, projectId)
      setProjects((prev) => prev.filter((item) => item.id !== projectId))
      setTasksByProject((prev) => {
        const next = { ...prev }
        delete next[projectId]
        return next
      })
      setMembersByProject((prev) => {
        const next = { ...prev }
        delete next[projectId]
        return next
      })
      setProblemByProject((prev) => {
        const next = { ...prev }
        delete next[projectId]
        return next
      })
    },
    [token]
  )

  const linkProjectRepo = useCallback(
    async (projectId: string, repo: { repoUrl?: string; repoFullName?: string }) => {
      if (!token) {
        throw new Error('Authentication required')
      }
      const data = await linkProjectRepoApi(token, projectId, repo)
      const updated = mapProjectResponse(data)
      setProjects((prev) => prev.map((item) => (item.id === projectId ? { ...item, ...updated } : item)))
      return updated
    },
    [mapProjectResponse, token]
  )

  const value = useMemo<AppState>(
    () => ({
      user,
      token,
      isAuthenticated,
      projects,
      sharedProjects,
      selectedProjectId,
      tasks: tasksByProject,
      membersByProject,
      problemByProject,
      github,
      login: (email, _password) => {
        const safeName = email.split('@')[0] || 'Demo User'
        const nextUser = {
          id: `user-${Date.now()}`,
          name: safeName.charAt(0).toUpperCase() + safeName.slice(1),
          email,
        }
        const nextToken = `mock-token-${Date.now()}`
        persistSession(nextUser, nextToken)
      },
      setSession: (nextUser, nextToken) => {
        persistSession(nextUser, nextToken)
      },
      logout: () => {
        setUser(null)
        setToken(null)
        setIsAuthenticated(false)
        setSelectedProjectId(null)
        setGithub({ connected: false, verified: false })
        window.localStorage.removeItem('authUser')
        window.localStorage.removeItem('authToken')
      },
      selectProject: setSelectedProjectId,
      createProject,
      loadProjects,
      updateProject,
      deleteProject,
      linkProjectRepo,
      updateTasks: (projectId, tasks) => {
        setTasksByProject((prev) => ({ ...prev, [projectId]: tasks }))
      },
      addMember: (projectId, member) => {
        setMembersByProject((prev) => ({
          ...prev,
          [projectId]: [...(prev[projectId] ?? []), member],
        }))
      },
      setProblem: (projectId, problem) => {
        setProblemByProject((prev) => ({ ...prev, [projectId]: problem }))
      },
      updateGithub,
      setGithubConnected,
    }),
    [
      user,
      token,
      isAuthenticated,
      projects,
      sharedProjects,
      selectedProjectId,
      tasksByProject,
      membersByProject,
      problemByProject,
      github,
      createProject,
      loadProjects,
      updateProject,
      deleteProject,
      linkProjectRepo,
      updateGithub,
      setGithubConnected,
      persistSession,
      mapProjectResponse,
    ]
  )

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>
}

export function useAppState() {
  const context = useContext(AppStateContext)
  if (!context) {
    throw new Error('useAppState must be used within AppStateProvider')
  }
  return context
}

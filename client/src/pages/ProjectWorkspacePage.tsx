import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DashboardTab } from '../components/DashboardTab'
import { KanbanTab } from '../components/KanbanTab'
import { ProjectTabs } from '../components/ProjectTabs'
import { TeamTab } from '../components/TeamTab'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Skeleton } from '../components/ui/skeleton'
import { useAppState } from '../context/AppState'
import type { Project, Task } from '../models/types'
import { TaskPriority, TaskStatus } from '../models/types'

type ProjectTabKey = 'dashboard' | 'kanban' | 'team'

export function ProjectWorkspacePage() {
  const {
    projects,
    sharedProjects,
    tasks,
    membersByProject,
    problemByProject,
    updateTasks,
    addMember,
    selectProject,
    setProblem,
  } = useAppState()
  const { projectId } = useParams()
  const [activeTab, setActiveTab] = useState<ProjectTabKey>('dashboard')
  const [loadingTasks, setLoadingTasks] = useState(false)

  const project = useMemo<Project | undefined>(
    () =>
      projects.find((item) => item.id === projectId) ||
      sharedProjects.find((item) => item.id === projectId),
    [projects, sharedProjects, projectId]
  )

  useEffect(() => {
    if (project?.id) {
      selectProject(project.id)
    }
  }, [project?.id, selectProject])

  const projectTasks = projectId ? tasks[projectId] ?? [] : []
  const members = projectId ? membersByProject[projectId] ?? [] : []
  const problem = projectId ? problemByProject[projectId] ?? '' : ''

  const progress = useMemo(() => {
    if (projectTasks.length === 0) return 0
    const completed = projectTasks.filter((task) => task.status === TaskStatus.DONE).length
    return Math.round((completed / projectTasks.length) * 100)
  }, [projectTasks])

  const warnings = useMemo(() => {
    const pending = projectTasks.filter((task) => task.status !== TaskStatus.DONE).length
    const noCommits = projectTasks.filter((task) => task.commits.length === 0).length
    const warningList: string[] = []
    if (pending >= 3) warningList.push('Too many pending tasks in flight')
    if (noCommits >= 2) warningList.push('No recent activity detected on 2+ tasks')
    if (projectTasks.length > 0) warningList.push('Risk of scope creep in final demo sprint')
    return warningList
  }, [projectTasks])

  const contributors = useMemo(
    () => [
      { name: 'Ari', tasks: 4, velocity: 'Pacing +12%' },
      { name: 'Leo', tasks: 3, velocity: 'Code reviews complete' },
      { name: 'Mina', tasks: 2, velocity: 'Design system locked' },
    ],
    []
  )

  const handleGenerateTasks = () => {
    if (!projectId) return
    setLoadingTasks(true)
    setTimeout(() => {
      const newTask: Task = {
        id: `task-${Date.now()}`,
        title: 'Presentation summary generator',
        description: 'Auto-create final demo narrative in 90 seconds.',
        priority: TaskPriority.MEDIUM,
        status: TaskStatus.TODO,
        assignee: 'Nova',
        commits: [],
      }
      updateTasks(projectId, [newTask, ...projectTasks])
      setLoadingTasks(false)
    }, 900)
  }

  const handleMoveTask = (id: string, status: TaskStatus) => {
    if (!projectId) return
    updateTasks(
      projectId,
      projectTasks.map((task) => (task.id === id ? { ...task, status } : task))
    )
  }

  const handleSaveTasks = () => {
    alert('Tasks saved locally. Ready for backend integration later.')
  }

  const handleInvite = (email: string) => {
    if (!projectId) return
    addMember(projectId, {
      id: `m${Date.now()}`,
      name: email.split('@')[0],
      role: 'Invited',
    })
  }

  if (!projectId) {
    return (
      <Card className="border border-dashed border-white/20 bg-white/5">
        <CardContent className="p-6 text-center text-sm text-white/60">
          Select a project to view its workspace.
        </CardContent>
      </Card>
    )
  }

  if (!project) {
    return (
      <Card className="border border-dashed border-white/20 bg-white/5">
        <CardHeader>
          <CardTitle>Project not found</CardTitle>
          <CardDescription>Return to your projects list to pick another workspace.</CardDescription>
        </CardHeader>
        <CardContent className="text-center">
          <Link className="text-sm text-acid" to="/projects">
            Back to projects
          </Link>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-white/40">Project Workspace</p>
          <h2 className="text-2xl font-semibold text-white">{project.name}</h2>
          <p className="text-sm text-white/60">{project.description}</p>
        </div>
        <ProjectTabs activeTab={activeTab} onChange={setActiveTab} />
      </div>

      {loadingTasks && (
        <div className="grid gap-4 md:grid-cols-2">
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
        </div>
      )}

      {activeTab === 'dashboard' && (
        <DashboardTab
          project={project}
          tasks={projectTasks}
          problem={problem}
          loading={loadingTasks}
          progress={progress}
          contributors={contributors}
          onProblemChange={(value) => setProblem(projectId, value)}
          onGenerateTasks={handleGenerateTasks}
          onUpdateTasks={(next) => updateTasks(projectId, next)}
          onSaveTasks={handleSaveTasks}
        />
      )}

      {activeTab === 'kanban' && (
        <KanbanTab tasks={projectTasks} onMove={handleMoveTask} warnings={warnings} />
      )}

      {activeTab === 'team' && <TeamTab members={members} onInvite={handleInvite} />}
    </div>
  )
}

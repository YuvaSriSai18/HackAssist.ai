import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { DashboardTab } from '../components/DashboardTab'
import { KanbanTab } from '../components/KanbanTab'
import { ProjectTabs } from '../components/ProjectTabs'
import { TeamTab } from '../components/TeamTab'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import { Skeleton } from '../components/ui/skeleton'
import { finalizeTasks, generateTasks } from '../apis'
import { useAppState } from '../context/AppState'
import type { Project, ProjectPlan, Task } from '../models/types'
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
    token,
  } = useAppState()
  const { projectId } = useParams()
  const [activeTab, setActiveTab] = useState<ProjectTabKey>('dashboard')
  const [loadingTasks, setLoadingTasks] = useState(false)
  const [plan, setPlan] = useState<ProjectPlan | null>(null)
  const [planSaving, setPlanSaving] = useState(false)
  const [planError, setPlanError] = useState<string | null>(null)

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
    if (!project || !token) {
      console.error('[ProjectWorkspacePage] Missing project or token')
      return
    }
    const resolvedProjectId = project.projectId ?? project.id
    console.log('[ProjectWorkspacePage] Starting task generation for project:', resolvedProjectId)
    console.log('[ProjectWorkspacePage] Problem statement:', problem)
    setLoadingTasks(true)
    setPlanError(null)
    generateTasks(token, { projectId: resolvedProjectId, problemStatement: problem })
      .then((response) => {
        console.log('[ProjectWorkspacePage] Generate tasks successful, mapping response...')
        const nextPlan: ProjectPlan = {
          projectId: response.projectId ?? resolvedProjectId,
          problemStatement: response.problemStatement ?? problem,
          techStack: {
            backend: response.techStack?.backend ?? 'Spring Boot (Java)',
            frontend: response.techStack?.frontend ?? 'React + Tailwind + shadcn',
            database: response.techStack?.database ?? 'MySQL/PostgreSQL',
            architecture: response.techStack?.architecture ?? 'REST APIs',
          },
          features:
            response.features?.map((feature, idx) => ({
              key: feature.key ?? `F-${String(idx + 1).padStart(3, '0')}`,
              name: feature.name ?? 'Feature',
              description: feature.description ?? '',
              priority: (feature.priority as TaskPriority) ?? TaskPriority.MEDIUM,
            })) ?? [],
          modules:
            response.modules?.map((module, idx) => ({
              key: module.key ?? `M-${String(idx + 1).padStart(3, '0')}`,
              name: module.name ?? 'Module',
              description: module.description ?? '',
            })) ?? [],
          tasks:
            response.tasks?.map((task, idx) => ({
              externalId: task.externalId ?? `TSK-${String(idx + 1).padStart(3, '0')}`,
              title: task.title ?? 'Task',
              description: task.description ?? '',
              priority: (task.priority as TaskPriority) ?? TaskPriority.MEDIUM,
              status: (task.status as TaskStatus) ?? TaskStatus.TODO,
              estimatedHours: task.estimatedHours ?? undefined,
              moduleKey: task.moduleKey ?? undefined,
              dependsOn: task.dependsOn ?? [],
            })) ?? [],
          risks:
            response.risks?.map((risk) => ({
              title: risk.title ?? 'Risk',
              impact: risk.impact ?? '',
              mitigation: risk.mitigation ?? '',
            })) ?? [],
        }
        console.log('[ProjectWorkspacePage] Setting plan state with', nextPlan.tasks.length, 'tasks')
        setPlan(nextPlan)
      })
      .catch((err) => {
        console.error('[ProjectWorkspacePage] AI plan generation failed', err)
        setPlanError(err.message || 'Unable to generate tasks. Please try again.')
      })
      .finally(() => {
        setLoadingTasks(false)
      })
  }

  const handleFinalizePlan = async () => {
    if (!project || !token || !plan) return
    const resolvedProjectId = project.projectId ?? project.id
    setPlanSaving(true)
    setPlanError(null)
    try {
      await finalizeTasks(token, resolvedProjectId, plan)
      const mappedTasks: Task[] = plan.tasks.map((task) => ({
        id: task.externalId,
        title: task.title,
        description: task.description,
        priority: task.priority,
        status: task.status,
        assignee: 'Unassigned',
        commits: [],
      }))
      updateTasks(resolvedProjectId, mappedTasks)
      setPlan(null)
    } catch (err) {
      console.error('[ProjectWorkspacePage] Failed to finalize plan', err)
      setPlanError('Unable to finalize tasks. Please try again.')
    } finally {
      setPlanSaving(false)
    }
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
          plan={plan}
          planSaving={planSaving}
          planError={planError}
          progress={progress}
          contributors={contributors}
          onProblemChange={(value) => setProblem(projectId, value)}
          onGenerateTasks={handleGenerateTasks}
          onPlanChange={setPlan}
          onFinalizePlan={handleFinalizePlan}
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

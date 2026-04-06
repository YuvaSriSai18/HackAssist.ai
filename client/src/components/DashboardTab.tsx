import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'
import { DashboardLayout } from './DashboardLayout'
import { InsightsPanel } from './InsightsPanel'
import { ProblemInput } from './ProblemInput'
import { ProjectRepoConnect } from './ProjectRepoConnect'
import { TaskEditor } from './TaskEditor'
import type { Project, Task } from '../models/types'
import { TaskStatus } from '../models/types'
import { useAppState } from '../context/AppState'

type DashboardTabProps = {
  project: Project
  tasks: Task[]
  problem: string
  loading: boolean
  progress: number
  contributors: { name: string; tasks: number; velocity: string }[]
  onProblemChange: (value: string) => void
  onGenerateTasks: () => void
  onUpdateTasks: (tasks: Task[]) => void
  onSaveTasks: () => void
}

export function DashboardTab({
  project,
  tasks,
  problem,
  loading,
  progress,
  contributors,
  onProblemChange,
  onGenerateTasks,
  onUpdateTasks,
  onSaveTasks,
}: DashboardTabProps) {
  const { linkProjectRepo } = useAppState()
  const hasTasks = tasks.length > 0

  return (
    <DashboardLayout
      left={
        <div className="space-y-6">
          <Card className="border border-white/10 bg-white/5">
            <CardHeader>
              <CardTitle>{project.name}</CardTitle>
              <CardDescription>{project.description}</CardDescription>
            </CardHeader>
          </Card>
          {!hasTasks && (
            <ProblemInput
              problem={problem}
              onChange={onProblemChange}
              onGenerate={onGenerateTasks}
              loading={loading}
            />
          )}
          {hasTasks && (
            <Card className="border border-white/10 bg-white/5">
              <CardHeader>
                <CardTitle>Problem Summary</CardTitle>
                <CardDescription>Locked once tasks are generated.</CardDescription>
              </CardHeader>
              <CardContent className="text-sm text-white/70">{problem}</CardContent>
            </Card>
          )}
          {hasTasks ? (
            <TaskEditor tasks={tasks} onUpdate={onUpdateTasks} onSave={onSaveTasks} />
          ) : (
            <Card className="border border-dashed border-white/20 bg-white/5">
              <CardContent className="p-6 text-center text-sm text-white/60">
                No tasks yet. Generate tasks to unlock editing and insights.
              </CardContent>
            </Card>
          )}
        </div>
      }
      right={
        hasTasks ? (
          <div className="space-y-6">
            <ProjectRepoConnect
              project={project}
              onLink={async (projectId, repo) => {
                await linkProjectRepo(projectId, repo)
              }}
            />
            <InsightsPanel
              progress={progress}
              total={tasks.length}
              completed={tasks.filter((task) => task.status === TaskStatus.DONE).length}
              contributors={contributors}
            />
          </div>
        ) : (
          <div className="space-y-6">
            <ProjectRepoConnect
              project={project}
              onLink={async (projectId, repo) => {
                await linkProjectRepo(projectId, repo)
              }}
            />
            <Card className="border border-dashed border-white/20 bg-white/5">
              <CardHeader>
                <CardTitle>Insights</CardTitle>
                <CardDescription>Generate tasks to unlock insights.</CardDescription>
              </CardHeader>
              <CardContent className="text-sm text-white/60">
                Progress, velocity, and contributor metrics appear after your first task set.
              </CardContent>
            </Card>
          </div>
        )
      }
    />
  )
}

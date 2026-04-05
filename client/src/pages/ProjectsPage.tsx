import { useNavigate } from 'react-router-dom'
import { CreateProjectModal } from '../components/CreateProjectModal'
import { ProjectList } from '../components/ProjectList'
import { useAppState } from '../context/AppState'
import type { Project } from '../models/types'

export function ProjectsPage() {
  const { projects, sharedProjects, createProject, selectProject } = useAppState()
  const navigate = useNavigate()

  const openProject = (projectId: string) => {
    selectProject(projectId)
    navigate(`/project/${projectId}`)
  }

  const handleCreate = (project: Project) => {
    createProject(project)
    navigate(`/project/${project.id}`)
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-white">Projects</h2>
          <p className="text-sm text-white/50">Manage your workspaces and shared hackathons.</p>
        </div>
        <CreateProjectModal onCreate={handleCreate} />
      </div>
      <ProjectList
        title="My Projects"
        projects={projects}
        emptyMessage="No projects yet. Create one to get started."
        onOpen={openProject}
      />
      <ProjectList
        title="Shared With Me"
        projects={sharedProjects}
        emptyMessage="No shared workspaces yet."
        onOpen={openProject}
      />
    </div>
  )
}

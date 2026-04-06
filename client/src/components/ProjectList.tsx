import type { Project } from '../models/types'
import { ProjectCard } from './ProjectCard'

type ProjectListProps = {
  title: string
  projects: Project[]
  emptyMessage: string
  onOpen: (id: string) => void
  onEdit: (id: string, payload: { name: string; description: string }) => Promise<void>
  onDelete: (id: string) => Promise<void>
}

export function ProjectList({ title, projects, emptyMessage, onOpen, onEdit, onDelete }: ProjectListProps) {
  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-white">{title}</h2>
        <span className="text-sm text-white/50">{projects.length} projects</span>
      </div>
      {projects.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-white/20 p-6 text-center text-sm text-white/60">
          {emptyMessage}
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {projects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onOpen={onOpen}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </section>
  )
}

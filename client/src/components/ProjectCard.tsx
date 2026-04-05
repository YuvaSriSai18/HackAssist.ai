import { Users } from 'lucide-react'
import type { Project } from '../models/types'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'

type ProjectCardProps = {
  project: Project
  onOpen: (id: string) => void
}

export function ProjectCard({ project, onOpen }: ProjectCardProps) {
  return (
    <Card className="transition hover:border-white/30">
      <CardHeader>
        <CardTitle>{project.name}</CardTitle>
        <CardDescription>{project.description}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="flex items-center justify-between text-sm text-white/60">
          <span>Owner</span>
          <span className="text-white">{project.owner}</span>
        </div>
        <div className="flex items-center justify-between text-sm text-white/60">
          <span className="flex items-center gap-2">
            <Users size={14} /> Members
          </span>
          <span className="text-white">{project.members.length}</span>
        </div>
        <Button variant="accent" size="sm" onClick={() => onOpen(project.id)}>
          Open Project
        </Button>
      </CardContent>
    </Card>
  )
}

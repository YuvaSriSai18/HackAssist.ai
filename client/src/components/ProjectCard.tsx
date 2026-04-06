import { useEffect, useState } from 'react'
import { Pencil, Trash2, Users } from 'lucide-react'
import type { Project } from '../models/types'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'

type ProjectCardProps = {
  project: Project
  onOpen: (id: string) => void
  onEdit: (id: string, payload: { name: string; description: string }) => Promise<void>
  onDelete: (id: string) => Promise<void>
}

export function ProjectCard({ project, onOpen, onEdit, onDelete }: ProjectCardProps) {
  const [name, setName] = useState(project.name)
  const [description, setDescription] = useState(project.description)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    setName(project.name)
    setDescription(project.description)
  }, [project.name, project.description])

  const handleEdit = async () => {
    if (!name.trim()) return
    setIsSubmitting(true)
    try {
      await onEdit(project.id, { name, description })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Delete this project and all related data?')) return
    await onDelete(project.id)
  }

  return (
    <Card className="transition hover:border-white/30">
      <CardHeader>
        <CardTitle>{project.name}</CardTitle>
        <CardDescription>{project.description}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="flex border-white items-center justify-between text-sm text-white/60">
          <span>Owner</span>
          <span className="text-white truncate">{` ${project.owner.split(" ")[0].toUpperCase()}`}</span>
        </div>
        <div className="flex items-center justify-between text-sm text-white/60">
          <span className="flex items-center gap-2">
            <Users size={14} /> Members
          </span>
          <span className="text-white">{project.members.length}</span>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Button variant="accent" size="sm" onClick={() => onOpen(project.id)}>
            Open Project
          </Button>
          <Dialog>
            <DialogTrigger asChild>
              <Button variant="outline" size="sm">
                <Pencil size={14} />
                Edit
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Edit project</DialogTitle>
                <DialogDescription>Update the project title and description.</DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <Input value={name} onChange={(event) => setName(event.target.value)} />
                <Textarea value={description} onChange={(event) => setDescription(event.target.value)} />
                <Button variant="accent" onClick={handleEdit} disabled={isSubmitting}>
                  {isSubmitting ? 'Saving...' : 'Save Changes'}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
          <Button variant="ghost" size="sm" onClick={handleDelete}>
            <Trash2 size={14} />
            Delete
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}

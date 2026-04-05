import { useState } from 'react'
import { FolderPlus } from 'lucide-react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'
import { Button } from './ui/button'
import type { Project } from '../models/types'

type CreateProjectModalProps = {
  onCreate: (project: Project) => void
}

export function CreateProjectModal({ onCreate }: CreateProjectModalProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  const handleCreate = () => {
    if (!name.trim()) return
    const project: Project = {
      id: `proj-${Date.now()}`,
      name,
      description,
      owner: 'Demo Lead',
      members: ['Demo Lead'],
      createdAt: new Date().toISOString().split('T')[0],
    }
    onCreate(project)
    setName('')
    setDescription('')
  }

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="accent">
          <FolderPlus size={16} />
          Create Project
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create a new project</DialogTitle>
          <DialogDescription>Start a fresh workspace for your hackathon.</DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <Input placeholder="Project name" value={name} onChange={(e) => setName(e.target.value)} />
          <Textarea
            placeholder="Short description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <Button variant="accent" onClick={handleCreate}>
            Create Project
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}

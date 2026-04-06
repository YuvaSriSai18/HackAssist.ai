import { useState } from 'react'
import { FolderPlus } from 'lucide-react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'
import { Button } from './ui/button'
import type { Project } from '../models/types'

type CreateProjectModalProps = {
  onCreate: (payload: { name: string; description: string }) => Promise<Project>
}

export function CreateProjectModal({ onCreate }: CreateProjectModalProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleCreate = async () => {
    if (!name.trim()) return
    setIsSubmitting(true)
    try {
      await onCreate({ name, description })
      setError(null)
      setName('')
      setDescription('')
    } catch (err) {
      console.error('[CreateProjectModal] Failed to create project', err)
      setError('Unable to create project. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
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
          {error ? <p className="text-xs text-red-300">{error}</p> : null}
          <Button variant="accent" onClick={handleCreate} disabled={isSubmitting}>
            {isSubmitting ? 'Creating...' : 'Create Project'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}

import { useState } from 'react'
import { MailPlus, UserPlus } from 'lucide-react'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from './ui/dialog'
import { Input } from './ui/input'
import type { Member } from '../models/types'

type TeamPanelProps = {
  members: Member[]
  onInvite: (email: string) => void
}

export function TeamPanel({ members, onInvite }: TeamPanelProps) {
  const [email, setEmail] = useState('')

  const handleInvite = () => {
    if (!email.trim()) return
    onInvite(email)
    setEmail('')
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Team Panel</CardTitle>
        <CardDescription>Assign tasks and onboard new contributors.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-3">
          {members.length === 0 ? (
            <div className="rounded-xl border border-dashed border-white/20 p-3 text-sm text-white/60">
              No team members yet. Invite someone to collaborate.
            </div>
          ) : (
            members.map((member) => (
              <div key={member.id} className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-white">{member.name}</p>
                  <p className="text-xs text-white/50">{member.role}</p>
                </div>
                <span className="text-xs text-white/40">Active</span>
              </div>
            ))
          )}
        </div>

        <Dialog>
          <DialogTrigger asChild>
            <Button variant="outline">
              <UserPlus size={16} />
              Invite Member
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Invite a contributor</DialogTitle>
              <DialogDescription>Send an invite to join the hackathon workspace.</DialogDescription>
            </DialogHeader>
            <div className="space-y-3">
              <Input
                placeholder="name@email.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
              <Button variant="accent" onClick={handleInvite}>
                <MailPlus size={16} />
                Send Invite
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  )
}

import type { Member } from '../models/types'
import { TeamPanel } from './TeamPanel'

type TeamTabProps = {
  members: Member[]
  onInvite: (email: string) => void
}

export function TeamTab({ members, onInvite }: TeamTabProps) {
  return <TeamPanel members={members} onInvite={onInvite} />
}

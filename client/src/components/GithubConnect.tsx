import { Github } from 'lucide-react'
import { Button } from './ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'

type GithubConnectProps = {
  connected: boolean
  onConnect: () => void
}

export function GithubConnect({ connected, onConnect }: GithubConnectProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>GitHub Command Link</CardTitle>
        <CardDescription>Mock connection to simulate GitHub access.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Button variant={connected ? 'outline' : 'accent'} onClick={onConnect}>
          <Github size={18} />
          {connected ? 'GitHub Connected' : 'Connect GitHub'}
        </Button>
        {connected && <p className="text-xs text-white/50">Connection active. Repos are hidden for now.</p>}
      </CardContent>
    </Card>
  )
}

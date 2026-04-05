import { Github } from 'lucide-react'
import { Button } from './ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'

type GithubConnectButtonProps = {
  connected: boolean
  onConnect: () => void
}

export function GithubConnectButton({ connected, onConnect }: GithubConnectButtonProps) {
  return (
    <Card className="border border-white/10 bg-white/5">
      <CardHeader>
        <CardTitle>GitHub</CardTitle>
        <CardDescription>Connect to unlock repository integrations later.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <Button
          variant={connected ? 'outline' : 'accent'}
          onClick={connected ? undefined : onConnect}
          disabled={connected}
        >
          <Github size={18} />
          {connected ? 'GitHub Connected' : 'Connect GitHub'}
        </Button>
        <p className="text-xs text-white/50">No repositories are shown yet.</p>
      </CardContent>
    </Card>
  )
}

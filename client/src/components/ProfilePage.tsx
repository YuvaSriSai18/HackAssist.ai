import { redirectToGithubAuth } from '../apis'
import { GithubConnectButton } from './GithubConnectButton'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'
import { useAppState } from '../context/AppState'

export function ProfilePage() {
  const { user, github } = useAppState()

  const handleConnect = () => {
    window.localStorage.setItem('oauthIntent', 'github-connect')
    redirectToGithubAuth()
  }

  if (!user) {
    return null
  }

  return (
    <div className="space-y-6">
      <Card className="border border-white/10 bg-white/5">
        <CardHeader>
          <CardTitle>Profile</CardTitle>
          <CardDescription>Manage your hackathon identity and integrations.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-white/70">
          <p>
            <span className="text-white/50">Name:</span> {user.name}
          </p>
          <p>
            <span className="text-white/50">Email:</span> {user.email}
          </p>
        </CardContent>
      </Card>

      <GithubConnectButton connected={github.connected} onConnect={handleConnect} />
    </div>
  )
}

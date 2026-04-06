import { useEffect, useState } from 'react'
import { Github } from 'lucide-react'
import { fetchGithubUser, redirectToGithubConnect } from '../apis'
import { useAppState } from '../context/AppState'
import { Button } from './ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'

type GithubProfile = {
  connected?: boolean
  githubId?: string
  githubUsername?: string
  name?: string
  avatarUrl?: string
  githubVerified?: boolean
}

export function GithubConnectButton() {
  const { github, setGithubConnected, token } = useAppState()
  const [profile, setProfile] = useState<GithubProfile | null>(null)

  useEffect(() => {
    if (!token) return
    fetchGithubUser(token)
      .then((response) => {
        const connected = Boolean(response.connected ?? response.githubVerified)
        setGithubConnected(connected, response.githubVerified)
        setProfile(response)
      })
      .catch(() => {
        setGithubConnected(false, false)
        setProfile(null)
      })
  }, [setGithubConnected, token])

  const handleConnect = () => {
    window.localStorage.setItem('oauthIntent', 'github-connect')
    redirectToGithubConnect()
  }

  const connected = github.connected
  const verified = Boolean(profile?.githubVerified)
  return (
    <Card className="border border-white/10 bg-white/5">
      <CardHeader>
        <CardTitle>GitHub</CardTitle>
        <CardDescription>Connect to unlock repository integrations later.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        <Button
          variant={connected ? 'outline' : 'accent'}
          onClick={connected ? undefined : handleConnect}
          disabled={connected}
        >
          <Github size={18} />
          {connected ? 'GitHub Connected' : 'Connect GitHub'}
        </Button>
        {connected && profile ? (
          <div className="space-y-2 text-xs text-white/70">
            <div className="flex items-center gap-3">
              {profile.avatarUrl ? (
                <img
                  src={profile.avatarUrl}
                  alt="GitHub avatar"
                  className="h-8 w-8 rounded-full border border-white/20"
                />
              ) : null}
              <div>
                <div className="font-medium text-white/90">
                  {profile.name || profile.githubUsername || 'GitHub User'}
                </div>
                {profile.githubUsername ? <div>@{profile.githubUsername}</div> : null}
              </div>
            </div>
            <div>GitHub ID: {profile.githubId ?? 'unknown'}</div>
            <div>Verified: {verified ? 'Yes' : 'No'}</div>
          </div>
        ) : (
          <p className="text-xs text-white/50">No repositories are shown yet.</p>
        )}
      </CardContent>
    </Card>
  )
}

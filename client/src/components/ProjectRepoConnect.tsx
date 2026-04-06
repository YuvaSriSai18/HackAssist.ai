import { useEffect, useMemo, useState } from 'react'
import { Github } from 'lucide-react'
import { fetchGithubRepos } from '../apis'
import { useAppState } from '../context/AppState'
import type { Project } from '../models/types'
import { Button } from './ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'

const EMPTY_REPO_NOTICE = 'No repositories available. Connect GitHub to see your repos.'

type RepoItem = {
  fullName: string
  htmlUrl: string
  isPrivate: boolean
}

type ProjectRepoConnectProps = {
  project: Project
  onLink: (projectId: string, repo: { repoUrl?: string; repoFullName?: string }) => Promise<void>
}

export function ProjectRepoConnect({ project, onLink }: ProjectRepoConnectProps) {
  const { token } = useAppState()
  const [repos, setRepos] = useState<RepoItem[]>([])
  const [selectedRepo, setSelectedRepo] = useState<RepoItem | null>(null)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState(false)

  const linkedRepoUrl = project.githubRepoUrl ?? ''

  useEffect(() => {
    if (!token) return
    setLoading(true)
    setError(null)
    fetchGithubRepos(token)
      .then((data) => {
        const mapped = data
          .map((repo) => ({
            fullName: repo.full_name ?? '',
            htmlUrl: repo.html_url ?? '',
            isPrivate: Boolean(repo.private),
          }))
          .filter((repo) => repo.fullName && repo.htmlUrl)
          .sort((a, b) => a.fullName.localeCompare(b.fullName))
        setRepos(mapped)
        if (linkedRepoUrl) {
          const linked = mapped.find((item) => item.htmlUrl === linkedRepoUrl)
          setSelectedRepo(linked ?? null)
        }
      })
      .catch((err) => {
        console.error('[ProjectRepoConnect] Failed to load repos', err)
        setError('Unable to load GitHub repositories.')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [linkedRepoUrl, token])

  const hasRepos = repos.length > 0
  const canSave = Boolean(selectedRepo) && !saving && (isEditing || !linkedRepoUrl)

  const repoLabel = useMemo(() => {
    if (!selectedRepo) return 'Select a repository'
    return selectedRepo.fullName
  }, [selectedRepo])

  const handleSave = async () => {
    if (!selectedRepo) return
    setSaving(true)
    setError(null)
    try {
      await onLink(project.id, {
        repoUrl: selectedRepo.htmlUrl,
        repoFullName: selectedRepo.fullName,
      })
      setIsEditing(false)
    } catch (err) {
      console.error('[ProjectRepoConnect] Failed to link repo', err)
      setError('Unable to link repository. Try again.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <Card className="border border-white/10 bg-white/5">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Github size={18} /> Link GitHub Repo
        </CardTitle>
        <CardDescription>Select one of your GitHub repositories to connect.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {loading ? (
          <p className="text-sm text-white/60">Loading repositories...</p>
        ) : hasRepos ? (
          linkedRepoUrl && !isEditing ? (
            <div className="space-y-2">
              <p className="text-sm text-white/70">Linked: {linkedRepoUrl}</p>
              <Button variant="ghost" size="sm" onClick={() => setIsEditing(true)}>
                Change repository
              </Button>
            </div>
          ) : (
            <div className="space-y-2">
              <label className="text-xs uppercase tracking-[0.2em] text-white/40" htmlFor="repo-select">
                Repository
              </label>
              <div className="relative">
                <select
                  id="repo-select"
                  className="w-full rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm text-white/80"
                  value={selectedRepo?.htmlUrl ?? ''}
                  onChange={(event) => {
                    const next = repos.find((repo) => repo.htmlUrl === event.target.value) ?? null
                    setSelectedRepo(next)
                  }}
                >
                  <option value="" className="text-ink">
                    Select a repository
                  </option>
                  {repos.map((repo) => (
                    <option key={repo.htmlUrl} value={repo.htmlUrl} className="text-ink">
                      {repo.fullName} ({repo.isPrivate ? 'Private' : 'Public'})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )
        ) : (
          <p className="text-sm text-white/60">{EMPTY_REPO_NOTICE}</p>
        )}

        {error ? <p className="text-xs text-red-300">{error}</p> : null}

        <div className="flex items-center justify-between">
          <span className="text-xs text-white/50">{repoLabel}</span>
          {linkedRepoUrl && !isEditing ? null : (
            <Button variant="accent" size="sm" disabled={!canSave} onClick={handleSave}>
              {saving ? 'Saving...' : 'Save Repo'}
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

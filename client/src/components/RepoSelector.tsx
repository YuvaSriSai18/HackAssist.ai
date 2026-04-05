import { Check } from 'lucide-react'
import { Button } from './ui/button'

type RepoSelectorProps = {
  repos: string[]
  selectedRepo: string | null
  onSelectRepo: (repo: string) => void
}

export function RepoSelector({ repos, selectedRepo, onSelectRepo }: RepoSelectorProps) {
  return (
    <div className="grid gap-2">
      {repos.map((repo) => (
        <button
          key={repo}
          type="button"
          onClick={() => onSelectRepo(repo)}
          className={`flex items-center justify-between rounded-xl border px-4 py-3 text-sm transition ${
            selectedRepo === repo
              ? 'border-acid/80 bg-acid/10 text-acid'
              : 'border-white/10 bg-white/5 text-white/70 hover:border-white/30'
          }`}
        >
          <span>{repo}</span>
          {selectedRepo === repo && <Check size={16} />}
        </button>
      ))}
      <Button variant="ghost" size="sm">
        + Add another repo
      </Button>
    </div>
  )
}

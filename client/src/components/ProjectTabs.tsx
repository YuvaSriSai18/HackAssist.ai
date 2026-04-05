type ProjectTab = 'dashboard' | 'kanban' | 'team'

type ProjectTabsProps = {
  activeTab: ProjectTab
  onChange: (tab: ProjectTab) => void
}

const tabs: { key: ProjectTab; label: string }[] = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'kanban', label: 'Kanban' },
  { key: 'team', label: 'Team' },
]

export function ProjectTabs({ activeTab, onChange }: ProjectTabsProps) {
  return (
    <div className="flex flex-wrap items-center gap-2 rounded-full border border-white/10 bg-white/5 p-1 text-sm">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          className={`rounded-full px-4 py-2 transition ${
            activeTab === tab.key ? 'bg-white text-ink' : 'text-white/70 hover:text-white'
          }`}
          onClick={() => onChange(tab.key)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}

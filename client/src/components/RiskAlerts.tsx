import { AlertTriangle, ShieldAlert } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from './ui/card'

type RiskAlertsProps = {
  warnings: string[]
}

export function RiskAlerts({ warnings }: RiskAlertsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Risk Alerts</CardTitle>
        <CardDescription>Watchouts from the AI auditor.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {warnings.length === 0 && (
          <div className="rounded-xl border border-dashed border-white/20 p-3 text-sm text-white/60">
            No active risk alerts right now.
          </div>
        )}
        {warnings.map((warning) => (
          <div
            key={warning}
            className="flex items-center gap-3 rounded-xl border border-ember/40 bg-ember/10 p-3 text-sm text-white"
          >
            <AlertTriangle size={18} className="text-ember" />
            <span>{warning}</span>
          </div>
        ))}
        <div className="flex items-center gap-2 text-xs text-white/50">
          <ShieldAlert size={14} />
          AI guardrails are active for scope creep detection.
        </div>
      </CardContent>
    </Card>
  )
}

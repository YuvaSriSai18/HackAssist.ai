import * as React from 'react'
import { cn } from '../../lib/utils'

type BadgeProps = React.HTMLAttributes<HTMLSpanElement> & {
  variant?: 'high' | 'medium' | 'low'
}

const badgeStyles: Record<NonNullable<BadgeProps['variant']>, string> = {
  high: 'border-red-400/40 bg-red-500/15 text-red-300',
  medium: 'border-yellow-300/40 bg-yellow-400/15 text-yellow-200',
  low: 'border-green-400/40 bg-green-500/15 text-green-300',
}

function Badge({ className, variant = 'medium', ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide',
        badgeStyles[variant],
        className
      )}
      {...props}
    />
  )
}

export { Badge }

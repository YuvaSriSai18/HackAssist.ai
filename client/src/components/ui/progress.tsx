import * as React from 'react'
import { cn } from '../../lib/utils'

type ProgressProps = React.HTMLAttributes<HTMLDivElement> & {
  value: number
}

function Progress({ value, className, ...props }: ProgressProps) {
  return (
    <div
      className={cn('h-2 w-full overflow-hidden rounded-full bg-white/10', className)}
      {...props}
    >
      <div
        className="h-full rounded-full bg-gradient-to-r from-ember to-acid"
        style={{ width: `${Math.min(100, Math.max(0, value))}%` }}
      />
    </div>
  )
}

export { Progress }

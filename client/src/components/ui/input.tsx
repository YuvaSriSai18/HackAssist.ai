import * as React from 'react'
import { cn } from '../../lib/utils'

const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        'h-10 w-full rounded-lg border border-white/10 bg-white/5 px-3 text-sm text-white',
        'placeholder:text-white/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-acid/60',
        className
      )}
      {...props}
    />
  )
)

Input.displayName = 'Input'

export { Input }

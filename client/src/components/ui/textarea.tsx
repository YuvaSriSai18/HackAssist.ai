import * as React from 'react'
import { cn } from '../../lib/utils'

const Textarea = React.forwardRef<HTMLTextAreaElement, React.TextareaHTMLAttributes<HTMLTextAreaElement>>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      className={cn(
        'min-h-[120px] w-full rounded-xl border border-white/10 bg-white/5 p-3 text-sm text-white',
        'placeholder:text-white/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-acid/60',
        className
      )}
      {...props}
    />
  )
)

Textarea.displayName = 'Textarea'

export { Textarea }

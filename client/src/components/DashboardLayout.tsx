import type React from 'react'

type DashboardLayoutProps = {
  left: React.ReactNode
  right: React.ReactNode
}

export function DashboardLayout({ left, right }: DashboardLayoutProps) {
  return (
    <div className="grid grid-cols-12 gap-6">
      <section className="col-span-12 space-y-6 xl:col-span-8">{left}</section>
      <aside className="col-span-12 space-y-6 xl:col-span-4">{right}</aside>
    </div>
  )
}

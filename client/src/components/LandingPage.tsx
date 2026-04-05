import { useNavigate } from 'react-router-dom'
import { Navbar } from './Navbar'
import { Button } from './ui/button'

export function LandingPage() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-night text-white">
      <Navbar />
      <div className="mx-auto flex min-h-[calc(100vh-96px)] max-w-5xl flex-col items-center justify-center px-6 py-8 text-center">
        <p className="text-xs uppercase tracking-[0.5em] text-white/40">HackAssist</p>
        <h1 className="mt-4 text-4xl font-semibold text-white md:text-5xl">AI-powered hackathon productivity system</h1>
        <p className="mt-4 max-w-2xl text-sm text-white/60 md:text-base">
          Orchestrate problem framing, task generation, and team execution in one focused workspace.
        </p>
        <Button variant="accent" className="mt-8 px-6" onClick={() => navigate('/login')}>
          Get Started
        </Button>
      </div>
    </div>
  )
}

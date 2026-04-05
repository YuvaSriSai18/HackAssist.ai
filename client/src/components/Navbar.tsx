import { NavLink, useNavigate } from 'react-router-dom'
import { LogOut, Sparkles } from 'lucide-react'
import { logoutRequest } from '../apis'
import { Button } from './ui/button'
import { useAppState } from '../context/AppState'

const tabClassName = ({ isActive }: { isActive: boolean }) =>
  `rounded-full px-4 py-2 transition ${
    isActive ? 'bg-white text-ink' : 'text-white/70 hover:text-white'
  }`

export function Navbar() {
  const { isAuthenticated, logout, token } = useAppState()
  const navigate = useNavigate()

  const handleLogout = () => {
    void logoutRequest(token ?? undefined).finally(() => {
      logout()
      navigate('/', { replace: true })
    })
  }

  return (
    <header className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 px-6 py-5">
      <div className="flex items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 text-acid">
          <Sparkles size={22} />
        </div>
        <div>
          <p className="text-sm uppercase tracking-[0.4em] text-white/40">HackAssist</p>
          <h1 className="text-2xl font-semibold text-white">AI Hackathon Command</h1>
        </div>
      </div>
      {isAuthenticated ? (
        <>
          <div className="flex flex-wrap items-center gap-2 rounded-full border border-white/10 bg-white/5 p-1 text-sm">
            <NavLink to="/projects" className={tabClassName}>
              Projects
            </NavLink>
            <NavLink to="/profile" className={tabClassName}>
              Profile
            </NavLink>
          </div>
          <div className="flex items-center gap-3">
            <Button
              variant="outline"
              onClick={handleLogout}
            >
              <LogOut size={18} />
              Logout
            </Button>
          </div>
        </>
      ) : (
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={() => navigate('/login')}>
            Login
          </Button>
        </div>
      )}
    </header>
  )
}

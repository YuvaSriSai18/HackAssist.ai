import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { Navbar } from './components/Navbar'
import { useAppState } from './context/AppState'
import { LandingPage } from './components/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { ProjectsPage } from './pages/ProjectsPage'
import { ProjectWorkspacePage } from './pages/ProjectWorkspacePage'
import { ProfilePage } from './components/ProfilePage'

function ProtectedLayout() {
  const { isAuthenticated } = useAppState()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="min-h-screen bg-night text-white">
      <Navbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  )
}

export default function App() {
  const { isAuthenticated } = useAppState()

  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/projects" replace /> : <LoginPage />}
      />
      <Route element={<ProtectedLayout />}>
        <Route path="/projects" element={<ProjectsPage />} />
        <Route path="/project/:projectId" element={<ProjectWorkspacePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="*" element={<Navigate to="/projects" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

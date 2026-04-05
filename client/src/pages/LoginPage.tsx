import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LoginForm } from '../components/Auth/LoginForm'
import { Navbar } from '../components/Navbar'
import { useAppState } from '../context/AppState'

export function LoginPage() {
  const { login } = useAppState()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const handleLogin = (email: string, password: string) => {
    setLoading(true)
    setTimeout(() => {
      login(email, password)
      setLoading(false)
      navigate('/projects', { replace: true })
    }, 500)
  }

  const handleGoogleLogin = () => {
    setLoading(true)
    setTimeout(() => {
      login('demo@google.com', 'oauth')
      setLoading(false)
      navigate('/projects', { replace: true })
    }, 400)
  }

  return (
    <div className="min-h-screen bg-night text-white">
      <Navbar />
      <div className="mx-auto flex min-h-[calc(100vh-96px)] max-w-6xl items-center justify-center px-6 py-8">
        <LoginForm onSubmit={handleLogin} onGoogle={handleGoogleLogin} loading={loading} />
      </div>
    </div>
  )
}

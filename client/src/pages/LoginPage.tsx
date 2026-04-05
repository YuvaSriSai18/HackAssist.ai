import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { loginWithDemo, redirectToGoogleAuth } from '../apis'
import { LoginForm } from '../components/Auth/LoginForm'
import { Navbar } from '../components/Navbar'
import { useAppState } from '../context/AppState'

export function LoginPage() {
  const { setSession } = useAppState()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const handleLogin = (email: string, password: string) => {
    console.log('[LoginPage] Demo login attempt for:', email)
    setLoading(true)
    void loginWithDemo(email, password)
      .then((result) => {
        console.log('[LoginPage] Demo login successful')
        setSession(result.user, result.token)
        navigate('/projects', { replace: true })
      })
      .catch((err) => {
        console.warn('[LoginPage] Demo login failed:', err)
        const safeName = email.split('@')[0] || 'Demo User'
        setSession(
          {
            id: `user-${Date.now()}`,
            name: safeName.charAt(0).toUpperCase() + safeName.slice(1),
            email,
          },
          `mock-token-${Date.now()}`
        )
        navigate('/projects', { replace: true })
      })
      .finally(() => {
        setLoading(false)
      })
  }

  const handleGoogleLogin = () => {
    console.log('[LoginPage] Google login button clicked')
    window.localStorage.removeItem('oauthIntent')
    console.log('[LoginPage] Redirecting to Google OAuth endpoint...')
    redirectToGoogleAuth()
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

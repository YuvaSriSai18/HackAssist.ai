import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card'
import {
  fetchCurrentUser,
  fetchGithubUser,
  parseOAuthCallback,
} from '../apis'
import { useAppState } from '../context/AppState'

export function AuthCallbackPage() {
  const { setSession, setGithubConnected, isAuthenticated } = useAppState()
  const navigate = useNavigate()

  useEffect(() => {
    const run = async () => {
      console.log('[AuthCallbackPage] ============ OAuth callback processing started ============')
      const callback = parseOAuthCallback(window.location.search)
      console.log('[AuthCallbackPage] Parsed OAuth callback:', {
        hasToken: !!callback.token,
        hasUser: !!callback.user,
        hasEmail: !!callback.email,
        hasName: !!callback.name,
        hasPicture: !!callback.picture,
        hasError: !!callback.error,
      })

      const intent = window.localStorage.getItem('oauthIntent')
      window.localStorage.removeItem('oauthIntent')
      console.log('[AuthCallbackPage] OAuth intent:', intent)

      if (callback.error) {
        console.error('[AuthCallbackPage] ✗ OAuth error from backend:', callback.error)
        navigate('/login', { replace: true })
        return
      }

      if (intent === 'github-connect') {
        const storedToken = window.localStorage.getItem('authToken')
        if (!storedToken) {
          console.warn('[AuthCallbackPage] No stored JWT for GitHub connect status')
          setGithubConnected(false, false)
          navigate('/profile', { replace: true })
          return
        }
        console.log('[AuthCallbackPage] Checking GitHub connection status via backend...')
        try {
          const response = await fetchGithubUser(storedToken)
          const connected = Boolean(response.connected ?? response.githubVerified)
          setGithubConnected(connected, response.githubVerified)
        } catch (err) {
          console.warn('[AuthCallbackPage] Failed to fetch GitHub user status', err)
          setGithubConnected(false, false)
        }
        navigate('/profile', { replace: true })
        return
      }

      if (!callback.token) {
        console.error('[AuthCallbackPage] ✗ No token in OAuth callback!')
        navigate('/login', { replace: true })
        return
      }

      console.log('[AuthCallbackPage] ✓ Token received from backend')

      let user
      try {
        console.log('[AuthCallbackPage] Attempting to fetch current user...')
        user = await fetchCurrentUser(callback.token)
        console.log('[AuthCallbackPage] ✓ User fetched:', user.email)
      } catch (err) {
        console.warn('[AuthCallbackPage] Could not fetch user, using callback data:', err)
        const fallbackEmail = callback.email ?? `${callback.user ?? 'user'}@oauth.local`
        user = {
          id: fallbackEmail,
          name: callback.name ?? fallbackEmail.split('@')[0],
          email: fallbackEmail,
        }
      }

      console.log('[AuthCallbackPage] Setting session with token and user...')
      setSession(user, callback.token)
      console.log('[AuthCallbackPage] ✓ Session saved to localStorage')

      if (isAuthenticated) {
        console.log('[AuthCallbackPage] Navigating to profile (GitHub connect or already authenticated)')
        setGithubConnected(true)
        navigate('/profile', { replace: true })
        return
      }

      console.log('[AuthCallbackPage] ✓ OAuth flow complete. Navigating to /projects')
      console.log('[AuthCallbackPage] ============ OAuth callback processing finished ============')
      navigate('/projects', { replace: true })
    }

    void run()
  }, [isAuthenticated, navigate, setGithubConnected, setSession])

  return (
    <div className="min-h-screen bg-night text-white">
      <div className="mx-auto flex min-h-screen max-w-6xl items-center justify-center px-6 py-8">
        <Card className="w-full max-w-md border border-white/10 bg-white/5">
          <CardHeader>
            <CardTitle>Completing sign-in</CardTitle>
            <CardDescription>We are finalizing your OAuth session.</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-white/70">Please wait...</CardContent>
        </Card>
      </div>
    </div>
  )
}

import type React from 'react'
import { useState } from 'react'
import { Chrome } from 'lucide-react'
import { Button } from '../ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card'
import { Input } from '../ui/input'

type LoginFormProps = {
  onSubmit: (email: string, password: string) => void
  onGoogle: () => void
  loading?: boolean
}

export function LoginForm({ onSubmit, onGoogle, loading = false }: LoginFormProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!email.trim() || !password.trim()) return
    onSubmit(email, password)
  }

  return (
    <Card className="w-full max-w-md border border-white/10 bg-white/5">
      <CardHeader>
        <CardTitle>Welcome back</CardTitle>
        <CardDescription>Sign in to continue to your hackathon workspace.</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <Input
            type="email"
            placeholder="you@team.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <Input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <Button type="submit" variant="accent" className="w-full" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </Button>
          <Button
            type="button"
            variant="outline"
            className="w-full"
            onClick={onGoogle}
            disabled={loading}
          >
            <Chrome size={18} />
            Continue with Google
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

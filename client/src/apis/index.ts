import type { User } from '../models/types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

type AuthCheckResponse = {
  authenticated?: boolean
  valid?: boolean
  message?: string
}

type AuthMeResponse = {
  uid?: string
  id?: string
  name?: string
  username?: string
  email?: string
}

type DemoLoginResponse = {
  token?: string
  username?: string
  email?: string
}

type OAuthCallbackPayload = {
  token: string | null
  user: string | null
  email: string | null
  name: string | null
  picture: string | null
  error: string | null
}

type GithubUserResponse = {
  connected?: boolean
  githubId?: string
  githubUsername?: string
  name?: string
  avatarUrl?: string
  githubVerified?: boolean
}

type GithubRepoResponse = {
  full_name?: string
  html_url?: string
  private?: boolean
}

type ProjectResponse = {
  id?: number
  projectId?: string
  name?: string
  description?: string
  createdAt?: string
  githubRepoUrl?: string
}

type ProjectPlanResponse = {
  projectId?: string
  problemStatement?: string
  techStack?: {
    backend?: string
    frontend?: string
    database?: string
    architecture?: string
  }
  features?: Array<{ key?: string; name?: string; description?: string; priority?: string }>
  modules?: Array<{ key?: string; name?: string; description?: string }>
  tasks?: Array<{
    externalId?: string
    title?: string
    description?: string
    priority?: string
    status?: string
    estimatedHours?: number
    moduleKey?: string
    dependsOn?: string[]
  }>
  risks?: Array<{ title?: string; impact?: string; mitigation?: string }>
}

function buildHeaders(token?: string): HeadersInit {
  if (!token) return { 'Content-Type': 'application/json' }
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  }
}

async function parseJsonSafe<T>(response: Response): Promise<T | null> {
  try {
    return (await response.json()) as T
  } catch {
    return null
  }
}

export function getGoogleAuthUrl() {
  const url = `${API_BASE_URL}/oauth2/authorization/google`
  console.log('[APIs] Google OAuth URL:', url)
  return url
}

export function getGithubAuthUrl() {
  console.log('[APIs] GitHub OAuth URL called')
  return `${API_BASE_URL}/oauth2/authorization/github`
}

export function getGithubConnectUrl() {
  const token = window.localStorage.getItem('authToken')
  const url = `${API_BASE_URL}/github/connect?token=${encodeURIComponent(token ?? '')}`
  console.log('[APIs] GitHub connect URL:', url)
  return url
}

export function redirectToGoogleAuth() {
  console.log('[APIs] Redirecting to Google Auth...')
  window.location.assign(getGoogleAuthUrl())
}

export function redirectToGithubAuth() {
  window.location.assign(getGithubAuthUrl())
}

export function redirectToGithubConnect() {
  window.location.assign(getGithubConnectUrl())
}

export function parseOAuthCallback(search: string): OAuthCallbackPayload {
  const params = new URLSearchParams(search)
  const payload = {
    token: params.get('token'),
    user: params.get('user'),
    email: params.get('email'),
    name: params.get('name'),
    picture: params.get('picture'),
    error: params.get('error'),
  }
  console.log('[APIs] Parsed OAuth callback from URL params:', payload)
  return payload
}

export async function loginWithDemo(email: string, _password: string) {
  console.log('[APIs] Demo login attempt for:', email)
  const demoLoginUrl = `${API_BASE_URL}/auth/demo-login?email=${encodeURIComponent(email)}`
  console.log('[APIs] Calling:', demoLoginUrl)
  
  const response = await fetch(demoLoginUrl, {
    method: 'POST',
    headers: buildHeaders(),
  })

  if (!response.ok) {
    console.error('[APIs] Demo login failed. Status:', response.status)
    throw new Error('Login failed')
  }

  const payload = await parseJsonSafe<DemoLoginResponse>(response)
  if (!payload?.token) {
    console.warn('[APIs] No token in demo login response')
    throw new Error('Missing token from login response')
  }

  const user: User = {
    id: payload.email ?? payload.username ?? email,
    name: payload.username ?? (payload.email ? payload.email.split('@')[0] : 'User'),
    email: payload.email ?? email,
  }

  console.log('[APIs] ✓ Demo login successful')
  return { user, token: payload.token }
}

export async function fetchCurrentUser(token: string) {
  console.log('[APIs] Fetching current user...')
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    console.warn('[APIs] Failed to fetch current user. Status:', response.status)
    throw new Error('Unable to fetch current user')
  }

  const payload = await parseJsonSafe<AuthMeResponse>(response)
  if (!payload) {
    console.warn('[APIs] Invalid user response (empty)')
    throw new Error('Invalid user response')
  }

  const email = payload.email ?? `${payload.uid ?? payload.id ?? payload.username ?? 'user'}@unknown.local`
  const user = {
    id: payload.uid ?? payload.id ?? payload.email ?? 'user',
    name: payload.name ?? payload.username ?? email.split('@')[0],
    email,
  } satisfies User
  
  console.log('[APIs] ✓ User fetched:', user.email)
  return user
}

export async function checkAuth(token: string) {
  const response = await fetch(`${API_BASE_URL}/auth/check`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) return false

  const payload = await parseJsonSafe<AuthCheckResponse>(response)
  if (!payload) return true

  if (typeof payload.authenticated === 'boolean') return payload.authenticated
  if (typeof payload.valid === 'boolean') return payload.valid
  return true
}

export async function logoutRequest(token?: string) {
  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: 'POST',
    headers: buildHeaders(token),
  })
}

export async function fetchGithubConnectionStatus(token: string) {
  const response = await fetch(`${API_BASE_URL}/github/user`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw new Error('Unable to fetch GitHub connection status')
  }

  const payload = await parseJsonSafe<{ connected?: boolean }>(response)
  return Boolean(payload?.connected)
}

export async function fetchGithubUser(token: string) {
  const response = await fetch(`${API_BASE_URL}/github/user`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw new Error('Unable to fetch GitHub user')
  }

  const payload = await parseJsonSafe<GithubUserResponse>(response)
  return payload ?? { connected: false }
}

export async function fetchGithubRepos(token: string) {
  const response = await fetch(`${API_BASE_URL}/github/repos`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw new Error('Unable to fetch GitHub repositories')
  }

  const payload = await parseJsonSafe<GithubRepoResponse[]>(response)
  return payload ?? []
}

export async function createProject(token: string, payload: { name: string; description: string }) {
  const response = await fetch(`${API_BASE_URL}/projects`, {
    method: 'POST',
    headers: buildHeaders(token),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Unable to create project')
  }

  const data = await parseJsonSafe<ProjectResponse>(response)
  if (!data) {
    throw new Error('Invalid project response')
  }
  return data
}

export async function fetchMyProjects(token: string) {
  const response = await fetch(`${API_BASE_URL}/projects/my`, {
    method: 'GET',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw new Error('Unable to fetch projects')
  }

  const data = await parseJsonSafe<ProjectResponse[]>(response)
  return data ?? []
}

export async function generateTasks(
  token: string,
  payload: { projectId: string; problemStatement: string }
) {
  console.log('[generateTasks] Sending request:', {
    projectId: payload.projectId,
    problemStatement: payload.problemStatement?.substring(0, 50) + '...',
  })

  if (!payload.projectId || !payload.projectId.trim()) {
    throw new Error('Project ID is required')
  }

  if (!payload.problemStatement || !payload.problemStatement.trim()) {
    throw new Error('Problem statement is required')
  }

  const response = await fetch(`${API_BASE_URL}/ai/generate-tasks`, {
    method: 'POST',
    headers: buildHeaders(token),
    body: JSON.stringify(payload),
  })

  console.log('[generateTasks] Response status:', response.status)

  if (!response.ok) {
    const errorText = await response.text()
    console.error('[generateTasks] Error response:', errorText)
    throw new Error(`Generate tasks failed: ${response.status} - ${errorText}`)
  }

  const data = await parseJsonSafe<ProjectPlanResponse>(response)
  if (!data) {
    throw new Error('Invalid AI response')
  }
  return data
}

export async function finalizeTasks(token: string, projectId: string, payload: ProjectPlanResponse) {
  const response = await fetch(`${API_BASE_URL}/projects/${projectId}/finalize-tasks`, {
    method: 'POST',
    headers: buildHeaders(token),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Unable to finalize tasks')
  }
}

export async function updateProject(
  token: string,
  projectId: string,
  payload: { name?: string; description?: string }
) {
  const response = await fetch(`${API_BASE_URL}/projects/${projectId}`, {
    method: 'PUT',
    headers: buildHeaders(token),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Unable to update project')
  }

  const data = await parseJsonSafe<ProjectResponse>(response)
  if (!data) {
    throw new Error('Invalid project response')
  }
  return data
}

export async function deleteProject(token: string, projectId: string) {
  const response = await fetch(`${API_BASE_URL}/projects/${projectId}`, {
    method: 'DELETE',
    headers: buildHeaders(token),
  })

  if (!response.ok) {
    throw new Error('Unable to delete project')
  }
}

export async function linkProjectRepo(
  token: string,
  projectId: string,
  payload: { repoUrl?: string; repoFullName?: string }
) {
  const response = await fetch(`${API_BASE_URL}/projects/${projectId}/repo`, {
    method: 'PUT',
    headers: buildHeaders(token),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Unable to link repository')
  }

  const data = await parseJsonSafe<ProjectResponse>(response)
  if (!data) {
    throw new Error('Invalid project response')
  }
  return data
}

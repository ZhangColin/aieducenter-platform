import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { act } from 'react'
import { useAuthStore } from '@aieducenter/shared/auth-store'
import { AuthGuard } from './auth-guard'

// Hoist mock functions so they are available inside vi.mock factories
const { mockReplace, mockLogin, mockLogout, mockOnFinishHydration } = vi.hoisted(() => ({
  mockReplace: vi.fn(),
  mockLogin: vi.fn(),
  mockLogout: vi.fn(),
  mockOnFinishHydration: vi.fn(),
}))

// Mock next/navigation
vi.mock('next/navigation', () => ({
  useRouter: () => ({ replace: mockReplace }),
}))

vi.mock('@aieducenter/shared/auth-store', () => {
  return {
    useAuthStore: Object.assign(
      vi.fn(() => ({
        token: null,
        user: null,
        isAuthenticated: false,
        login: mockLogin,
        logout: mockLogout,
        setUser: vi.fn(),
      })),
      {
        getState: vi.fn(() => ({
          token: null,
          user: null,
          isAuthenticated: false,
          login: mockLogin,
          logout: mockLogout,
          setUser: vi.fn(),
        })),
        persist: {
          hasHydrated: vi.fn(() => false),
          onFinishHydration: mockOnFinishHydration,
        },
      }
    ),
  }
})

describe('AuthGuard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(false)
    vi.mocked(mockOnFinishHydration).mockImplementation(() => {})
    vi.mocked(useAuthStore).mockReturnValue({
      token: null,
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
    vi.mocked(useAuthStore.getState).mockReturnValue({
      token: null,
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
  })

  it('given_hydration_pending_when_render_then_show_loading_and_no_redirect', () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(false)
    // onFinishHydration is registered but never called — hydration never completes

    render(
      <AuthGuard>
        <div>Protected Content</div>
      </AuthGuard>
    )

    expect(screen.getByTestId('auth-loading')).toBeTruthy()
    expect(screen.queryByText('Protected Content')).toBeNull()
    expect(mockReplace).not.toHaveBeenCalled()
  })

  it('given_hydrated_no_token_when_render_then_redirect_to_login', async () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(true)
    vi.mocked(useAuthStore).mockReturnValue({
      token: null,
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })

    render(
      <AuthGuard>
        <div>Protected Content</div>
      </AuthGuard>
    )

    await waitFor(() => {
      expect(mockReplace).toHaveBeenCalledWith('/login')
    })
    expect(screen.queryByText('Protected Content')).toBeNull()
  })

  it('given_hydrated_token_no_user_when_profile_fetch_succeeds_then_render_children', async () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(true)
    vi.mocked(useAuthStore).mockReturnValue({
      token: 'valid-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
    vi.mocked(useAuthStore.getState).mockReturnValue({
      token: 'valid-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })

    const profileData = {
      code: 200,
      data: {
        userId: '42',
        nickname: 'Test User',
        avatar: 'https://example.com/avatar.jpg',
      },
    }
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => profileData,
      })
    )

    await act(async () => {
      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )
    })

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('valid-jwt-token', {
        userId: '42',
        nickname: 'Test User',
        avatar: 'https://example.com/avatar.jpg',
      })
    })
    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeTruthy()
    })
    expect(mockReplace).not.toHaveBeenCalled()

    vi.unstubAllGlobals()
  })

  it('given_hydrated_token_no_user_when_profile_fetch_returns_401_then_no_duplicate_logout', async () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(true)
    vi.mocked(useAuthStore).mockReturnValue({
      token: 'expired-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
    vi.mocked(useAuthStore.getState).mockReturnValue({
      token: 'expired-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 401,
      })
    )

    await act(async () => {
      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )
    })

    await waitFor(() => {
      expect(vi.mocked(fetch)).toHaveBeenCalled()
    })

    // authErrorMiddleware already handles 401 — component must NOT call logout again
    expect(mockLogout).not.toHaveBeenCalled()
    expect(mockReplace).not.toHaveBeenCalled()
    expect(screen.queryByText('Protected Content')).toBeNull()

    vi.unstubAllGlobals()
  })

  it('given_hydrated_token_no_user_when_profile_fetch_network_error_then_logout_and_redirect', async () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(true)
    vi.mocked(useAuthStore).mockReturnValue({
      token: 'valid-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
    vi.mocked(useAuthStore.getState).mockReturnValue({
      token: 'valid-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })

    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new Error('Network error'))
    )

    await act(async () => {
      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )
    })

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalled()
    })
    await waitFor(() => {
      expect(mockReplace).toHaveBeenCalledWith('/login')
    })
    expect(screen.queryByText('Protected Content')).toBeNull()

    vi.unstubAllGlobals()
  })

  it('given_hydrated_already_authenticated_when_render_then_render_children_without_fetch', async () => {
    vi.mocked(useAuthStore.persist.hasHydrated).mockReturnValue(true)
    vi.mocked(useAuthStore).mockReturnValue({
      token: 'valid-jwt-token',
      user: { userId: '42', nickname: 'Test User', avatar: null },
      isAuthenticated: true,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })
    vi.mocked(useAuthStore.getState).mockReturnValue({
      token: 'valid-jwt-token',
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: mockLogout,
      setUser: vi.fn(),
    })

    const fetchFn = vi.fn()
    vi.stubGlobal('fetch', fetchFn)
    const fetchSpy = fetchFn

    await act(async () => {
      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      )
    })

    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeTruthy()
    })
    expect(fetchSpy).not.toHaveBeenCalled()
    expect(mockReplace).not.toHaveBeenCalled()

    vi.unstubAllGlobals()
  })
})

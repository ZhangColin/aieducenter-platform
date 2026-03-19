import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useAuthStore } from './auth-store'

describe('AuthStore', () => {
  beforeEach(() => {
    // Reset store state before each test
    useAuthStore.setState({ token: null, user: null, isAuthenticated: false })
    // Clear localStorage
    localStorage.clear()
  })

  it('given_no_state_when_login_then_token_and_user_set_and_isAuthenticated_true', () => {
    const token = 'test-jwt-token'
    const user = {
      userId: '12345',
      nickname: 'Test User',
      avatar: null,
    }

    useAuthStore.getState().login(token, user)

    const state = useAuthStore.getState()
    expect(state.token).toBe(token)
    expect(state.user).toEqual(user)
    expect(state.isAuthenticated).toBe(true)
  })

  it('given_authenticated_when_logout_then_all_state_cleared', () => {
    const token = 'test-jwt-token'
    const user = {
      userId: '12345',
      nickname: 'Test User',
      avatar: 'https://example.com/avatar.jpg',
    }

    // Setup authenticated state
    useAuthStore.getState().login(token, user)
    expect(useAuthStore.getState().isAuthenticated).toBe(true)

    // Logout
    useAuthStore.getState().logout()

    const state = useAuthStore.getState()
    expect(state.token).toBeNull()
    expect(state.user).toBeNull()
    expect(state.isAuthenticated).toBe(false)
  })

  it('given_authenticated_when_setUser_then_user_updated', () => {
    const token = 'test-jwt-token'
    const initialUser = {
      userId: '12345',
      nickname: 'Old Name',
      avatar: null,
    }

    // Setup authenticated state
    useAuthStore.getState().login(token, initialUser)

    // Update user
    const updatedUser = {
      userId: '12345',
      nickname: 'New Name',
      avatar: 'https://example.com/avatar.jpg',
    }
    useAuthStore.getState().setUser(updatedUser)

    const state = useAuthStore.getState()
    expect(state.user).toEqual(updatedUser)
    expect(state.token).toBe(token) // Token should remain unchanged
    expect(state.isAuthenticated).toBe(true) // Should remain authenticated
  })

  it('given_token_in_localStorage_when_store_created_then_token_restored', () => {
    // Mock localStorage with persisted auth state
    const persistedState = {
      state: {
        token: 'persisted-jwt-token',
      },
    }
    localStorage.setItem('aieducenter-auth', JSON.stringify(persistedState))

    // Trigger hydration from localStorage
    useAuthStore.persist.rehydrate()

    const state = useAuthStore.getState()
    expect(state.token).toBe('persisted-jwt-token')
  })
})

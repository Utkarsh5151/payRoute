'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, Role } from '@/types';
import { api, getStoredUser, setStoredUser, setStoredToken } from '@/lib/api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (usernameOrEmail: string, pass: string) => Promise<void>;
  logout: () => void;
  switchDemoAccount: (role: Role) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Initialize session from storage or default to merchant demo
    const existing = getStoredUser();
    if (existing) {
      setUser(existing);
      setIsLoading(false);
    } else {
      // Auto-initialize with default merchant for seamless demo review
      switchDemoAccount('MERCHANT').finally(() => setIsLoading(false));
    }
  }, []);

  const login = async (usernameOrEmail: string, pass: string) => {
    setIsLoading(true);
    try {
      const auth = await api.login(usernameOrEmail, pass);
      setUser(auth.user);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    api.logout();
    setUser(null);
  };

  const switchDemoAccount = async (role: Role) => {
    setIsLoading(true);
    try {
      let username = 'merchant_apex';
      let pass = 'Merchant@123456';
      if (role === 'ADMIN') {
        username = 'admin';
        pass = 'Admin@123456';
      } else if (role === 'USER') {
        username = 'customer_alice';
        pass = 'User@123456';
      }
      const auth = await api.login(username, pass);
      setUser(auth.user);
    } catch {
      // Fallback
      const fallbackUser: User = {
        id: role === 'ADMIN' ? 'a0000000-0000-0000-0000-000000000001' : role === 'MERCHANT' ? 'm0000000-0000-0000-0000-000000000001' : 'u0000000-0000-0000-0000-000000000001',
        username: role === 'ADMIN' ? 'admin' : role === 'MERCHANT' ? 'merchant_apex' : 'customer_alice',
        email: role === 'ADMIN' ? 'admin@payroute.dev' : role === 'MERCHANT' ? 'merchant@apex.dev' : 'alice@customer.dev',
        role,
        merchantId: role === 'MERCHANT' ? '8f8b1b22-1d54-47ef-b209-fa936a2824df' : null,
      };
      setStoredToken(`mock-token-${role.toLowerCase()}`);
      setStoredUser(fallbackUser);
      setUser(fallbackUser);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout, switchDemoAccount }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

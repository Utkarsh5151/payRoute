'use client';

import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types';
import {
  Activity,
  Layers,
  Sliders,
  Send,
  ShieldCheck,
  Zap,
  UserCheck,
  ChevronDown,
  LogOut,
  Info,
  Server
} from 'lucide-react';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  onOpenArchitecture: () => void;
}

export default function Navbar({ activeTab, setActiveTab, onOpenArchitecture }: NavbarProps) {
  const { user, switchDemoAccount, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const roles: { role: Role; label: string; email: string }[] = [
    { role: 'ADMIN', label: 'Admin (System)', email: 'admin@payroute.dev' },
    { role: 'MERCHANT', label: 'Merchant Apex', email: 'merchant@apex.dev' },
    { role: 'USER', label: 'Customer Alice', email: 'alice@customer.dev' },
  ];

  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-tr from-brand-600 to-cyan-400 text-white shadow-lg shadow-brand-500/25">
            <Zap className="h-5 w-5" />
            <span className="absolute -bottom-1 -right-1 flex h-3.5 w-3.5 items-center justify-center rounded-full bg-emerald-500 ring-2 ring-slate-950">
              <span className="h-1.5 w-1.5 animate-ping rounded-full bg-white opacity-75"></span>
            </span>
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-black tracking-tight text-white">
                Pay<span className="bg-gradient-to-r from-brand-400 to-cyan-400 bg-clip-text text-transparent">Route</span>
              </span>
              <span className="rounded-md bg-brand-950/80 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-brand-300 ring-1 ring-brand-500/30">
                Orchestrator v1.0
              </span>
            </div>
            <p className="hidden text-xs text-slate-400 sm:block">
              Intelligent Routing • State Machine • Resilience
            </p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="hidden items-center space-x-1 md:flex">
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`flex items-center space-x-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-all ${
              activeTab === 'dashboard'
                ? 'bg-slate-800/90 text-brand-400 ring-1 ring-slate-700 shadow-sm'
                : 'text-slate-400 hover:bg-slate-850 hover:text-slate-200'
            }`}
          >
            <Activity className="h-4 w-4" />
            <span>Overview</span>
          </button>

          <button
            onClick={() => setActiveTab('simulator')}
            className={`flex items-center space-x-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-all ${
              activeTab === 'simulator'
                ? 'bg-slate-800/90 text-brand-400 ring-1 ring-slate-700 shadow-sm'
                : 'text-slate-400 hover:bg-slate-850 hover:text-slate-200'
            }`}
          >
            <Send className="h-4 w-4" />
            <span>Payment Simulator</span>
          </button>

          <button
            onClick={() => setActiveTab('transactions')}
            className={`flex items-center space-x-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-all ${
              activeTab === 'transactions'
                ? 'bg-slate-800/90 text-brand-400 ring-1 ring-slate-700 shadow-sm'
                : 'text-slate-400 hover:bg-slate-850 hover:text-slate-200'
            }`}
          >
            <Layers className="h-4 w-4" />
            <span>Transactions & Audits</span>
          </button>

          <button
            onClick={() => setActiveTab('chaos')}
            className={`flex items-center space-x-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-all ${
              activeTab === 'chaos'
                ? 'bg-slate-800/90 text-brand-400 ring-1 ring-slate-700 shadow-sm'
                : 'text-slate-400 hover:bg-slate-850 hover:text-slate-200'
            }`}
          >
            <Sliders className="h-4 w-4" />
            <span>Chaos Lab</span>
          </button>
        </nav>

        {/* Right side controls: Architecture Button + Account Switcher */}
        <div className="flex items-center space-x-3">
          <button
            onClick={onOpenArchitecture}
            className="flex items-center space-x-1.5 rounded-lg border border-slate-700/80 bg-slate-900/80 px-2.5 py-1.5 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-colors"
            title="View system architecture and data flow"
          >
            <Info className="h-3.5 w-3.5 text-cyan-400" />
            <span className="hidden sm:inline">Architecture</span>
          </button>

          {/* Quick User Switcher */}
          <div className="relative">
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center space-x-2 rounded-xl border border-slate-800 bg-slate-900/90 px-3 py-1.5 text-xs text-slate-200 transition-colors hover:border-slate-700"
            >
              <div className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-500/20 text-brand-400 font-bold">
                {user?.username ? user.username.substring(0, 1).toUpperCase() : 'U'}
              </div>
              <div className="text-left hidden sm:block">
                <div className="font-semibold text-white leading-none">{user?.username || 'Guest'}</div>
                <div className="text-[10px] text-slate-400 leading-none mt-0.5 font-mono uppercase text-brand-400">
                  {user?.role || 'USER'}
                </div>
              </div>
              <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
            </button>

            {dropdownOpen && (
              <div className="absolute right-0 mt-2 w-56 rounded-xl border border-slate-800 bg-slate-900 p-2 shadow-2xl backdrop-blur-2xl z-50">
                <div className="px-2 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400 border-b border-slate-800/80">
                  Switch Active Role
                </div>
                <div className="py-1">
                  {roles.map((item) => (
                    <button
                      key={item.role}
                      onClick={() => {
                        switchDemoAccount(item.role);
                        setDropdownOpen(false);
                      }}
                      className={`flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-xs transition-colors ${
                        user?.role === item.role
                          ? 'bg-brand-500/10 text-brand-400 font-semibold'
                          : 'text-slate-300 hover:bg-slate-800'
                      }`}
                    >
                      <div className="text-left">
                        <div>{item.label}</div>
                        <div className="text-[10px] text-slate-500">{item.email}</div>
                      </div>
                      {user?.role === item.role && <UserCheck className="h-3.5 w-3.5 text-brand-400" />}
                    </button>
                  ))}
                </div>
                <div className="border-t border-slate-800/80 pt-1">
                  <button
                    onClick={() => {
                      logout();
                      setDropdownOpen(false);
                    }}
                    className="flex w-full items-center space-x-2 rounded-lg px-2.5 py-1.5 text-xs text-rose-400 hover:bg-rose-500/10 transition-colors"
                  >
                    <LogOut className="h-3.5 w-3.5" />
                    <span>Reset Session</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile subnav */}
      <div className="flex border-t border-slate-800/80 px-4 py-2 md:hidden overflow-x-auto space-x-2">
        <button
          onClick={() => setActiveTab('dashboard')}
          className={`flex items-center space-x-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium ${
            activeTab === 'dashboard' ? 'bg-slate-800 text-brand-400' : 'text-slate-400'
          }`}
        >
          <Activity className="h-3.5 w-3.5" />
          <span>Overview</span>
        </button>
        <button
          onClick={() => setActiveTab('simulator')}
          className={`flex items-center space-x-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium ${
            activeTab === 'simulator' ? 'bg-slate-800 text-brand-400' : 'text-slate-400'
          }`}
        >
          <Send className="h-3.5 w-3.5" />
          <span>Simulator</span>
        </button>
        <button
          onClick={() => setActiveTab('transactions')}
          className={`flex items-center space-x-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium ${
            activeTab === 'transactions' ? 'bg-slate-800 text-brand-400' : 'text-slate-400'
          }`}
        >
          <Layers className="h-3.5 w-3.5" />
          <span>Transactions</span>
        </button>
        <button
          onClick={() => setActiveTab('chaos')}
          className={`flex items-center space-x-1.5 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium ${
            activeTab === 'chaos' ? 'bg-slate-800 text-brand-400' : 'text-slate-400'
          }`}
        >
          <Sliders className="h-3.5 w-3.5" />
          <span>Chaos Lab</span>
        </button>
      </div>
    </header>
  );
}

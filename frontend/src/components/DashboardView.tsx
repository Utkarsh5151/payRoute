'use client';

import React, { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { DashboardStats, Payment } from '@/types';
import {
  TrendingUp,
  DollarSign,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Zap,
  ArrowUpRight,
  Shield,
  Server,
  RefreshCw,
  Cpu,
  Layers
} from 'lucide-react';

interface DashboardViewProps {
  onSelectPayment: (payment: Payment) => void;
  onNavigateToSimulator: () => void;
}

export default function DashboardView({ onSelectPayment, onNavigateToSimulator }: DashboardViewProps) {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const fetchStats = async () => {
    try {
      const data = await api.getDashboardStats();
      setStats(data);
    } catch (e) {
      console.error('Failed to load dashboard stats', e);
    } finally {
      setLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    fetchStats();
    // Poll every 10 seconds for real-time updates
    const interval = setInterval(fetchStats, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleManualRefresh = () => {
    setIsRefreshing(true);
    fetchStats();
  };

  if (loading && !stats) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-brand-500 border-t-transparent"></div>
          <p className="text-sm text-slate-400">Loading platform metrics...</p>
        </div>
      </div>
    );
  }

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 2,
    }).format(val || 0);
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Top Banner: Real-time System Status & Refresh */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
            Orchestration Intelligence Dashboard
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time telemetry, routing performance, and multi-gateway resilience monitoring.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={handleManualRefresh}
            disabled={isRefreshing}
            className="flex items-center space-x-2 rounded-xl border border-slate-800 bg-slate-900/90 px-3.5 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-all shadow-sm"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${isRefreshing ? 'animate-spin text-brand-400' : ''}`} />
            <span>{isRefreshing ? 'Syncing...' : 'Refresh Metrics'}</span>
          </button>
          <button
            onClick={onNavigateToSimulator}
            className="flex items-center space-x-2 rounded-xl bg-gradient-to-r from-brand-600 to-indigo-600 px-4 py-2 text-xs font-bold text-white shadow-lg shadow-brand-500/25 hover:from-brand-500 hover:to-indigo-500 transition-all hover:shadow-brand-500/40"
          >
            <Zap className="h-3.5 w-3.5" />
            <span>Simulate Payment</span>
          </button>
        </div>
      </div>

      {/* Service Mesh Status Bar */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-5">
        <div className="glass-panel flex items-center space-x-2.5 rounded-xl p-3 border-emerald-500/20">
          <span className="relative flex h-2.5 w-2.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
          </span>
          <div className="text-xs">
            <div className="font-semibold text-slate-200">PostgreSQL 16</div>
            <div className="text-[10px] text-emerald-400">ACID State Machine</div>
          </div>
        </div>

        <div className="glass-panel flex items-center space-x-2.5 rounded-xl p-3 border-cyan-500/20">
          <span className="relative flex h-2.5 w-2.5">
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-cyan-500"></span>
          </span>
          <div className="text-xs">
            <div className="font-semibold text-slate-200">Redis 7 Cluster</div>
            <div className="text-[10px] text-cyan-400">Token Bucket Limiter</div>
          </div>
        </div>

        <div className="glass-panel flex items-center space-x-2.5 rounded-xl p-3 border-indigo-500/20">
          <span className="relative flex h-2.5 w-2.5">
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-indigo-500"></span>
          </span>
          <div className="text-xs">
            <div className="font-semibold text-slate-200">Haskell Scotty</div>
            <div className="text-[10px] text-indigo-400">Pure Rules Engine</div>
          </div>
        </div>

        <div className="glass-panel flex items-center space-x-2.5 rounded-xl p-3 border-violet-500/20">
          <span className="relative flex h-2.5 w-2.5">
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-violet-500"></span>
          </span>
          <div className="text-xs">
            <div className="font-semibold text-slate-200">Apache Kafka 3.7</div>
            <div className="text-[10px] text-violet-400">Event Sourcing / DLQ</div>
          </div>
        </div>

        <div className="glass-panel col-span-2 sm:col-span-4 lg:col-span-1 flex items-center space-x-2.5 rounded-xl p-3 border-amber-500/20">
          <span className="relative flex h-2.5 w-2.5">
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-amber-500"></span>
          </span>
          <div className="text-xs">
            <div className="font-semibold text-slate-200">Resilience4j</div>
            <div className="text-[10px] text-amber-400">Circuit Breakers & Retries</div>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Total Volume */}
        <div className="glass-panel glass-card-hover rounded-2xl p-5 border-slate-800">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Processed Volume</span>
            <div className="rounded-lg bg-emerald-500/10 p-2 text-emerald-400">
              <DollarSign className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-black tracking-tight text-white sm:text-3xl">
              {formatCurrency(stats?.totalVolume || 0)}
            </div>
            <div className="mt-2 flex items-center space-x-1.5 text-xs text-emerald-400">
              <TrendingUp className="h-3.5 w-3.5" />
              <span>Settled via active orchestration</span>
            </div>
          </div>
        </div>

        {/* Total Payments */}
        <div className="glass-panel glass-card-hover rounded-2xl p-5 border-slate-800">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Transactions</span>
            <div className="rounded-lg bg-brand-500/10 p-2 text-brand-400">
              <Layers className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-black tracking-tight text-white sm:text-3xl">
              {stats?.totalPayments || 0}
            </div>
            <div className="mt-2 text-xs text-slate-400">
              <span className="font-semibold text-emerald-400">{stats?.successfulPayments || 0}</span> success •{' '}
              <span className="font-semibold text-rose-400">{stats?.failedPayments || 0}</span> failed
            </div>
          </div>
        </div>

        {/* Success Rate */}
        <div className="glass-panel glass-card-hover rounded-2xl p-5 border-slate-800">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Routing Success Rate</span>
            <div className="rounded-lg bg-cyan-500/10 p-2 text-cyan-400">
              <CheckCircle2 className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-black tracking-tight text-white sm:text-3xl">
              {stats?.successRatePercentage ?? 100}%
            </div>
            <div className="mt-2 text-xs text-cyan-400">
              Automated multi-gateway fallback active
            </div>
          </div>
        </div>

        {/* Average SLA */}
        <div className="glass-panel glass-card-hover rounded-2xl p-5 border-slate-800">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Avg Gateway Latency</span>
            <div className="rounded-lg bg-violet-500/10 p-2 text-violet-400">
              <Clock className="h-4 w-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-black tracking-tight text-white sm:text-3xl">
              {stats?.providerCards?.length
                ? Math.round(
                    stats.providerCards.reduce((acc, p) => acc + p.avgLatencyMs, 0) /
                      stats.providerCards.length
                  )
                : 180}
              ms
            </div>
            <div className="mt-2 text-xs text-slate-400">
              P95 target &lt; 250ms SLA
            </div>
          </div>
        </div>
      </div>

      {/* Provider Health & Circuit Breakers Section */}
      <div>
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-white flex items-center space-x-2">
              <Shield className="h-5 w-5 text-brand-400" />
              <span>Simulated Payment Gateways & Circuit Breaker States</span>
            </h2>
            <p className="text-xs text-slate-400">
              Resilience4j sliding window failure rates dynamically open circuit breakers to prevent cascade failures.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          {stats?.providerCards?.map((p) => {
            const isClosed = p.circuitBreakerState === 'CLOSED';
            const isHalfOpen = p.circuitBreakerState === 'HALF_OPEN';
            const isOpen = p.circuitBreakerState === 'OPEN';

            return (
              <div
                key={p.providerCode}
                className="glass-panel glass-card-hover rounded-2xl p-5 border-slate-800 relative overflow-hidden"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <span className="font-mono text-[10px] font-bold text-slate-400 tracking-wider">
                      {p.providerCode}
                    </span>
                    <h3 className="text-base font-bold text-white mt-0.5">{p.providerName}</h3>
                  </div>

                  {/* Circuit Breaker Badge */}
                  <div
                    className={`inline-flex items-center space-x-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${
                      isClosed
                        ? 'bg-emerald-500/10 text-emerald-400 ring-1 ring-emerald-500/30'
                        : isHalfOpen
                        ? 'bg-amber-500/10 text-amber-400 ring-1 ring-amber-500/30'
                        : 'bg-rose-500/10 text-rose-400 ring-1 ring-rose-500/30'
                    }`}
                  >
                    <span
                      className={`h-2 w-2 rounded-full ${
                        isClosed ? 'bg-emerald-400' : isHalfOpen ? 'bg-amber-400' : 'bg-rose-400'
                      }`}
                    ></span>
                    <span>CB: {p.circuitBreakerState}</span>
                  </div>
                </div>

                <div className="mt-4 grid grid-cols-2 gap-3 border-t border-slate-800/80 pt-4">
                  <div>
                    <span className="text-[11px] text-slate-400">Historical Success</span>
                    <div className="text-lg font-bold text-white mt-0.5">{p.successRate}%</div>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-400">Avg Simulated Latency</span>
                    <div className="text-lg font-bold text-white mt-0.5">{p.avgLatencyMs}ms</div>
                  </div>
                </div>

                <div className="mt-4 flex items-center justify-between text-[11px] text-slate-400 bg-slate-900/60 rounded-lg p-2">
                  <span>Routing Affinity:</span>
                  <span className="font-medium text-slate-200">
                    {p.providerCode === 'PROVIDER_A'
                      ? 'High UPI Priority'
                      : p.providerCode === 'PROVIDER_B'
                      ? 'High Card Affinity'
                      : 'Global Net Banking'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Recent Transactions Feed */}
      <div className="glass-panel rounded-2xl p-6 border-slate-800">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800/80">
          <div>
            <h2 className="text-lg font-bold text-white">Recent Payment Transactions</h2>
            <p className="text-xs text-slate-400">
              Click any transaction row to inspect state transitions and provider attempt logs.
            </p>
          </div>
          <button
            onClick={onNavigateToSimulator}
            className="flex items-center space-x-1 text-xs font-semibold text-brand-400 hover:text-brand-300"
          >
            <span>Launch Checkout</span>
            <ArrowUpRight className="h-3.5 w-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto mt-4">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
                <th className="py-3 px-3">Transaction ID / Ref</th>
                <th className="py-3 px-3">Customer</th>
                <th className="py-3 px-3">Amount</th>
                <th className="py-3 px-3">Method</th>
                <th className="py-3 px-3">Gateway Selected</th>
                <th className="py-3 px-3">Attempts</th>
                <th className="py-3 px-3">Status</th>
                <th className="py-3 px-3 text-right">Audit</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {stats?.recentPayments?.map((payment) => {
                const isSuccess = payment.status === 'SUCCESS';
                const isFailed = payment.status === 'FAILED';
                const isRefunded = payment.status === 'REFUNDED';

                return (
                  <tr
                    key={payment.id}
                    onClick={() => onSelectPayment(payment)}
                    className="hover:bg-slate-850/60 cursor-pointer transition-colors group"
                  >
                    <td className="py-3.5 px-3">
                      <div className="font-mono text-slate-200 group-hover:text-brand-400 transition-colors">
                        {payment.id.substring(0, 8)}...
                      </div>
                      <div className="text-[10px] text-slate-500 font-mono">
                        {payment.clientReferenceId || 'N/A'}
                      </div>
                    </td>
                    <td className="py-3.5 px-3 text-slate-300">
                      <div>{payment.customerEmail}</div>
                      <div className="text-[10px] text-slate-500 truncate max-w-[150px]">
                        {payment.description || 'Simulated Order'}
                      </div>
                    </td>
                    <td className="py-3.5 px-3 font-semibold text-white">
                      {payment.currency} {payment.amount.toFixed(2)}
                    </td>
                    <td className="py-3.5 px-3">
                      <span className="rounded-md bg-slate-800 px-2 py-1 text-[10px] font-semibold text-slate-300">
                        {payment.paymentMethod}
                      </span>
                    </td>
                    <td className="py-3.5 px-3 text-slate-300">
                      {payment.selectedProviderName || payment.selectedProviderCode || 'None (Failed)'}
                    </td>
                    <td className="py-3.5 px-3">
                      <span className="font-mono text-slate-400">
                        {payment.attemptsCount || 1} {payment.attemptsCount > 1 ? 'hops' : 'hop'}
                      </span>
                    </td>
                    <td className="py-3.5 px-3">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[10px] font-bold ${
                          isSuccess
                            ? 'bg-emerald-500/10 text-emerald-400 ring-1 ring-emerald-500/20'
                            : isFailed
                            ? 'bg-rose-500/10 text-rose-400 ring-1 ring-rose-500/20'
                            : isRefunded
                            ? 'bg-cyan-500/10 text-cyan-400 ring-1 ring-cyan-500/20'
                            : 'bg-amber-500/10 text-amber-400 ring-1 ring-amber-500/20'
                        }`}
                      >
                        {payment.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-3 text-right">
                      <button className="rounded-lg bg-slate-800 px-2.5 py-1 text-[11px] font-semibold text-slate-300 group-hover:bg-brand-600 group-hover:text-white transition-colors">
                        Inspect
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

'use client';

import React, { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { PaymentProvider } from '@/types';
import {
  Sliders,
  Flame,
  Turtle,
  HeartHandshake,
  Shield,
  Save,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Zap,
  Activity
} from 'lucide-react';

export default function ProviderChaosLab() {
  const [providers, setProviders] = useState<PaymentProvider[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const fetchProviders = async () => {
    setLoading(true);
    try {
      const data = await api.getProviders();
      setProviders(data);
    } catch (e) {
      console.error('Failed to load providers', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProviders();
  }, []);

  const handleFieldChange = (
    providerId: string,
    field: keyof PaymentProvider,
    value: any
  ) => {
    setProviders((prev) =>
      prev.map((p) => (p.id === providerId ? { ...p, [field]: value } : p))
    );
  };

  const handleSaveConfig = async (provider: PaymentProvider) => {
    setSavingId(provider.id);
    setStatusMessage(null);
    try {
      await api.updateProviderConfig(provider.id, {
        avgLatencyMs: provider.avgLatencyMs,
        successRate: provider.successRate,
        failureRate: provider.failureRate,
        timeoutRate: provider.timeoutRate,
        priorityWeight: provider.priorityWeight,
        status: provider.status,
      });
      setStatusMessage(`Updated ${provider.name} simulation parameters successfully.`);
      fetchProviders();
    } catch (e: any) {
      alert('Error updating provider: ' + (e.message || 'Unknown error'));
    } finally {
      setSavingId(null);
    }
  };

  // Preset 1: Trip ApexPay Circuit Breaker
  const handleTripApexPay = async () => {
    const apex = providers.find((p) => p.code === 'PROVIDER_A');
    if (!apex) return;
    setSavingId(apex.id);
    try {
      await api.updateProviderConfig(apex.id, {
        failureRate: 90.0,
        successRate: 10.0,
        avgLatencyMs: 450,
      });
      setStatusMessage('🔥 Simulating ApexPay Outage (90% failure rate). Next payments will trip circuit breaker and route to NovaPay!');
      fetchProviders();
    } finally {
      setSavingId(null);
    }
  };

  // Preset 2: Spike NovaPay Latency
  const handleSpikeNovaPay = async () => {
    const nova = providers.find((p) => p.code === 'PROVIDER_B');
    if (!nova) return;
    setSavingId(nova.id);
    try {
      await api.updateProviderConfig(nova.id, {
        avgLatencyMs: 1850,
      });
      setStatusMessage('🐢 NovaPay latency spiked to 1850ms. Intelligent routing engine will heavily penalize its composite score.');
      fetchProviders();
    } finally {
      setSavingId(null);
    }
  };

  // Preset 3: Restore All Providers
  const handleRestoreAll = async () => {
    setLoading(true);
    try {
      for (const p of providers) {
        await api.updateProviderConfig(p.id, {
          avgLatencyMs: p.code === 'PROVIDER_A' ? 180 : p.code === 'PROVIDER_B' ? 140 : 300,
          successRate: 98.0,
          failureRate: 1.0,
          timeoutRate: 1.0,
          priorityWeight: p.code === 'PROVIDER_A' ? 100 : p.code === 'PROVIDER_B' ? 90 : 80,
          status: 'ACTIVE',
        });
      }
      setStatusMessage('💚 All gateways restored to peak performance (<200ms latency, 98% success rate, CB: CLOSED).');
      fetchProviders();
    } finally {
      setLoading(false);
    }
  };

  if (loading && providers.length === 0) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <RefreshCw className="h-8 w-8 animate-spin text-brand-400" />
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl flex items-center space-x-2">
          <Sliders className="h-7 w-7 text-brand-400" />
          <span>Provider Chaos Engineering & Resilience Lab</span>
        </h1>
        <p className="text-sm text-slate-400 mt-1">
          Dynamically tune provider latency distributions, failure probabilities, and test automated failover and Resilience4j circuit breakers in real time.
        </p>
      </div>

      {/* Preset Disaster Scenarios */}
      <div className="glass-panel rounded-2xl p-6 border-slate-800 space-y-4">
        <h2 className="text-sm font-bold uppercase tracking-wider text-slate-300 flex items-center space-x-2">
          <Zap className="h-4 w-4 text-amber-400" />
          <span>Chaos Engineering Quick Scenarios</span>
        </h2>

        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <button
            onClick={handleTripApexPay}
            className="flex items-center space-x-3 rounded-xl border border-rose-500/30 bg-rose-950/20 p-4 text-left transition-all hover:bg-rose-950/40 hover:border-rose-500/60 group"
          >
            <div className="rounded-lg bg-rose-500/20 p-2 text-rose-400 group-hover:scale-110 transition-transform">
              <Flame className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-white">Trip ApexPay Circuit Breaker</div>
              <div className="text-[11px] text-rose-300/80 mt-0.5">
                Set 90% failure rate to trip breaker OPEN
              </div>
            </div>
          </button>

          <button
            onClick={handleSpikeNovaPay}
            className="flex items-center space-x-3 rounded-xl border border-amber-500/30 bg-amber-950/20 p-4 text-left transition-all hover:bg-amber-950/40 hover:border-amber-500/60 group"
          >
            <div className="rounded-lg bg-amber-500/20 p-2 text-amber-400 group-hover:scale-110 transition-transform">
              <Turtle className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-white">Degrade NovaPay Latency</div>
              <div className="text-[11px] text-amber-300/80 mt-0.5">
                Spike latency to 1850ms to trigger reroute
              </div>
            </div>
          </button>

          <button
            onClick={handleRestoreAll}
            className="flex items-center space-x-3 rounded-xl border border-emerald-500/30 bg-emerald-950/20 p-4 text-left transition-all hover:bg-emerald-950/40 hover:border-emerald-500/60 group"
          >
            <div className="rounded-lg bg-emerald-500/20 p-2 text-emerald-400 group-hover:scale-110 transition-transform">
              <HeartHandshake className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-white">Restore All Gateways</div>
              <div className="text-[11px] text-emerald-300/80 mt-0.5">
                Reset healthy latencies & close breakers
              </div>
            </div>
          </button>
        </div>

        {statusMessage && (
          <div className="rounded-xl border border-brand-500/30 bg-brand-950/20 p-3 text-xs text-brand-300 flex items-center space-x-2">
            <CheckCircle2 className="h-4 w-4 text-brand-400 shrink-0" />
            <span>{statusMessage}</span>
          </div>
        )}
      </div>

      {/* Interactive Provider Cards & Tuning Sliders */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        {providers.map((p) => {
          const isSaving = savingId === p.id;
          const isClosed = p.circuitBreakerState === 'CLOSED' || !p.circuitBreakerState;

          return (
            <div
              key={p.id}
              className="glass-panel rounded-2xl p-6 border-slate-800 space-y-5 flex flex-col justify-between"
            >
              <div className="space-y-4">
                {/* Provider Header */}
                <div className="flex items-center justify-between">
                  <div>
                    <span className="font-mono text-[10px] font-bold text-slate-500">{p.code}</span>
                    <h3 className="text-lg font-bold text-white">{p.name}</h3>
                  </div>

                  <div className="flex items-center space-x-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-[10px] font-bold uppercase ${
                        isClosed ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'
                      }`}
                    >
                      {p.circuitBreakerState || 'CLOSED'}
                    </span>
                  </div>
                </div>

                {/* Status Toggle */}
                <div className="flex items-center justify-between rounded-xl bg-slate-900/60 p-2.5 text-xs">
                  <span className="text-slate-400 font-medium">Gateway Availability:</span>
                  <button
                    type="button"
                    onClick={() =>
                      handleFieldChange(p.id, 'status', p.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')
                    }
                    className={`rounded-lg px-2.5 py-1 font-bold text-[11px] transition-colors ${
                      p.status === 'ACTIVE'
                        ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
                    }`}
                  >
                    {p.status}
                  </button>
                </div>

                {/* Avg Latency Slider */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">Simulated Latency:</span>
                    <span className="font-mono font-bold text-white">{p.avgLatencyMs} ms</span>
                  </div>
                  <input
                    type="range"
                    min="10"
                    max="2000"
                    step="10"
                    value={p.avgLatencyMs}
                    onChange={(e) =>
                      handleFieldChange(p.id, 'avgLatencyMs', parseInt(e.target.value))
                    }
                    className="w-full accent-brand-500 cursor-pointer"
                  />
                  <div className="flex justify-between text-[10px] text-slate-500 font-mono">
                    <span>10ms (Fast)</span>
                    <span>2000ms (Timeout)</span>
                  </div>
                </div>

                {/* Success Rate Slider */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">Base Success Rate:</span>
                    <span className="font-mono font-bold text-emerald-400">{p.successRate}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    value={p.successRate}
                    onChange={(e) =>
                      handleFieldChange(p.id, 'successRate', parseFloat(e.target.value))
                    }
                    className="w-full accent-emerald-500 cursor-pointer"
                  />
                </div>

                {/* Failure Rate Slider */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">Simulated Failure Rate:</span>
                    <span className="font-mono font-bold text-rose-400">{p.failureRate}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    value={p.failureRate}
                    onChange={(e) =>
                      handleFieldChange(p.id, 'failureRate', parseFloat(e.target.value))
                    }
                    className="w-full accent-rose-500 cursor-pointer"
                  />
                </div>

                {/* Timeout Rate Slider */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">Simulated Timeout Rate:</span>
                    <span className="font-mono font-bold text-amber-400">{p.timeoutRate}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="50"
                    step="1"
                    value={p.timeoutRate}
                    onChange={(e) =>
                      handleFieldChange(p.id, 'timeoutRate', parseFloat(e.target.value))
                    }
                    className="w-full accent-amber-500 cursor-pointer"
                  />
                </div>

                {/* Priority Weight */}
                <div className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-400 font-medium">Priority Weight:</span>
                    <span className="font-mono font-bold text-brand-400">{p.priorityWeight}</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="100"
                    step="5"
                    value={p.priorityWeight}
                    onChange={(e) =>
                      handleFieldChange(p.id, 'priorityWeight', parseInt(e.target.value))
                    }
                    className="w-full accent-brand-500 cursor-pointer"
                  />
                </div>
              </div>

              {/* Save Button */}
              <div className="pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => handleSaveConfig(p)}
                  disabled={isSaving}
                  className="flex w-full items-center justify-center space-x-2 rounded-xl bg-slate-800 py-2.5 text-xs font-bold text-white hover:bg-brand-600 transition-colors disabled:opacity-50"
                >
                  {isSaving ? (
                    <RefreshCw className="h-4 w-4 animate-spin text-brand-400" />
                  ) : (
                    <Save className="h-4 w-4" />
                  )}
                  <span>Apply Simulation Parameters</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

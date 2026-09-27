'use client';

import React from 'react';
import {
  X,
  Cpu,
  Database,
  Layers,
  Zap,
  Server,
  Activity,
  ShieldCheck,
  RotateCcw,
  CheckCircle2
} from 'lucide-react';

interface ArchitectureModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ArchitectureModal({ isOpen, onClose }: ArchitectureModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md p-4 animate-fadeIn">
      <div className="relative max-h-[90vh] w-full max-w-4xl overflow-y-auto rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-2xl space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div>
            <span className="text-[10px] font-mono font-bold uppercase tracking-wider text-brand-400">
              System Design & Topology
            </span>
            <h2 className="text-xl font-black text-white mt-0.5">
              PayRoute Architecture & Reliability Guarantees
            </h2>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg bg-slate-800 p-2 text-slate-400 hover:text-white transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Visual Pipeline Diagram */}
        <div className="glass-panel rounded-xl p-5 border-slate-800 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-300">
            End-to-End Payment Orchestration Pipeline
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-5 gap-2 text-center text-xs">
            <div className="rounded-xl border border-slate-800 bg-slate-950 p-3 flex flex-col items-center justify-center space-y-1.5">
              <div className="rounded-lg bg-brand-500/20 p-2 text-brand-400">
                <Zap className="h-4 w-4" />
              </div>
              <span className="font-bold text-white">1. Client Ingress</span>
              <span className="text-[10px] text-slate-400 font-mono">Next.js Dashboard</span>
            </div>

            <div className="rounded-xl border border-slate-800 bg-slate-950 p-3 flex flex-col items-center justify-center space-y-1.5">
              <div className="rounded-lg bg-cyan-500/20 p-2 text-cyan-400">
                <ShieldCheck className="h-4 w-4" />
              </div>
              <span className="font-bold text-white">2. Rate Limit & Lock</span>
              <span className="text-[10px] text-slate-400 font-mono">Redis & Postgres</span>
            </div>

            <div className="rounded-xl border border-slate-800 bg-slate-950 p-3 flex flex-col items-center justify-center space-y-1.5">
              <div className="rounded-lg bg-indigo-500/20 p-2 text-indigo-400">
                <Cpu className="h-4 w-4" />
              </div>
              <span className="font-bold text-white">3. Intelligent Routing</span>
              <span className="text-[10px] text-slate-400 font-mono">Haskell Pure Rules</span>
            </div>

            <div className="rounded-xl border border-slate-800 bg-slate-950 p-3 flex flex-col items-center justify-center space-y-1.5">
              <div className="rounded-lg bg-amber-500/20 p-2 text-amber-400">
                <Activity className="h-4 w-4" />
              </div>
              <span className="font-bold text-white">4. Gateway Failover</span>
              <span className="text-[10px] text-slate-400 font-mono">Resilience4j CB</span>
            </div>

            <div className="rounded-xl border border-slate-800 bg-slate-950 p-3 flex flex-col items-center justify-center space-y-1.5">
              <div className="rounded-lg bg-emerald-500/20 p-2 text-emerald-400">
                <Database className="h-4 w-4" />
              </div>
              <span className="font-bold text-white">5. Settle & Publish</span>
              <span className="text-[10px] text-slate-400 font-mono">Kafka Event Bus</span>
            </div>
          </div>
        </div>

        {/* Feature Highlights Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="glass-panel rounded-xl p-4 border-slate-800 space-y-2">
            <div className="flex items-center space-x-2 text-brand-400 font-bold">
              <Cpu className="h-4 w-4" />
              <span>Haskell Pure Functional Rules Engine</span>
            </div>
            <p className="text-slate-400 leading-relaxed">
              Standalone microservice on port 8081. Evaluates routing decisions through algebraic data types (ADTs) and immutable pure functions. Scores candidates on a multi-factor weighted equation: 45% Success Rate + 30% Latency Subscore + 15% Priority Weight + 10% Reliability with circuit breaker and method affinity multipliers.
            </p>
          </div>

          <div className="glass-panel rounded-xl p-4 border-slate-800 space-y-2">
            <div className="flex items-center space-x-2 text-cyan-400 font-bold">
              <ShieldCheck className="h-4 w-4" />
              <span>ACID Idempotency & Concurrency Safety</span>
            </div>
            <p className="text-slate-400 leading-relaxed">
              Strict exactly-once processing guarantees using PostgreSQL unique constraints (`merchant_id`, `idempotency_key`) and `REQUIRES_NEW` transaction propagation. Replays cached responses without invoking payment gateways twice.
            </p>
          </div>

          <div className="glass-panel rounded-xl p-4 border-slate-800 space-y-2">
            <div className="flex items-center space-x-2 text-amber-400 font-bold">
              <Activity className="h-4 w-4" />
              <span>Resilience4j Circuit Breakers & Dynamic Fallback</span>
            </div>
            <p className="text-slate-400 leading-relaxed">
              Maintains per-provider sliding failure windows. When an upstream gateway reaches 50% failures, the circuit transitions from CLOSED to OPEN, preventing cascading brownouts and instantly rerouting traffic to healthy alternative providers.
            </p>
          </div>

          <div className="glass-panel rounded-xl p-4 border-slate-800 space-y-2">
            <div className="flex items-center space-x-2 text-emerald-400 font-bold">
              <Layers className="h-4 w-4" />
              <span>State Machine Audit & Kafka Event Bus</span>
            </div>
            <p className="text-slate-400 leading-relaxed">
              Every transition (`CREATED` → `PROCESSING` → `SUCCESS` / `FAILED` → `REFUND_PENDING` → `REFUNDED`) is strictly validated against invalid transitions, recorded in the audit event log, and asynchronously published to Kafka topic `payment.events`.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end pt-2">
          <button
            onClick={onClose}
            className="rounded-xl bg-slate-800 px-5 py-2 text-xs font-bold text-white hover:bg-slate-700 transition-colors"
          >
            Close Blueprint
          </button>
        </div>
      </div>
    </div>
  );
}

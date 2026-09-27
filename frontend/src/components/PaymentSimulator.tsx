'use client';

import React, { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { Currency, PaymentMethod, PaymentProvider, Payment } from '@/types';
import {
  CreditCard,
  Smartphone,
  Building2,
  Wallet,
  Zap,
  RefreshCw,
  Copy,
  Check,
  ShieldCheck,
  ArrowRight,
  Sparkles,
  AlertCircle,
  Repeat,
  Layers,
  Clock,
  CheckCircle2,
  XCircle
} from 'lucide-react';

interface PaymentSimulatorProps {
  onPaymentCompleted: (payment: Payment) => void;
}

export default function PaymentSimulator({ onPaymentCompleted }: PaymentSimulatorProps) {
  const [amount, setAmount] = useState<number>(125.00);
  const [currency, setCurrency] = useState<Currency>('USD');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CARD');
  const [customerEmail, setCustomerEmail] = useState<string>('sarah.connor@cyberdyne.io');
  const [description, setDescription] = useState<string>('DevOps Platform License');
  const [idempotencyKey, setIdempotencyKey] = useState<string>('');
  const [isCopied, setIsCopied] = useState(false);

  // Simulation execution states
  const [providers, setProviders] = useState<PaymentProvider[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [activeStep, setActiveStep] = useState<number>(0);
  const [lastPaymentResult, setLastPaymentResult] = useState<{
    payment: Payment;
    isReplayed: boolean;
  } | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Generate new idempotency key
  const generateNewIdempotencyKey = () => {
    const key = `idem-${Math.random().toString(36).substring(2, 9)}-${Date.now().toString(36)}`;
    setIdempotencyKey(key);
  };

  useEffect(() => {
    generateNewIdempotencyKey();
    api.getProviders().then(setProviders).catch(console.error);
  }, []);

  const copyIdempotencyKey = () => {
    navigator.clipboard.writeText(idempotencyKey);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  };

  // Pure mathematical score calculation matching Haskell engine
  const calculateCandidateScore = (p: PaymentProvider) => {
    const cbMult = p.circuitBreakerState === 'OPEN' ? 0.0 : p.circuitBreakerState === 'HALF_OPEN' ? 0.4 : 1.0;
    const latScore = p.avgLatencyMs <= 100 ? 100 : p.avgLatencyMs >= 2000 ? 0 : Math.max(0, 100 - (p.avgLatencyMs - 100) / 19.0);
    const succScore = Math.max(0, Math.min(100, p.successRate));
    const prioScore = Math.max(0, Math.min(100, p.priorityWeight));

    let methodFactor = 1.0;
    if (paymentMethod === 'UPI' && p.code === 'PROVIDER_A') methodFactor = 1.05;
    if (paymentMethod === 'CARD' && p.code === 'PROVIDER_B') methodFactor = 1.05;

    const rawScore = (succScore * 0.45) + (latScore * 0.30) + (prioScore * 0.15) + ((100.0 - p.timeoutRate) * 0.10);
    const finalScore = rawScore * cbMult * methodFactor;

    return {
      rawScore: Math.round(rawScore * 10) / 10,
      finalScore: Math.round(finalScore * 10) / 10,
      latScore: Math.round(latScore),
      succScore: Math.round(succScore),
      prioScore: Math.round(prioScore),
      cbMult,
      methodFactor,
    };
  };

  // Sort providers based on real-time calculated score
  const rankedCandidates = [...providers].map((p) => ({
    provider: p,
    scoring: calculateCandidateScore(p),
  })).sort((a, b) => b.scoring.finalScore - a.scoring.finalScore);

  const handleSubmit = async (isReplayTest: boolean = false) => {
    setIsSubmitting(true);
    setErrorMsg(null);
    setActiveStep(1);

    try {
      // Step 1: Routing Engine pure evaluation
      await new Promise((r) => setTimeout(r, 300));
      setActiveStep(2);

      // Step 2: ACID Locking & Idempotency check
      await new Promise((r) => setTimeout(r, 300));
      setActiveStep(3);

      // Step 3: Gateway execution & Fallback chain
      const result = await api.createPayment(
        {
          amount: Number(amount),
          currency,
          paymentMethod,
          customerEmail,
          description,
          clientReferenceId: `REF-${Math.floor(10000 + Math.random() * 90000)}`,
        },
        idempotencyKey
      );

      setActiveStep(4);
      setLastPaymentResult(result);
      onPaymentCompleted(result.payment);

      // If it wasn't an intentional duplicate replay test, prepare a new key for next time
      if (!isReplayTest && !result.isReplayed) {
        // Keep key for a moment so user can test duplicate replay if desired
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Payment processing failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl flex items-center space-x-2">
          <Zap className="h-7 w-7 text-brand-400" />
          <span>Interactive Payment & Intelligent Routing Terminal</span>
        </h1>
        <p className="text-sm text-slate-400 mt-1">
          Create live transactions, inspect Haskell pure rules evaluation, test ACID idempotency, and watch multi-gateway failover in action.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        {/* Left Column: Payment Form Terminal (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          <div className="glass-panel rounded-2xl p-6 border-slate-800 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800/80">
              <h2 className="text-base font-bold text-white flex items-center space-x-2">
                <CreditCard className="h-4 w-4 text-brand-400" />
                <span>Payment Parameters</span>
              </h2>
              <span className="rounded-full bg-brand-500/10 px-2.5 py-0.5 text-[10px] font-bold text-brand-400 ring-1 ring-brand-500/20">
                SANDBOX SIMULATION
              </span>
            </div>

            {/* Amount & Currency */}
            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2">
                <label className="text-xs font-semibold text-slate-300">Transaction Amount</label>
                <div className="relative mt-1.5 rounded-xl border border-slate-700 bg-slate-900 px-3 py-2.5 shadow-inner focus-within:border-brand-500">
                  <span className="absolute left-3 top-2.5 text-sm font-bold text-slate-500">$</span>
                  <input
                    type="number"
                    min="1"
                    step="0.01"
                    value={amount}
                    onChange={(e) => setAmount(parseFloat(e.target.value) || 0)}
                    className="w-full bg-transparent pl-5 text-base font-bold text-white outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300">Currency</label>
                <select
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value as Currency)}
                  className="mt-1.5 w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-3 text-xs font-bold text-white outline-none focus:border-brand-500"
                >
                  <option value="USD">USD ($)</option>
                  <option value="EUR">EUR (€)</option>
                  <option value="GBP">GBP (£)</option>
                  <option value="INR">INR (₹)</option>
                </select>
              </div>
            </div>

            {/* Payment Method Selector */}
            <div>
              <label className="text-xs font-semibold text-slate-300">Payment Method</label>
              <div className="mt-2 grid grid-cols-2 gap-2">
                {[
                  { id: 'CARD', label: 'Credit/Debit Card', icon: CreditCard },
                  { id: 'UPI', label: 'UPI / Instant QR', icon: Smartphone },
                  { id: 'NET_BANKING', label: 'Net Banking', icon: Building2 },
                  { id: 'WALLET', label: 'Digital Wallet', icon: Wallet },
                ].map((item) => {
                  const Icon = item.icon;
                  const isSelected = paymentMethod === item.id;
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => setPaymentMethod(item.id as PaymentMethod)}
                      className={`flex items-center space-x-2.5 rounded-xl p-3 text-xs font-semibold transition-all border ${
                        isSelected
                          ? 'border-brand-500 bg-brand-500/10 text-white shadow-md shadow-brand-500/10'
                          : 'border-slate-800 bg-slate-900/60 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                      }`}
                    >
                      <Icon className={`h-4 w-4 ${isSelected ? 'text-brand-400' : 'text-slate-500'}`} />
                      <span>{item.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Customer Details */}
            <div className="space-y-3">
              <div>
                <label className="text-xs font-semibold text-slate-300">Customer Email</label>
                <input
                  type="email"
                  value={customerEmail}
                  onChange={(e) => setCustomerEmail(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-xs text-white outline-none focus:border-brand-500"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-300">Order Description</label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-xs text-white outline-none focus:border-brand-500"
                />
              </div>
            </div>

            {/* Idempotency Key Section */}
            <div className="rounded-xl border border-brand-500/20 bg-brand-950/20 p-3.5 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-brand-300 flex items-center space-x-1.5">
                  <ShieldCheck className="h-4 w-4 text-brand-400" />
                  <span>Idempotency-Key Header</span>
                </span>
                <button
                  type="button"
                  onClick={generateNewIdempotencyKey}
                  className="flex items-center space-x-1 text-[11px] text-brand-400 hover:text-brand-300 font-semibold"
                >
                  <RefreshCw className="h-3 w-3" />
                  <span>Generate New</span>
                </button>
              </div>

              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  value={idempotencyKey}
                  onChange={(e) => setIdempotencyKey(e.target.value)}
                  className="w-full rounded-lg border border-slate-700 bg-slate-900 px-2.5 py-1.5 font-mono text-xs text-slate-200 outline-none focus:border-brand-500"
                />
                <button
                  type="button"
                  onClick={copyIdempotencyKey}
                  className="rounded-lg border border-slate-700 bg-slate-800 p-1.5 text-slate-300 hover:text-white"
                  title="Copy Key"
                >
                  {isCopied ? <Check className="h-4 w-4 text-emerald-400" /> : <Copy className="h-4 w-4" />}
                </button>
              </div>
              <p className="text-[10px] text-slate-400">
                Guaranteeing exactly-once ACID execution via PostgreSQL row-level locks and Redis TTL cache.
              </p>
            </div>

            {/* Action Buttons: Regular Execute + Double-Click Replay Test */}
            <div className="pt-2 space-y-2.5">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => handleSubmit(false)}
                className="flex w-full items-center justify-center space-x-2 rounded-xl bg-gradient-to-r from-brand-600 via-indigo-600 to-cyan-500 py-3 text-sm font-bold text-white shadow-lg shadow-brand-500/25 hover:opacity-95 transition-all disabled:opacity-50"
              >
                {isSubmitting ? (
                  <>
                    <RefreshCw className="h-4 w-4 animate-spin" />
                    <span>Orchestrating Route...</span>
                  </>
                ) : (
                  <>
                    <Zap className="h-4 w-4" />
                    <span>Execute Payment Orchestration</span>
                  </>
                )}
              </button>

              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => handleSubmit(true)}
                className="flex w-full items-center justify-center space-x-2 rounded-xl border border-cyan-500/40 bg-cyan-950/20 py-2.5 text-xs font-semibold text-cyan-300 hover:bg-cyan-900/30 transition-all shadow-sm"
                title="Send duplicate request with the identical Idempotency-Key to test replay protection"
              >
                <Repeat className="h-3.5 w-3.5 text-cyan-400" />
                <span>Simulate Double-Click (Test Idempotency Replay)</span>
              </button>
            </div>

            {errorMsg && (
              <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-xs text-rose-300 flex items-start space-x-2">
                <AlertCircle className="h-4 w-4 text-rose-400 shrink-0 mt-0.5" />
                <span>{errorMsg}</span>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Intelligent Routing Visualizer & Execution Trace (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          {/* Live Heuristic / Haskell Scoring Engine Card */}
          <div className="glass-panel rounded-2xl p-6 border-slate-800 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800/80">
              <div>
                <h2 className="text-base font-bold text-white flex items-center space-x-2">
                  <Sparkles className="h-4 w-4 text-cyan-400" />
                  <span>Real-time Intelligent Routing Engine Evaluation</span>
                </h2>
                <p className="text-xs text-slate-400">
                  Pure ADT function: Evaluates success rates, latencies, circuit breakers, and method affinity factors.
                </p>
              </div>
              <span className="rounded-md bg-indigo-500/10 px-2 py-0.5 text-[10px] font-mono text-indigo-400 border border-indigo-500/20">
                Scotty Engine (Port 8081)
              </span>
            </div>

            {/* Provider Candidates Ranking */}
            <div className="space-y-3">
              {rankedCandidates.map((cand, index) => {
                const p = cand.provider;
                const s = cand.scoring;
                const isPrimary = index === 0 && s.finalScore > 0;
                const isOpen = p.circuitBreakerState === 'OPEN';

                return (
                  <div
                    key={p.code}
                    className={`rounded-xl p-4 transition-all border ${
                      isPrimary
                        ? 'border-brand-500/60 bg-brand-950/20 ring-1 ring-brand-500/30 shadow-lg shadow-brand-500/10'
                        : isOpen
                        ? 'border-rose-900/40 bg-rose-950/10 opacity-70'
                        : 'border-slate-800 bg-slate-900/50'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-3">
                        <div
                          className={`flex h-8 w-8 items-center justify-center rounded-lg font-mono text-xs font-bold ${
                            isPrimary
                              ? 'bg-brand-500 text-white'
                              : 'bg-slate-800 text-slate-400'
                          }`}
                        >
                          #{index + 1}
                        </div>
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-bold text-white text-sm">{p.name}</span>
                            <span className="font-mono text-[10px] text-slate-400">({p.code})</span>
                          </div>
                          <div className="text-[11px] text-slate-400 mt-0.5">
                            Success: <span className="text-slate-200">{s.succScore}%</span> • Latency:{' '}
                            <span className="text-slate-200">{p.avgLatencyMs}ms</span> • CB:{' '}
                            <span className={isOpen ? 'text-rose-400 font-bold' : 'text-emerald-400 font-bold'}>
                              {p.circuitBreakerState || 'CLOSED'}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="text-right">
                        <div className="text-xs font-mono font-bold text-slate-400">Composite Score</div>
                        <div
                          className={`text-xl font-black ${
                            isPrimary ? 'text-brand-400' : isOpen ? 'text-rose-400 line-through' : 'text-slate-300'
                          }`}
                        >
                          {s.finalScore.toFixed(1)}
                        </div>
                        {isPrimary && (
                          <span className="inline-block rounded bg-emerald-500/10 px-1.5 py-0.5 text-[9px] font-bold text-emerald-400 uppercase tracking-wider">
                            Primary Route
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Formula Breakdown Tags */}
                    <div className="mt-3 flex flex-wrap gap-1.5 border-t border-slate-800/60 pt-2 text-[10px] font-mono">
                      <span className="rounded bg-slate-800/80 px-2 py-0.5 text-slate-300">
                        Success Factor: (45% × {s.succScore})
                      </span>
                      <span className="rounded bg-slate-800/80 px-2 py-0.5 text-slate-300">
                        Latency Factor: (30% × {s.latScore})
                      </span>
                      <span className="rounded bg-slate-800/80 px-2 py-0.5 text-slate-300">
                        Priority: (15% × {s.prioScore})
                      </span>
                      {s.methodFactor > 1.0 && (
                        <span className="rounded bg-cyan-500/10 px-2 py-0.5 text-cyan-300 font-semibold">
                          Affinity Bonus: ×{s.methodFactor} ({paymentMethod})
                        </span>
                      )}
                      {s.cbMult < 1.0 && (
                        <span className="rounded bg-rose-500/10 px-2 py-0.5 text-rose-300 font-semibold">
                          CB Penalty: ×{s.cbMult}
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Orchestration Execution Stepper */}
          {isSubmitting && (
            <div className="glass-panel rounded-2xl p-6 border-brand-500/40 bg-brand-950/20 space-y-4 animate-pulse">
              <h3 className="text-sm font-bold text-brand-300 flex items-center space-x-2">
                <RefreshCw className="h-4 w-4 animate-spin text-brand-400" />
                <span>Live Orchestration Pipeline Active</span>
              </h3>
              <div className="space-y-2 text-xs">
                <div className={`flex items-center space-x-2 ${activeStep >= 1 ? 'text-emerald-400' : 'text-slate-500'}`}>
                  <CheckCircle2 className="h-4 w-4" />
                  <span>1. Haskell Scotty evaluating pure rule tree against candidate gateways</span>
                </div>
                <div className={`flex items-center space-x-2 ${activeStep >= 2 ? 'text-emerald-400' : 'text-slate-500'}`}>
                  <CheckCircle2 className="h-4 w-4" />
                  <span>2. PostgreSQL acquiring row-level lock on Idempotency record</span>
                </div>
                <div className={`flex items-center space-x-2 ${activeStep >= 3 ? 'text-emerald-400' : 'text-slate-500'}`}>
                  <CheckCircle2 className="h-4 w-4" />
                  <span>3. Invoking primary gateway with Resilience4j circuit breaker tracking</span>
                </div>
                <div className={`flex items-center space-x-2 ${activeStep >= 4 ? 'text-emerald-400' : 'text-slate-500'}`}>
                  <CheckCircle2 className="h-4 w-4" />
                  <span>4. Publishing event to Kafka topic `payment.events` and committing state</span>
                </div>
              </div>
            </div>
          )}

          {/* Payment Result Card */}
          {lastPaymentResult && (
            <div className="glass-panel rounded-2xl p-6 border-emerald-500/30 bg-emerald-950/10 space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2.5">
                  <div
                    className={`rounded-xl p-2 ${
                      lastPaymentResult.payment.status === 'SUCCESS'
                        ? 'bg-emerald-500/20 text-emerald-400'
                        : 'bg-rose-500/20 text-rose-400'
                    }`}
                  >
                    {lastPaymentResult.payment.status === 'SUCCESS' ? (
                      <CheckCircle2 className="h-6 w-6" />
                    ) : (
                      <XCircle className="h-6 w-6" />
                    )}
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-white">
                      Payment {lastPaymentResult.payment.status}
                    </h3>
                    <p className="text-xs text-slate-400 font-mono">
                      Txn: {lastPaymentResult.payment.id}
                    </p>
                  </div>
                </div>

                {lastPaymentResult.isReplayed && (
                  <span className="rounded-full bg-cyan-500/20 border border-cyan-500/40 px-3 py-1 text-xs font-bold text-cyan-300 flex items-center space-x-1">
                    <Repeat className="h-3.5 w-3.5" />
                    <span>Idempotent Cache Replay</span>
                  </span>
                )}
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 border-t border-slate-800/80 pt-4 text-xs">
                <div>
                  <span className="text-slate-500">Gateway Settled</span>
                  <div className="font-semibold text-white mt-0.5">
                    {lastPaymentResult.payment.selectedProviderName || 'None'}
                  </div>
                </div>
                <div>
                  <span className="text-slate-500">Attempts Taken</span>
                  <div className="font-semibold text-white mt-0.5">
                    {lastPaymentResult.payment.attemptsCount} hop(s)
                  </div>
                </div>
                <div>
                  <span className="text-slate-500">Amount Charged</span>
                  <div className="font-semibold text-white mt-0.5">
                    {lastPaymentResult.payment.currency} {lastPaymentResult.payment.amount.toFixed(2)}
                  </div>
                </div>
                <div>
                  <span className="text-slate-500">Client Reference</span>
                  <div className="font-mono text-white mt-0.5">
                    {lastPaymentResult.payment.clientReferenceId || 'N/A'}
                  </div>
                </div>
              </div>

              {lastPaymentResult.isReplayed && (
                <div className="rounded-lg bg-cyan-950/30 border border-cyan-800/40 p-2.5 text-xs text-cyan-300">
                  ⚡ <strong>Notice:</strong> This response was replayed directly from the PostgreSQL idempotency record without executing duplicate charges or calling the provider again.
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

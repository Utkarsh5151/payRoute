'use client';

import React, { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { Payment, PaymentAttempt, PaymentEvent, PaymentStatus } from '@/types';
import {
  Layers,
  Search,
  Filter,
  RefreshCw,
  Clock,
  ArrowRight,
  ShieldAlert,
  CheckCircle2,
  XCircle,
  RotateCcw,
  ExternalLink,
  ChevronRight,
  X,
  Copy,
  Check,
  AlertTriangle,
  Code
} from 'lucide-react';

interface TransactionsViewProps {
  selectedPayment: Payment | null;
  onCloseDetail: () => void;
  onSelectPayment: (p: Payment) => void;
}

export default function TransactionsView({
  selectedPayment,
  onCloseDetail,
  onSelectPayment,
}: TransactionsViewProps) {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Detail view states
  const [attempts, setAttempts] = useState<PaymentAttempt[]>([]);
  const [events, setEvents] = useState<PaymentEvent[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [refundReason, setRefundReason] = useState('Customer return request');
  const [refundAmount, setRefundAmount] = useState<number>(0);
  const [isRefunding, setIsRefunding] = useState(false);
  const [refundSuccess, setRefundSuccess] = useState(false);
  const [copiedField, setCopiedField] = useState<string | null>(null);

  const fetchPayments = async () => {
    setLoading(true);
    try {
      const res = await api.getPayments({
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
      });
      setPayments(res.content || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [statusFilter]);

  // Load attempts & events whenever a payment is selected
  useEffect(() => {
    if (selectedPayment) {
      setDetailLoading(true);
      setRefundAmount(selectedPayment.amount);
      setRefundSuccess(false);

      Promise.all([
        api.getPaymentAttempts(selectedPayment.id).catch(() => []),
        api.getPaymentEvents(selectedPayment.id).catch(() => []),
      ]).then(([attData, evData]) => {
        setAttempts(attData);
        setEvents(evData);
        setDetailLoading(false);
      });
    }
  }, [selectedPayment]);

  const handleCopy = (text: string, field: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(field);
    setTimeout(() => setCopiedField(null), 2000);
  };

  const handleRefund = async () => {
    if (!selectedPayment) return;
    setIsRefunding(true);
    try {
      await api.refundPayment(selectedPayment.id, refundAmount, refundReason);
      setRefundSuccess(true);
      fetchPayments();
      // Update local object
      selectedPayment.status = 'REFUNDED';
    } catch (err: any) {
      alert('Refund failed: ' + (err.message || 'Unknown error'));
    } finally {
      setIsRefunding(false);
    }
  };

  const filteredPayments = payments.filter((p) => {
    const matchesSearch =
      searchTerm === '' ||
      p.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (p.clientReferenceId && p.clientReferenceId.toLowerCase().includes(searchTerm.toLowerCase())) ||
      p.customerEmail.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSearch;
  });

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white sm:text-3xl flex items-center space-x-2">
            <Layers className="h-7 w-7 text-brand-400" />
            <span>Transactions & Multi-Gateway Audit Ledger</span>
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Complete transaction history with full event sourcing timeline, state transitions, and provider attempt logs.
          </p>
        </div>

        <button
          onClick={fetchPayments}
          className="flex items-center space-x-2 rounded-xl border border-slate-800 bg-slate-900 px-3.5 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-all shadow-sm"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin text-brand-400' : ''}`} />
          <span>Refresh Ledger</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="glass-panel flex flex-col sm:flex-row items-center justify-between gap-3 rounded-2xl p-4 border-slate-800">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search Txn ID, Ref, or Customer..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full rounded-xl border border-slate-700 bg-slate-900 py-2 pl-9 pr-3 text-xs text-white placeholder-slate-500 outline-none focus:border-brand-500"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
          <Filter className="h-3.5 w-3.5 text-slate-500 shrink-0 hidden sm:block" />
          {['ALL', 'SUCCESS', 'FAILED', 'REFUNDED', 'PROCESSING'].map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all whitespace-nowrap ${
                statusFilter === st
                  ? 'bg-brand-500 text-white shadow-sm'
                  : 'bg-slate-900/80 text-slate-400 hover:bg-slate-850 hover:text-slate-200 border border-slate-800'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {/* Ledger Table */}
      <div className="glass-panel rounded-2xl border-slate-800 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-900/60 text-slate-400 uppercase tracking-wider font-semibold">
                <th className="py-3 px-4">Transaction ID / Reference</th>
                <th className="py-3 px-4">Customer Email</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Method</th>
                <th className="py-3 px-4">Provider Settled</th>
                <th className="py-3 px-4">Attempts / Hops</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filteredPayments.map((p) => {
                const isSuccess = p.status === 'SUCCESS';
                const isFailed = p.status === 'FAILED';
                const isRefunded = p.status === 'REFUNDED';
                const isSelected = selectedPayment?.id === p.id;

                return (
                  <tr
                    key={p.id}
                    onClick={() => onSelectPayment(p)}
                    className={`cursor-pointer transition-colors ${
                      isSelected
                        ? 'bg-brand-500/10'
                        : 'hover:bg-slate-850/60'
                    }`}
                  >
                    <td className="py-3.5 px-4 font-mono text-slate-200">
                      <div className="font-semibold text-white">{p.id.substring(0, 13)}...</div>
                      <div className="text-[10px] text-slate-500">{p.clientReferenceId || 'No ref'}</div>
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      <div>{p.customerEmail}</div>
                      <div className="text-[10px] text-slate-500">{p.description}</div>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-white">
                      {p.currency} {p.amount.toFixed(2)}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="rounded-md bg-slate-800 px-2 py-0.5 text-[10px] font-semibold text-slate-300">
                        {p.paymentMethod}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {p.selectedProviderName || p.selectedProviderCode || 'None (Failed)'}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {p.attemptsCount} {p.attemptsCount > 1 ? 'hops' : 'hop'}
                    </td>
                    <td className="py-3.5 px-4">
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
                        {p.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <ChevronRight className="inline-block h-4 w-4 text-slate-500" />
                    </td>
                  </tr>
                );
              })}
              {filteredPayments.length === 0 && (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No transactions found matching the selected filter.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Deep-Dive Slide-Over Modal */}
      {selectedPayment && (
        <div className="fixed inset-0 z-50 flex items-center justify-end bg-black/60 backdrop-blur-sm animate-fadeIn">
          <div className="h-full w-full max-w-2xl bg-slate-900 border-l border-slate-800 p-6 shadow-2xl overflow-y-auto space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div>
                <span className="text-[10px] font-mono font-bold uppercase tracking-wider text-brand-400">
                  Transaction Audit Trail
                </span>
                <h2 className="text-lg font-bold text-white mt-0.5">
                  Payment #{selectedPayment.id.substring(0, 12)}
                </h2>
              </div>
              <button
                onClick={onCloseDetail}
                className="rounded-lg bg-slate-800 p-2 text-slate-400 hover:text-white"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* Quick Metadata Card */}
            <div className="glass-panel rounded-xl p-4 border-slate-800 grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
              <div>
                <span className="text-slate-500">Amount</span>
                <div className="font-bold text-white text-base mt-0.5">
                  {selectedPayment.currency} {selectedPayment.amount.toFixed(2)}
                </div>
              </div>
              <div>
                <span className="text-slate-500">Payment Method</span>
                <div className="font-semibold text-white mt-0.5">{selectedPayment.paymentMethod}</div>
              </div>
              <div>
                <span className="text-slate-500">Status</span>
                <div className="mt-0.5">
                  <span className="font-bold text-emerald-400">{selectedPayment.status}</span>
                </div>
              </div>
              <div>
                <span className="text-slate-500">Customer</span>
                <div className="font-medium text-slate-200 mt-0.5">{selectedPayment.customerEmail}</div>
              </div>
              <div>
                <span className="text-slate-500">Settled Provider</span>
                <div className="font-semibold text-white mt-0.5">
                  {selectedPayment.selectedProviderName || 'None'}
                </div>
              </div>
              <div>
                <span className="text-slate-500">Idempotency Key</span>
                <div className="flex items-center space-x-1 font-mono text-[10px] text-brand-400 mt-0.5 truncate">
                  <span>{selectedPayment.idempotencyKey || 'None'}</span>
                  {selectedPayment.idempotencyKey && (
                    <button
                      onClick={() => handleCopy(selectedPayment.idempotencyKey!, 'idem')}
                      className="text-slate-500 hover:text-white"
                    >
                      {copiedField === 'idem' ? <Check className="h-3 w-3 text-emerald-400" /> : <Copy className="h-3 w-3" />}
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* State Machine Transition Timeline */}
            <div className="space-y-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center space-x-1.5">
                <Clock className="h-3.5 w-3.5 text-brand-400" />
                <span>PostgreSQL State Machine Transition Flow</span>
              </h3>

              <div className="relative pl-6 space-y-4 border-l border-slate-800">
                {events.map((ev, i) => (
                  <div key={ev.id || i} className="relative">
                    <div className="absolute -left-[31px] top-1 flex h-4 w-4 items-center justify-center rounded-full bg-brand-600 ring-4 ring-slate-900">
                      <span className="h-1.5 w-1.5 rounded-full bg-white"></span>
                    </div>
                    <div className="text-xs">
                      <div className="flex items-center space-x-2">
                        <span className="font-mono font-bold text-white">{ev.eventType}</span>
                        {ev.fromStatus && (
                          <span className="text-[10px] text-slate-500">
                            ({ev.fromStatus} → {ev.toStatus})
                          </span>
                        )}
                      </div>
                      <div className="text-[10px] text-slate-500 mt-0.5">
                        {new Date(ev.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Provider Attempt Logs (Failover audit) */}
            <div className="space-y-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center space-x-1.5">
                <Code className="h-3.5 w-3.5 text-cyan-400" />
                <span>Provider Attempt Audit Trail ({attempts.length} attempts)</span>
              </h3>

              <div className="space-y-2.5">
                {attempts.map((att) => {
                  const isAttSuccess = att.status === 'SUCCESS';
                  const isCircuit = att.status === 'CIRCUIT_OPEN';

                  return (
                    <div
                      key={att.id}
                      className="rounded-xl border border-slate-800 bg-slate-950/60 p-3.5 text-xs space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <span className="rounded bg-slate-800 px-1.5 py-0.5 font-mono text-[10px] font-bold text-slate-300">
                            Hop #{att.attemptNumber}
                          </span>
                          <span className="font-bold text-white">{att.providerName}</span>
                          <span className="font-mono text-[10px] text-slate-500">({att.providerCode})</span>
                        </div>

                        <div className="flex items-center space-x-2">
                          <span className="text-[10px] text-slate-400 font-mono">{att.latencyMs}ms</span>
                          <span
                            className={`rounded-full px-2 py-0.5 text-[9px] font-bold uppercase ${
                              isAttSuccess
                                ? 'bg-emerald-500/10 text-emerald-400'
                                : isCircuit
                                ? 'bg-amber-500/10 text-amber-400'
                                : 'bg-rose-500/10 text-rose-400'
                            }`}
                          >
                            {att.status}
                          </span>
                        </div>
                      </div>

                      {att.errorMessage && (
                        <div className="text-[11px] text-rose-400 bg-rose-950/20 p-2 rounded-lg border border-rose-900/30">
                          {att.errorMessage}
                        </div>
                      )}

                      {att.responsePayload && (
                        <pre className="p-2 rounded bg-slate-900 text-[10px] font-mono text-slate-400 overflow-x-auto max-h-24">
                          {att.responsePayload}
                        </pre>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Refund Action Card */}
            {selectedPayment.status === 'SUCCESS' && (
              <div className="glass-panel rounded-xl p-4 border-slate-800 space-y-3">
                <h3 className="text-xs font-bold text-white flex items-center space-x-1.5">
                  <RotateCcw className="h-4 w-4 text-cyan-400" />
                  <span>Initiate Refund Protection</span>
                </h3>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="text-[11px] text-slate-400">Refund Amount</label>
                    <input
                      type="number"
                      max={selectedPayment.amount}
                      value={refundAmount}
                      onChange={(e) => setRefundAmount(parseFloat(e.target.value) || 0)}
                      className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-900 px-2.5 py-1.5 text-xs text-white outline-none focus:border-brand-500"
                    />
                  </div>
                  <div>
                    <label className="text-[11px] text-slate-400">Refund Reason</label>
                    <input
                      type="text"
                      value={refundReason}
                      onChange={(e) => setRefundReason(e.target.value)}
                      className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-900 px-2.5 py-1.5 text-xs text-white outline-none focus:border-brand-500"
                    />
                  </div>
                </div>

                <button
                  onClick={handleRefund}
                  disabled={isRefunding}
                  className="flex w-full items-center justify-center space-x-2 rounded-lg bg-cyan-600 py-2 text-xs font-bold text-white hover:bg-cyan-500 transition-colors disabled:opacity-50"
                >
                  {isRefunding ? (
                    <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <RotateCcw className="h-3.5 w-3.5" />
                  )}
                  <span>Execute Idempotent Refund</span>
                </button>
              </div>
            )}

            {refundSuccess && (
              <div className="rounded-xl border border-emerald-500/30 bg-emerald-950/20 p-3 text-xs text-emerald-300 flex items-center space-x-2">
                <CheckCircle2 className="h-4 w-4 text-emerald-400" />
                <span>Payment successfully marked as REFUNDED.</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

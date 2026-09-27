'use client';

import React, { useState } from 'react';
import Navbar from '@/components/Navbar';
import DashboardView from '@/components/DashboardView';
import PaymentSimulator from '@/components/PaymentSimulator';
import TransactionsView from '@/components/TransactionsView';
import ProviderChaosLab from '@/components/ProviderChaosLab';
import ArchitectureModal from '@/components/ArchitectureModal';
import { Payment } from '@/types';

export default function HomePage() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [architectureOpen, setArchitectureOpen] = useState(false);

  const handleSelectPayment = (payment: Payment) => {
    setSelectedPayment(payment);
    setActiveTab('transactions');
  };

  const handlePaymentCompleted = (payment: Payment) => {
    // Keep user in simulator so they can observe the result card
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      {/* Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenArchitecture={() => setArchitectureOpen(true)}
      />

      {/* Main Content Area */}
      <main className="flex-1 mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {activeTab === 'dashboard' && (
          <DashboardView
            onSelectPayment={handleSelectPayment}
            onNavigateToSimulator={() => setActiveTab('simulator')}
          />
        )}

        {activeTab === 'simulator' && (
          <PaymentSimulator onPaymentCompleted={handlePaymentCompleted} />
        )}

        {activeTab === 'transactions' && (
          <TransactionsView
            selectedPayment={selectedPayment}
            onCloseDetail={() => setSelectedPayment(null)}
            onSelectPayment={(p) => setSelectedPayment(p)}
          />
        )}

        {activeTab === 'chaos' && <ProviderChaosLab />}
      </main>

      {/* System Architecture Blueprint Modal */}
      <ArchitectureModal
        isOpen={architectureOpen}
        onClose={() => setArchitectureOpen(false)}
      />

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-6 text-center text-xs text-slate-500">
        <div className="mx-auto max-w-7xl px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>PayRoute Payment Orchestration & Intelligent Routing Simulation Platform</span>
          <div className="flex items-center space-x-4 text-slate-400">
            <span>Spring Boot 3</span>
            <span>•</span>
            <span>Haskell Scotty</span>
            <span>•</span>
            <span>Next.js 14</span>
            <span>•</span>
            <span>PostgreSQL</span>
            <span>•</span>
            <span>Kafka</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

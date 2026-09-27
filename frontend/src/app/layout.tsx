import './globals.css';
import type { Metadata } from 'next';
import { AuthProvider } from '@/context/AuthContext';

export const metadata: Metadata = {
  title: 'PayRoute — Payment Orchestration & Intelligent Routing Platform',
  description:
    'Educational payment orchestration simulation platform featuring intelligent routing, state machine guarantees, idempotency, provider fallback, circuit breakers, and end-to-end observability.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-slate-950 text-slate-100 antialiased selection:bg-brand-500 selection:text-white">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}

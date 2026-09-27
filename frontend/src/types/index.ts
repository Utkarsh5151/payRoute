export type Role = 'ADMIN' | 'MERCHANT' | 'USER';

export type PaymentStatus = 
  | 'CREATED'
  | 'PROCESSING'
  | 'SUCCESS'
  | 'FAILED'
  | 'REFUND_PENDING'
  | 'REFUNDED';

export type PaymentMethod = 'CARD' | 'UPI' | 'NET_BANKING' | 'WALLET';

export type Currency = 'USD' | 'EUR' | 'GBP' | 'INR';

export type AttemptStatus = 'SUCCESS' | 'FAILURE' | 'TIMEOUT' | 'CIRCUIT_OPEN';

export type CircuitBreakerStatus = 'CLOSED' | 'HALF_OPEN' | 'OPEN';

export interface User {
  id: string;
  username: string;
  email: string;
  role: Role;
  merchantId?: string | null;
}

export interface AuthResponse {
  token: string;
  expiresInSeconds: number;
  user: User;
}

export interface PaymentProvider {
  id: string;
  code: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE';
  successRate: number;
  avgLatencyMs: number;
  timeoutRate: number;
  failureRate: number;
  priorityWeight: number;
  circuitBreakerState?: CircuitBreakerStatus;
}

export interface ProviderStatusCard {
  providerCode: string;
  providerName: string;
  status: string;
  successRate: number;
  avgLatencyMs: number;
  circuitBreakerState: CircuitBreakerStatus;
}

export interface PaymentAttempt {
  id: string;
  paymentId: string;
  providerCode: string;
  providerName: string;
  attemptNumber: number;
  status: AttemptStatus;
  requestPayload?: string;
  responsePayload?: string;
  errorCode?: string;
  errorMessage?: string;
  latencyMs: number;
  startedAt: string;
  completedAt?: string;
}

export interface PaymentEvent {
  id: string;
  paymentId: string;
  eventType: string;
  fromStatus?: PaymentStatus;
  toStatus: PaymentStatus;
  payload?: any;
  createdAt: string;
}

export interface Refund {
  id: string;
  paymentId: string;
  merchantId: string;
  amount: number;
  currency: Currency;
  reason: string;
  status: 'REFUND_PENDING' | 'REFUNDED' | 'REFUND_FAILED';
  createdAt: string;
}

export interface Payment {
  id: string;
  merchantId: string;
  merchantName?: string;
  amount: number;
  currency: Currency;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  customerEmail: string;
  description?: string;
  clientReferenceId?: string;
  idempotencyKey?: string;
  selectedProviderCode?: string;
  selectedProviderName?: string;
  attemptsCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  amount: number;
  currency: Currency;
  paymentMethod: PaymentMethod;
  customerEmail: string;
  description?: string;
  clientReferenceId?: string;
}

export interface DashboardStats {
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  successRatePercentage: number;
  totalVolume: number;
  providerCards: ProviderStatusCard[];
  recentPayments: Payment[];
}

export interface ScoredCandidate {
  provider: PaymentProvider;
  calculatedScore: number;
  scoringFactors: string[];
}

export interface RoutingDecision {
  selectedProviderId: string;
  selectedProviderCode: string;
  selectedProviderName: string;
  calculatedScore: number;
  rankedCandidates: {
    scoredCandidate: PaymentProvider;
    calculatedScore: number;
    scoringFactors: string[];
  }[];
  explanation: string;
  engineSource?: 'HASKELL_RULES_ENGINE' | 'JAVA_HEURISTIC_FALLBACK';
}

export interface UpdateProviderConfigRequest {
  avgLatencyMs?: number;
  successRate?: number;
  timeoutRate?: number;
  failureRate?: number;
  priorityWeight?: number;
  status?: 'ACTIVE' | 'INACTIVE';
}

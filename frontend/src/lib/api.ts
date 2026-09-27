import {
  AuthResponse,
  CreatePaymentRequest,
  DashboardStats,
  Payment,
  PaymentAttempt,
  PaymentEvent,
  PaymentProvider,
  ProviderStatusCard,
  Refund,
  RoutingDecision,
  UpdateProviderConfigRequest,
  User
} from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// Token storage key
const TOKEN_KEY = 'payroute_jwt_token';
const USER_KEY = 'payroute_user_data';

export function getStoredToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null): void {
  if (typeof window === 'undefined') return;
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function getStoredUser(): User | null {
  if (typeof window === 'undefined') return null;
  const data = localStorage.getItem(USER_KEY);
  if (!data) return null;
  try {
    return JSON.parse(data);
  } catch {
    return null;
  }
}

export function setStoredUser(user: User | null): void {
  if (typeof window === 'undefined') return;
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(USER_KEY);
  }
}

// Global flag to track backend connectivity
let isLiveBackendAvailable = true;

// ----------------------------------------------------
// Mock In-Memory Simulation State (for offline showcase)
// ----------------------------------------------------
let mockProviders: PaymentProvider[] = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    code: 'PROVIDER_A',
    name: 'ApexPay Gateway',
    status: 'ACTIVE',
    successRate: 98.0,
    avgLatencyMs: 180,
    timeoutRate: 1.0,
    failureRate: 1.0,
    priorityWeight: 100,
    circuitBreakerState: 'CLOSED',
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    code: 'PROVIDER_B',
    name: 'NovaPay Switch',
    status: 'ACTIVE',
    successRate: 95.0,
    avgLatencyMs: 140,
    timeoutRate: 3.0,
    failureRate: 2.0,
    priorityWeight: 90,
    circuitBreakerState: 'CLOSED',
  },
  {
    id: '33333333-3333-3333-3333-333333333333',
    code: 'PROVIDER_C',
    name: 'PulsePay Connect',
    status: 'ACTIVE',
    successRate: 91.0,
    avgLatencyMs: 320,
    timeoutRate: 5.0,
    failureRate: 4.0,
    priorityWeight: 80,
    circuitBreakerState: 'CLOSED',
  },
];

let mockPayments: Payment[] = [
  {
    id: '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001',
    merchantId: '8f8b1b22-1d54-47ef-b209-fa936a2824df',
    merchantName: 'Apex Retail Solutions',
    amount: 149.99,
    currency: 'USD',
    paymentMethod: 'CARD',
    status: 'SUCCESS',
    customerEmail: 'alex.smith@example.com',
    description: 'Enterprise Cloud Subscription',
    clientReferenceId: 'ORD-98241',
    idempotencyKey: 'idem-ec2-001',
    selectedProviderCode: 'PROVIDER_B',
    selectedProviderName: 'NovaPay Switch',
    attemptsCount: 1,
    createdAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    updatedAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
  },
  {
    id: '9b0a5f3c-8d2e-4b4f-ac30-2f9e7d5b0002',
    merchantId: '8f8b1b22-1d54-47ef-b209-fa936a2824df',
    merchantName: 'Apex Retail Solutions',
    amount: 49.50,
    currency: 'USD',
    paymentMethod: 'UPI',
    status: 'SUCCESS',
    customerEmail: 'priya.sharma@example.com',
    description: 'Digital License Key Renewal',
    clientReferenceId: 'ORD-98242',
    idempotencyKey: 'idem-ec2-002',
    selectedProviderCode: 'PROVIDER_A',
    selectedProviderName: 'ApexPay Gateway',
    attemptsCount: 1,
    createdAt: new Date(Date.now() - 1000 * 60 * 28).toISOString(),
    updatedAt: new Date(Date.now() - 1000 * 60 * 28).toISOString(),
  },
  {
    id: '7c8e3d1a-6b0c-492d-8a1e-0d7c5b3a0003',
    merchantId: '8f8b1b22-1d54-47ef-b209-fa936a2824df',
    merchantName: 'Apex Retail Solutions',
    amount: 299.00,
    currency: 'USD',
    paymentMethod: 'NET_BANKING',
    status: 'FAILED',
    customerEmail: 'marcus.vance@corp.org',
    description: 'Bulk Hardware Checkout',
    clientReferenceId: 'ORD-98243',
    idempotencyKey: 'idem-ec2-003',
    selectedProviderCode: 'PROVIDER_C',
    selectedProviderName: 'PulsePay Connect',
    attemptsCount: 3,
    createdAt: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
    updatedAt: new Date(Date.now() - 1000 * 60 * 44).toISOString(),
  },
];

let mockAttempts: Record<string, PaymentAttempt[]> = {
  '7c8e3d1a-6b0c-492d-8a1e-0d7c5b3a0003': [
    {
      id: 'att-01',
      paymentId: '7c8e3d1a-6b0c-492d-8a1e-0d7c5b3a0003',
      providerCode: 'PROVIDER_A',
      providerName: 'ApexPay Gateway',
      attemptNumber: 1,
      status: 'TIMEOUT',
      requestPayload: '{"amount":299.0,"method":"NET_BANKING"}',
      responsePayload: '{"error":"GATEWAY_TIMEOUT"}',
      errorCode: 'TIMEOUT',
      errorMessage: 'Socket read timeout after 1500ms',
      latencyMs: 1512,
      startedAt: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
      completedAt: new Date(Date.now() - 1000 * 60 * 44 - 500).toISOString(),
    },
    {
      id: 'att-02',
      paymentId: '7c8e3d1a-6b0c-492d-8a1e-0d7c5b3a0003',
      providerCode: 'PROVIDER_B',
      providerName: 'NovaPay Switch',
      attemptNumber: 2,
      status: 'FAILURE',
      requestPayload: '{"amount":299.0,"method":"NET_BANKING"}',
      responsePayload: '{"error":"DECLINED_ISSUER_UNAVAILABLE"}',
      errorCode: 'DECLINED',
      errorMessage: 'Customer bank network unavailable',
      latencyMs: 310,
      startedAt: new Date(Date.now() - 1000 * 60 * 44 - 400).toISOString(),
      completedAt: new Date(Date.now() - 1000 * 60 * 44 - 100).toISOString(),
    },
    {
      id: 'att-03',
      paymentId: '7c8e3d1a-6b0c-492d-8a1e-0d7c5b3a0003',
      providerCode: 'PROVIDER_C',
      providerName: 'PulsePay Connect',
      attemptNumber: 3,
      status: 'FAILURE',
      requestPayload: '{"amount":299.0,"method":"NET_BANKING"}',
      responsePayload: '{"error":"RISK_CHECK_FAILED"}',
      errorCode: 'DECLINED_RISK',
      errorMessage: 'Transaction flagged by internal risk engine',
      latencyMs: 245,
      startedAt: new Date(Date.now() - 1000 * 60 * 44).toISOString(),
      completedAt: new Date(Date.now() - 1000 * 60 * 43).toISOString(),
    }
  ],
  '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001': [
    {
      id: 'att-10',
      paymentId: '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001',
      providerCode: 'PROVIDER_B',
      providerName: 'NovaPay Switch',
      attemptNumber: 1,
      status: 'SUCCESS',
      requestPayload: '{"amount":149.99,"method":"CARD"}',
      responsePayload: '{"status":"APPROVED","txId":"TXN-NOV-874291"}',
      latencyMs: 142,
      startedAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
      completedAt: new Date(Date.now() - 1000 * 60 * 12 + 142).toISOString(),
    }
  ],
  '9b0a5f3c-8d2e-4b4f-ac30-2f9e7d5b0002': [
    {
      id: 'att-20',
      paymentId: '9b0a5f3c-8d2e-4b4f-ac30-2f9e7d5b0002',
      providerCode: 'PROVIDER_A',
      providerName: 'ApexPay Gateway',
      attemptNumber: 1,
      status: 'SUCCESS',
      requestPayload: '{"amount":49.50,"method":"UPI"}',
      responsePayload: '{"status":"APPROVED","txId":"TXN-APX-331902"}',
      latencyMs: 165,
      startedAt: new Date(Date.now() - 1000 * 60 * 28).toISOString(),
      completedAt: new Date(Date.now() - 1000 * 60 * 28 + 165).toISOString(),
    }
  ]
};

let mockEvents: Record<string, PaymentEvent[]> = {
  '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001': [
    {
      id: 'ev-1',
      paymentId: '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001',
      eventType: 'PAYMENT_CREATED',
      toStatus: 'CREATED',
      createdAt: new Date(Date.now() - 1000 * 60 * 12 - 300).toISOString(),
    },
    {
      id: 'ev-2',
      paymentId: '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001',
      eventType: 'PAYMENT_PROCESSING',
      fromStatus: 'CREATED',
      toStatus: 'PROCESSING',
      createdAt: new Date(Date.now() - 1000 * 60 * 12 - 200).toISOString(),
    },
    {
      id: 'ev-3',
      paymentId: '8a9f4e2b-7c1d-4a3e-9b2f-1e8d6c4a0001',
      eventType: 'PAYMENT_SUCCEEDED',
      fromStatus: 'PROCESSING',
      toStatus: 'SUCCESS',
      createdAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    },
  ]
};

const mockIdempotencyCache = new Map<string, Payment>();

// ----------------------------------------------------
// Core Fetch Wrapper with Auth & Fallback
// ----------------------------------------------------
async function request<T>(
  endpoint: string,
  options: RequestInit & { idempotencyKey?: string } = {}
): Promise<{ data: T; replayed?: boolean }> {
  const token = getStoredToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (options.idempotencyKey) {
    headers['Idempotency-Key'] = options.idempotencyKey;
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (res.ok) {
      isLiveBackendAvailable = true;
      const replayed = res.headers.get('X-Idempotency-Replayed') === 'true';
      const json = await res.json();
      return { data: json.data !== undefined ? json.data : json, replayed };
    }

    if (res.status >= 400 && res.status < 500) {
      const err = await res.json().catch(() => ({ message: 'Bad request' }));
      throw new Error(err.message || err.error || `HTTP error ${res.status}`);
    }

    throw new Error(`Server returned error ${res.status}`);
  } catch (error: any) {
    // If connection refused or server offline, fall back to realistic mock engine
    if (
      error.message?.includes('Failed to fetch') ||
      error.message?.includes('NetworkError') ||
      error.message?.includes('ECONNREFUSED')
    ) {
      isLiveBackendAvailable = false;
      return handleMockFallback<T>(endpoint, options);
    }
    throw error;
  }
}

// ----------------------------------------------------
// Mock Fallback Engine (Simulates Haskell / State Machine)
// ----------------------------------------------------
function handleMockFallback<T>(
  endpoint: string,
  options: RequestInit & { idempotencyKey?: string }
): { data: T; replayed?: boolean } {
  const method = options.method || 'GET';

  // 1. Dashboard Stats
  if (endpoint.startsWith('/dashboard/stats')) {
    const total = mockPayments.length;
    const successful = mockPayments.filter((p) => p.status === 'SUCCESS').length;
    const failed = mockPayments.filter((p) => p.status === 'FAILED').length;
    const pending = mockPayments.filter((p) => p.status === 'PROCESSING').length;
    const volume = mockPayments
      .filter((p) => p.status === 'SUCCESS')
      .reduce((acc, p) => acc + p.amount, 0);

    const providerCards: ProviderStatusCard[] = mockProviders.map((p) => ({
      providerCode: p.code,
      providerName: p.name,
      status: p.status,
      successRate: p.successRate,
      avgLatencyMs: p.avgLatencyMs,
      circuitBreakerState: p.circuitBreakerState || 'CLOSED',
    }));

    const stats: DashboardStats = {
      totalPayments: total,
      successfulPayments: successful,
      failedPayments: failed,
      pendingPayments: pending,
      successRatePercentage: total > 0 ? Math.round((successful / total) * 1000) / 10 : 100,
      totalVolume: volume,
      providerCards,
      recentPayments: mockPayments.slice(0, 10),
    };
    return { data: stats as unknown as T };
  }

  // 2. Providers List
  if (endpoint === '/providers' && method === 'GET') {
    return { data: mockProviders as unknown as T };
  }

  // 3. Provider Config Patch
  if (endpoint.startsWith('/providers/') && endpoint.endsWith('/config') && method === 'PATCH') {
    const parts = endpoint.split('/');
    const providerId = parts[2];
    const body: UpdateProviderConfigRequest = options.body ? JSON.parse(options.body as string) : {};
    
    const p = mockProviders.find((prov) => prov.id === providerId || prov.code === providerId);
    if (p) {
      if (body.avgLatencyMs !== undefined) p.avgLatencyMs = body.avgLatencyMs;
      if (body.successRate !== undefined) p.successRate = body.successRate;
      if (body.failureRate !== undefined) p.failureRate = body.failureRate;
      if (body.timeoutRate !== undefined) p.timeoutRate = body.timeoutRate;
      if (body.priorityWeight !== undefined) p.priorityWeight = body.priorityWeight;
      if (body.status !== undefined) p.status = body.status;

      // Dynamic Circuit Breaker simulation
      if (p.failureRate >= 60 || p.status === 'INACTIVE') {
        p.circuitBreakerState = 'OPEN';
      } else if (p.failureRate >= 30) {
        p.circuitBreakerState = 'HALF_OPEN';
      } else {
        p.circuitBreakerState = 'CLOSED';
      }
      return { data: p as unknown as T };
    }
  }

  // 4. Payments Listing
  if (endpoint.startsWith('/payments') && method === 'GET') {
    if (endpoint.includes('/attempts')) {
      const match = endpoint.match(/\/payments\/([^/]+)\/attempts/);
      const id = match ? match[1] : '';
      return { data: (mockAttempts[id] || []) as unknown as T };
    }
    if (endpoint.includes('/events')) {
      const match = endpoint.match(/\/payments\/([^/]+)\/events/);
      const id = match ? match[1] : '';
      return { data: (mockEvents[id] || []) as unknown as T };
    }
    return {
      data: {
        content: mockPayments,
        totalElements: mockPayments.length,
        totalPages: 1,
        pageNumber: 0,
        pageSize: 10,
        last: true,
      } as unknown as T,
    };
  }

  // 5. Create Payment (Simulate Haskell Pure Routing & Fallback Execution)
  if (endpoint === '/payments' && method === 'POST') {
    const body: CreatePaymentRequest = JSON.parse(options.body as string);
    const idempotencyKey = options.idempotencyKey;

    // ACID Idempotency Check
    if (idempotencyKey && mockIdempotencyCache.has(idempotencyKey)) {
      const cached = mockIdempotencyCache.get(idempotencyKey)!;
      return { data: cached as unknown as T, replayed: true };
    }

    // Evaluate Routing (Haskell Pure Scoring formula)
    const scoredCandidates = mockProviders.map((p) => {
      const cbMult = p.circuitBreakerState === 'OPEN' ? 0.0 : p.circuitBreakerState === 'HALF_OPEN' ? 0.4 : 1.0;
      const latScore = p.avgLatencyMs <= 100 ? 100 : p.avgLatencyMs >= 2000 ? 0 : Math.max(0, 100 - (p.avgLatencyMs - 100) / 19.0);
      const succScore = Math.max(0, Math.min(100, p.successRate));
      const prioScore = Math.max(0, Math.min(100, p.priorityWeight));
      
      let methodFactor = 1.0;
      if (body.paymentMethod === 'UPI' && p.code === 'PROVIDER_A') methodFactor = 1.05;
      if (body.paymentMethod === 'CARD' && p.code === 'PROVIDER_B') methodFactor = 1.05;

      const rawScore = (succScore * 0.45) + (latScore * 0.30) + (prioScore * 0.15) + ((100.0 - p.timeoutRate) * 0.10);
      const finalScore = rawScore * cbMult * methodFactor;

      return {
        provider: p,
        calculatedScore: Math.round(finalScore * 10) / 10,
        scoringFactors: [
          `Base Success: ${succScore}%`,
          `Latency: ${p.avgLatencyMs}ms (Subscore: ${Math.round(latScore)})`,
          `Circuit Breaker: ${p.circuitBreakerState || 'CLOSED'}`,
          `Method Affinity: x${methodFactor}`,
        ],
      };
    });

    // Rank candidates by calculated score descending
    scoredCandidates.sort((a, b) => b.calculatedScore - a.calculatedScore);

    const paymentId = 'pay-' + Math.random().toString(36).substring(2, 10) + '-' + Date.now().toString(36);
    const createdAttempts: PaymentAttempt[] = [];
    let chosenProvider = scoredCandidates[0]?.provider || mockProviders[0];
    let terminalStatus: 'SUCCESS' | 'FAILED' = 'SUCCESS';

    // Simulate Fallback Chain
    let seq = 1;
    for (const cand of scoredCandidates) {
      const p = cand.provider;
      if (p.circuitBreakerState === 'OPEN') {
        createdAttempts.push({
          id: `att-${paymentId}-${seq}`,
          paymentId,
          providerCode: p.code,
          providerName: p.name,
          attemptNumber: seq++,
          status: 'CIRCUIT_OPEN',
          errorCode: 'CIRCUIT_BREAKER_OPEN',
          errorMessage: 'Resilience4j rejected call: Provider circuit breaker is OPEN',
          latencyMs: 0,
          startedAt: new Date().toISOString(),
          completedAt: new Date().toISOString(),
        });
        continue;
      }

      // Roll probability for failure / timeout based on candidate config
      const roll = Math.random() * 100;
      if (roll < p.timeoutRate) {
        createdAttempts.push({
          id: `att-${paymentId}-${seq}`,
          paymentId,
          providerCode: p.code,
          providerName: p.name,
          attemptNumber: seq++,
          status: 'TIMEOUT',
          errorCode: 'TIMEOUT',
          errorMessage: 'Gateway timed out waiting for issuer confirmation',
          latencyMs: p.avgLatencyMs + 500,
          startedAt: new Date().toISOString(),
          completedAt: new Date().toISOString(),
        });
        continue;
      }

      if (roll < p.timeoutRate + p.failureRate) {
        createdAttempts.push({
          id: `att-${paymentId}-${seq}`,
          paymentId,
          providerCode: p.code,
          providerName: p.name,
          attemptNumber: seq++,
          status: 'FAILURE',
          errorCode: 'DECLINED',
          errorMessage: 'Card issuer declined transaction (simulated failure rate)',
          latencyMs: p.avgLatencyMs,
          startedAt: new Date().toISOString(),
          completedAt: new Date().toISOString(),
        });
        continue;
      }

      // Success with this provider
      chosenProvider = p;
      createdAttempts.push({
        id: `att-${paymentId}-${seq}`,
        paymentId,
        providerCode: p.code,
        providerName: p.name,
        attemptNumber: seq++,
        status: 'SUCCESS',
        latencyMs: p.avgLatencyMs,
        responsePayload: JSON.stringify({ status: 'APPROVED', txnId: `TXN-${p.code}-${Math.floor(100000 + Math.random() * 900000)}` }),
        startedAt: new Date().toISOString(),
        completedAt: new Date().toISOString(),
      });
      terminalStatus = 'SUCCESS';
      break;
    }

    if (!createdAttempts.some((a) => a.status === 'SUCCESS')) {
      terminalStatus = 'FAILED';
    }

    const newPayment: Payment = {
      id: paymentId,
      merchantId: '8f8b1b22-1d54-47ef-b209-fa936a2824df',
      merchantName: 'Apex Retail Solutions',
      amount: body.amount,
      currency: body.currency,
      paymentMethod: body.paymentMethod,
      status: terminalStatus,
      customerEmail: body.customerEmail,
      description: body.description || 'Payment Simulation',
      clientReferenceId: body.clientReferenceId || `REF-${Math.floor(10000 + Math.random() * 90000)}`,
      idempotencyKey,
      selectedProviderCode: chosenProvider.code,
      selectedProviderName: chosenProvider.name,
      attemptsCount: createdAttempts.length,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockPayments.unshift(newPayment);
    mockAttempts[paymentId] = createdAttempts;
    mockEvents[paymentId] = [
      {
        id: `ev-${paymentId}-1`,
        paymentId,
        eventType: 'PAYMENT_CREATED',
        toStatus: 'CREATED',
        createdAt: new Date(Date.now() - 250).toISOString(),
      },
      {
        id: `ev-${paymentId}-2`,
        paymentId,
        eventType: 'PAYMENT_PROCESSING',
        fromStatus: 'CREATED',
        toStatus: 'PROCESSING',
        createdAt: new Date(Date.now() - 100).toISOString(),
      },
      {
        id: `ev-${paymentId}-3`,
        paymentId,
        eventType: terminalStatus === 'SUCCESS' ? 'PAYMENT_SUCCEEDED' : 'PAYMENT_FAILED',
        fromStatus: 'PROCESSING',
        toStatus: terminalStatus,
        createdAt: new Date().toISOString(),
      },
    ];

    if (idempotencyKey) {
      mockIdempotencyCache.set(idempotencyKey, newPayment);
    }

    return { data: newPayment as unknown as T };
  }

  // 6. Refund Payment
  if (endpoint.includes('/refund') && method === 'POST') {
    const match = endpoint.match(/\/payments\/([^/]+)\/refund/);
    const id = match ? match[1] : '';
    const payment = mockPayments.find((p) => p.id === id);
    if (payment) {
      payment.status = 'REFUNDED';
      payment.updatedAt = new Date().toISOString();
      const refund: Refund = {
        id: 'ref-' + Math.random().toString(36).substring(2, 9),
        paymentId: payment.id,
        merchantId: payment.merchantId,
        amount: payment.amount,
        currency: payment.currency,
        reason: 'Requested by merchant',
        status: 'REFUNDED',
        createdAt: new Date().toISOString(),
      };
      return { data: refund as unknown as T };
    }
  }

  return { data: {} as unknown as T };
}

// ----------------------------------------------------
// Public API Methods
// ----------------------------------------------------
export const api = {
  // Authentication
  async login(usernameOrEmail: string, password: string): Promise<AuthResponse> {
    try {
      const res = await request<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ usernameOrEmail, password }),
      });
      setStoredToken(res.data.token);
      setStoredUser(res.data.user);
      return res.data;
    } catch (e: any) {
      // In offline demo mode, recognize default seed credentials
      if (
        (usernameOrEmail === 'admin' || usernameOrEmail === 'admin@payroute.dev') &&
        password === 'Admin@123456'
      ) {
        const dummy: AuthResponse = {
          token: 'mock-admin-jwt-token-payroute',
          expiresInSeconds: 86400,
          user: {
            id: 'a0000000-0000-0000-0000-000000000001',
            username: 'admin',
            email: 'admin@payroute.dev',
            role: 'ADMIN',
          },
        };
        setStoredToken(dummy.token);
        setStoredUser(dummy.user);
        return dummy;
      }
      if (
        (usernameOrEmail === 'merchant_apex' || usernameOrEmail === 'merchant@apex.dev') &&
        password === 'Merchant@123456'
      ) {
        const dummy: AuthResponse = {
          token: 'mock-merchant-jwt-token-payroute',
          expiresInSeconds: 86400,
          user: {
            id: 'm0000000-0000-0000-0000-000000000001',
            username: 'merchant_apex',
            email: 'merchant@apex.dev',
            role: 'MERCHANT',
            merchantId: '8f8b1b22-1d54-47ef-b209-fa936a2824df',
          },
        };
        setStoredToken(dummy.token);
        setStoredUser(dummy.user);
        return dummy;
      }
      if (
        (usernameOrEmail === 'customer_alice' || usernameOrEmail === 'alice@customer.dev') &&
        password === 'User@123456'
      ) {
        const dummy: AuthResponse = {
          token: 'mock-user-jwt-token-payroute',
          expiresInSeconds: 86400,
          user: {
            id: 'u0000000-0000-0000-0000-000000000001',
            username: 'customer_alice',
            email: 'alice@customer.dev',
            role: 'USER',
          },
        };
        setStoredToken(dummy.token);
        setStoredUser(dummy.user);
        return dummy;
      }
      throw e;
    }
  },

  async getCurrentUser(): Promise<User> {
    const res = await request<User>('/auth/me');
    setStoredUser(res.data);
    return res.data;
  },

  logout(): void {
    setStoredToken(null);
    setStoredUser(null);
  },

  // Observability & Dashboard
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await request<DashboardStats>('/dashboard/stats');
    return res.data;
  },

  // Providers
  async getProviders(): Promise<PaymentProvider[]> {
    const res = await request<PaymentProvider[]>('/providers');
    return res.data;
  },

  async updateProviderConfig(
    providerId: string,
    req: UpdateProviderConfigRequest
  ): Promise<PaymentProvider> {
    const res = await request<PaymentProvider>(`/providers/${providerId}/config`, {
      method: 'PATCH',
      body: JSON.stringify(req),
    });
    return res.data;
  },

  // Payments
  async createPayment(
    paymentRequest: CreatePaymentRequest,
    idempotencyKey?: string
  ): Promise<{ payment: Payment; isReplayed: boolean }> {
    const res = await request<Payment>('/payments', {
      method: 'POST',
      body: JSON.stringify(paymentRequest),
      idempotencyKey,
    });
    return { payment: res.data, isReplayed: !!res.replayed };
  },

  async getPayments(params?: {
    status?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: Payment[]; totalElements: number }> {
    const query = new URLSearchParams();
    if (params?.status) query.set('status', params.status);
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());

    const res = await request<{ content: Payment[]; totalElements: number }>(
      `/payments?${query.toString()}`
    );
    return res.data;
  },

  async getPaymentAttempts(paymentId: string): Promise<PaymentAttempt[]> {
    const res = await request<PaymentAttempt[]>(`/payments/${paymentId}/attempts`);
    return res.data;
  },

  async getPaymentEvents(paymentId: string): Promise<PaymentEvent[]> {
    const res = await request<PaymentEvent[]>(`/payments/${paymentId}/events`);
    return res.data;
  },

  async refundPayment(paymentId: string, amount: number, reason: string): Promise<Refund> {
    const res = await request<Refund>(`/payments/${paymentId}/refund`, {
      method: 'POST',
      body: JSON.stringify({ amount, reason }),
      idempotencyKey: `refund-${paymentId}-${Date.now()}`,
    });
    return res.data;
  },

  isLiveBackend(): boolean {
    return isLiveBackendAvailable;
  },
};

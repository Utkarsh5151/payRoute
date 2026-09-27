# Stage 1: Build Haskell Binary
FROM haskell:9.6-slim AS builder

WORKDIR /opt/build

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgmp-dev \
    zlib1g-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Cache dependencies
COPY haskell-routing-service.cabal ./
RUN cabal update && cabal build --only-dependencies -j4

# Copy source code and build executable
COPY src/ ./src/
COPY app/ ./app/
RUN cabal build -j4 && \
    cp $(cabal list-bin haskell-routing-service-exe) /opt/build/haskell-routing-service-exe

# Stage 2: Minimal Runtime Image
FROM debian:bookworm-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    libgmp10 \
    netbase \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /opt/build/haskell-routing-service-exe /app/haskell-routing-service-exe

EXPOSE 8081

HEALTHCHECK --interval=5s --timeout=3s --retries=5 \
  CMD curl -f http://localhost:8081/health || exit 1

ENTRYPOINT ["/app/haskell-routing-service-exe"]

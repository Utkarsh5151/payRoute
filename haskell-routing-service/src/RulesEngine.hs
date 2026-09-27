{-# LANGUAGE OverloadedStrings #-}

module RulesEngine
    ( evaluateRouting
    , scoreCandidate
    , rankCandidates
    ) where

import Types
import Data.List (sortBy)
import Data.Ord (comparing)
import qualified Data.Text as T

-- | Pure function: Pattern matching on Circuit Breaker state
circuitBreakerMultiplier :: CircuitBreakerState -> Double
circuitBreakerMultiplier CLOSED    = 1.0
circuitBreakerMultiplier HALF_OPEN = 0.4
circuitBreakerMultiplier OPEN      = 0.0

-- | Pure function: Calculate latency subscore (bounded between 0 and 100)
latencySubscore :: Double -> Double
latencySubscore ms
    | ms <= 100.0 = 100.0
    | ms >= 2000.0 = 0.0
    | otherwise   = max 0.0 (100.0 - ((ms - 100.0) / 19.0))

-- | Pure function: Calculate payment method affinity factor
methodAffinityFactor :: PaymentMethod -> Text -> Double
methodAffinityFactor UPI code
    | "PROVIDER_A" `T.isPrefixOf` code = 1.05
    | otherwise                         = 1.00
methodAffinityFactor CARD code
    | "PROVIDER_B" `T.isPrefixOf` code = 1.05
    | otherwise                         = 1.00
methodAffinityFactor _ _               = 1.00

-- | Pure function: Computes normalized score for a single candidate provider
-- Demonstrates function composition, pattern matching, and immutability
scoreCandidate :: PaymentMethod -> Double -> ProviderCandidate -> ScoredProvider
scoreCandidate method amt cand =
    let cbMult       = circuitBreakerMultiplier (circuitBreakerState cand)
        latScore     = latencySubscore (latencyMs cand)
        succScore    = max 0.0 (min 100.0 (successRate cand))
        prioScore    = fromIntegral (max 0 (min 100 (priorityWeight cand)))
        methodFactor = methodAffinityFactor method (providerCode cand)

        -- Multi-factor weighted composition:
        -- Success Rate (45%) + Latency (30%) + Priority (15%) + Reliability (10%)
        rawScore     = (succScore * 0.45) + (latScore * 0.30) + (prioScore * 0.15) + ((100.0 - timeoutRate cand) * 0.10)
        finalScore   = rawScore * cbMult * methodFactor

        explanationParts =
            [ "Base Success Rate: " <> T.pack (show (round succScore :: Int)) <> "%"
            , "Latency: " <> T.pack (show (round (latencyMs cand) :: Int)) <> "ms (Subscore: " <> T.pack (show (round latScore :: Int)) <> ")"
            , "Circuit Breaker: " <> T.pack (show (circuitBreakerState cand))
            , "Method Affinity Factor: " <> T.pack (show methodFactor)
            ]
    in ScoredProvider
        { scoredCandidate = cand
        , calculatedScore = finalScore
        , scoringFactors  = explanationParts
        }

-- | Higher-order function: Filters and ranks candidates using sortBy and function composition
rankCandidates :: PaymentMethod -> Double -> [ProviderCandidate] -> [ScoredProvider]
rankCandidates method amt =
    sortBy (flip (comparing calculatedScore)) . map (scoreCandidate method amt)

-- | Main Pure Rules Engine Evaluation:
-- Returns Either RoutingError RoutingDecision
evaluateRouting :: RoutingRequest -> Either RoutingError RoutingDecision
evaluateRouting req
    | null (candidates req) =
        Left (RoutingError "No provider candidates provided for routing decision")
    | otherwise =
        let ranked = rankCandidates (paymentMethod req) (amount req) (candidates req)
            activeCandidates = filter (\sp -> calculatedScore sp > 0.0) ranked
        in case activeCandidates of
            [] ->
                Left (RoutingError "All available payment providers are currently unavailable or circuit breakers are OPEN")
            (top:_) ->
                let winner = scoredCandidate top
                    expl = "Selected " <> providerName winner <> " (" <> providerCode winner <> ") with highest composite score of "
                           <> T.pack (show (round (calculatedScore top) :: Int)) <> ". Factors: "
                           <> T.intercalate "; " (scoringFactors top)
                in Right RoutingDecision
                    { selectedProviderId   = providerId winner
                    , selectedProviderCode = providerCode winner
                    , selectedProviderName = providerName winner
                    , calculatedScore      = calculatedScore top
                    , rankedCandidates     = ranked
                    , explanation          = expl
                    }

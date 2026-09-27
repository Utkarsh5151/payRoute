{-# LANGUAGE DeriveGeneric #-}
{-# LANGUAGE OverloadedStrings #-}

module Types where

import Data.Aeson
import Data.Text (Text)
import GHC.Generics

-- | Algebraic Data Type for Payment Methods
data PaymentMethod = CARD | UPI | NET_BANKING | WALLET
    deriving (Show, Eq, Generic)

instance FromJSON PaymentMethod
instance ToJSON PaymentMethod

-- | Algebraic Data Type for Circuit Breaker State
data CircuitBreakerState = CLOSED | HALF_OPEN | OPEN
    deriving (Show, Eq, Generic)

instance FromJSON CircuitBreakerState
instance ToJSON CircuitBreakerState

-- | Candidate Provider Health & Performance metrics
data ProviderCandidate = ProviderCandidate
    { providerId          :: !Text
    , providerCode        :: !Text
    , providerName        :: !Text
    , successRate         :: !Double  -- e.g. 98.5
    , latencyMs           :: !Double  -- e.g. 180.0
    , timeoutRate         :: !Double  -- e.g. 1.5
    , failureRate         :: !Double  -- e.g. 1.0
    , circuitBreakerState :: !CircuitBreakerState
    , priorityWeight      :: !Int
    } deriving (Show, Eq, Generic)

instance FromJSON ProviderCandidate
instance ToJSON ProviderCandidate

-- | Inbound Routing Request
data RoutingRequest = RoutingRequest
    { amount        :: !Double
    , currency      :: !Text
    , paymentMethod :: !PaymentMethod
    , candidates    :: ![ProviderCandidate]
    } deriving (Show, Eq, Generic)

instance FromJSON RoutingRequest
instance ToJSON RoutingRequest

-- | Scored Provider result
data ScoredProvider = ScoredProvider
    { scoredCandidate :: !ProviderCandidate
    , calculatedScore :: !Double
    , scoringFactors  :: ![Text]
    } deriving (Show, Eq, Generic)

instance FromJSON ScoredProvider
instance ToJSON ScoredProvider

-- | Final Routing Decision
data RoutingDecision = RoutingDecision
    { selectedProviderId   :: !Text
    , selectedProviderCode :: !Text
    , selectedProviderName :: !Text
    , calculatedScore      :: !Double
    , rankedCandidates     :: ![ScoredProvider]
    , explanation          :: !Text
    } deriving (Show, Eq, Generic)

instance FromJSON RoutingDecision
instance ToJSON RoutingDecision

-- | Error response
data RoutingError = RoutingError
    { errorMessage :: !Text
    } deriving (Show, Eq, Generic)

instance FromJSON RoutingError
instance ToJSON RoutingError

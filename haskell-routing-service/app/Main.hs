{-# LANGUAGE OverloadedStrings #-}

module Main where

import Web.Scotty
import Network.HTTP.Types.Status (status200, status422)
import Network.Wai.Middleware.Cors
import Data.Aeson (object, (.=))
import Types
import RulesEngine

main :: IO ()
main = scotty 8081 $ do
    middleware simpleCors

    get "/health" $ do
        status status200
        json $ object
            [ "status" .= ("UP" :: String)
            , "service" .= ("haskell-routing-rules" :: String)
            , "version" .= ("1.0.0" :: String)
            ]

    post "/route" $ do
        req <- jsonData :: ActionM RoutingRequest
        case evaluateRouting req of
            Right decision -> do
                status status200
                json decision
            Left err -> do
                status status422
                json err

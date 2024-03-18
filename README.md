> ⚠️ Documentation is still WIP. Expect more updates around May.

# Paddy Auth

This is the authentication/authorization component for Paddy, the Power Administration Daemon.

It uses [Quarkus, the Supersonic Subatomic Java Framework](https://quarkus.io/) running Java 17, is written in Kotlin, and is deployed in Docker.

The job of this application is to issue & verify JWTs for all who want to access the Paddy API, be it HTTP or MQTT. It does not support HTTPS, as normally this runs inside a VPC.

## Issuing
This app exposes an API to issue JWTs on-demand. It can generate short-expiration JWTs for clients, or long-expiration ones for Daemons.

## Verification

### HTTP Authentication

1. Signature is verified.
2. JWT Expiration is checked.

### HTTP Authorization

No Authorization for HTTP is done on this application. That is rather done on the Paddy Backend app.

### MQTT Authentication

A JWKS (JSON Web Key Set) is exposed in an API, and ingested by the MQTT Broker. This key is used to verify incoming usernames, which should be set to a valid JWT for anyone who wants to connect to the broker.

1. Signature is verified.
2. JWT Expiration is checked.

### MQTT Authorization

MQTT Authorization is performed by checking what action the client wants to perform. The scope of what a client can do is limited to the "sub" claim. For instance, if your sub claim was `17`, you can only connect to the broker if:

1. You have a valid JWT (see above).
2. You wish to publish/subscribe to a topic that begins with `daemon/17`. So, `daemon/17/hello` and `daemon/17/the/answer/is/42` are valid, but `daemonn/17` and `daemon/18/malicious/attack` are not. This is done to prevent eavesdropping on other daemons' messages or disrupting their flow.
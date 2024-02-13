package online.danielstefani.paddy.authorization.dto

/*
This payload will be sent by EMQX to verify
whether an actor has access to a topic.
This step happens after the actor's authenticity has been verified.

https://www.emqx.io/docs/en/latest/access-control/authz/http.html
*/
data class AuthorizationRequestDto(
    val username: String, // Expected to be a JWT
    val topic: String     // Topic that client wants to access
)

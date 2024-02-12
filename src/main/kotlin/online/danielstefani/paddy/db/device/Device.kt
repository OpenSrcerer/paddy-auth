package online.danielstefani.paddy.db.device

import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity
class Device(
    @Id val serial: String,
    var jwt: String
)
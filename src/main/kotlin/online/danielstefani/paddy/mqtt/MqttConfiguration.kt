package online.danielstefani.paddy.mqtt

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "mqtt")
interface MqttConfiguration {

    @WithDefault("localhost")
    fun host(): String

    @WithDefault("1883")
    fun port(): Int

    @WithDefault("paddy-auth")
    fun clientId(): String

    @WithDefault("daemon/+/v1/reads")
    fun deviceReadTopic(): String
}
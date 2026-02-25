package network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual fun httpClientEngine(): HttpClientEngine {
    return OkHttp.create()
}

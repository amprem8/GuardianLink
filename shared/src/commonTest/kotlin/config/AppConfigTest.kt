package config

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AppConfigTest {

    @Test
    fun `BASE_URL is not blank`() {
        assertTrue(AppConfig.BASE_URL.isNotBlank())
    }

    @Test
    fun `BASE_URL uses HTTPS`() {
        assertTrue(AppConfig.BASE_URL.startsWith("https://"), "BASE_URL must use HTTPS")
    }

    @Test
    fun `BASE_URL does not have trailing slash`() {
        assertTrue(!AppConfig.BASE_URL.endsWith("/"), "BASE_URL should not end with /")
    }
}

package push

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import software.amazon.awssdk.services.sns.model.EndpointDisabledException

class SosPushHandlerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `disabled SNS endpoint returns failed push result without corrupting endpoint token`() {
        mockkObject(SnsEndpointRegistry, SnsPushClient)
        try {
            val endpointArn = "arn:aws:sns:ap-south-1:291759414836:endpoint/GCM/resq-android-fcm/test-disabled-endpoint"
            every { SnsEndpointRegistry.getEndpointsByPhone("9751537485") } returns listOf(endpointArn)
            every {
                SnsPushClient.publishSos(
                    endpointArn = endpointArn,
                    title = "SOS Alert",
                    body = "Test Sender might need help",
                    data = any(),
                )
            } throws EndpointDisabledException.builder().message("Endpoint is disabled").build()

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """
                {
                  "victimUserId":"9345771470",
                  "victimName":"Test Sender",
                  "contacts":[
                    {
                      "contactName":"Second User",
                      "phoneNumber":"9751537485",
                      "endpointArn":"",
                      "includeGPS":false
                    }
                  ]
                }
            """.trimIndent()

            val response = SosPushHandler.handle(event)
            assertEquals(200, response.statusCode)

            val body = response.body
            assertNotNull(body)
            val parsed = json.decodeFromString(SosPushHandler.SosPushResponse.serializer(), body)
            assertEquals(0, parsed.sentCount)
            assertEquals(1, parsed.failedCount)
            assertFalse(parsed.allPublished)
            assertEquals(1, parsed.results.size)
            assertFalse(parsed.results.first().published)
            assertTrue(parsed.results.first().deliveryMethod == "PUSH")
            assertTrue(parsed.results.first().error?.contains("Endpoint is disabled") == true)

            verify(exactly = 0) { SnsPushClient.upsertEndpointAttributes(any(), any()) }
        } finally {
            unmockkAll()
        }
    }
}


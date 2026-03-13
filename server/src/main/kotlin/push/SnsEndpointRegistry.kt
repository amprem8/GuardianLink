package push

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest

object SnsEndpointRegistry {

    private val tableName: String =
        System.getenv("PUSH_ENDPOINTS_TABLE").orEmpty().trim().ifEmpty { "PushEndpoints" }

    private val client: DynamoDbClient by lazy {
        val region = System.getenv("AWS_REGION").orEmpty().trim().ifEmpty { "ap-south-1" }
        DynamoDbClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
    }

    fun saveEndpoint(
        phone: String,
        deviceId: String,
        endpointArn: String,
        platform: String,
    ) {
        val now = System.currentTimeMillis().toString()
        val item = mapOf(
            "phone" to AttributeValue.builder().s(phone).build(),
            "deviceId" to AttributeValue.builder().s(deviceId).build(),
            "endpointArn" to AttributeValue.builder().s(endpointArn).build(),
            "platform" to AttributeValue.builder().s(platform).build(),
            "enabled" to AttributeValue.builder().bool(true).build(),
            "updatedAt" to AttributeValue.builder().n(now).build(),
        )

        client.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
        )
    }

    fun getEndpoint(phone: String, deviceId: String): String? {
        val key = mapOf(
            "phone" to AttributeValue.builder().s(phone).build(),
            "deviceId" to AttributeValue.builder().s(deviceId).build(),
        )
        val item = client.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .consistentRead(false)
                .build()
        ).item()

        if (item.isNullOrEmpty()) return null
        return item["endpointArn"]?.s()?.trim()?.ifEmpty { null }
    }

    fun getEndpointsByPhone(phone: String): List<String> {
        val req = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("#phone = :phone")
            .expressionAttributeNames(mapOf("#phone" to "phone"))
            .expressionAttributeValues(
                mapOf(
                    ":phone" to AttributeValue.builder().s(phone).build(),
                )
            )
            .build()

        return client.query(req)
            .items()
            .mapNotNull { row ->
                val isEnabled = row["enabled"]?.bool() ?: false
                val endpoint = row["endpointArn"]?.s()?.trim().orEmpty()
                if (isEnabled && endpoint.isNotEmpty()) endpoint else null
            }
            .distinct()
    }
}


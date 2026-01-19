package db


import models.User
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

object DynamoClient {
    private val client = DynamoDbClient.builder()
        .region(Region.AP_SOUTH_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()

    private const val TABLE_NAME = "Users"

    fun saveUser(user: User) {
        try {
            val item = mapOf(
                "email" to AttributeValue.builder().s(user.email).build(), // Partition key
                "userId" to AttributeValue.builder().s(user.userId).build(),
                "name" to AttributeValue.builder().s(user.name).build(),
                "passwordHash" to AttributeValue.builder().s(user.passwordHash).build(),
                "createdAt" to AttributeValue.builder().n(user.createdAt.toString()).build()
            )
            client.putItem(PutItemRequest.builder().tableName(TABLE_NAME).item(item).build())
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
            throw e
        }
    }


    fun getUser(email: String): User? {
        val key = mapOf("email" to AttributeValue.builder().s(email).build())
        val resp = client.getItem(GetItemRequest.builder().tableName(TABLE_NAME).key(key).build())
        val item = resp.item()
        return if (item.isEmpty()) null else User(
            userId = item["userId"]?.s() ?: "",
            name = item["name"]?.s() ?: "",
            email = item["email"]?.s() ?: "",
            passwordHash = item["passwordHash"]?.s() ?: "",
            createdAt = item["createdAt"]?.n()?.toLong() ?: 0
        )
    }
}

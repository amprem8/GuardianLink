package auth

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import kotlin.random.Random

object OtpService {

    private const val TABLE_NAME = "OtpTable"
    private const val OTP_TTL_SECONDS = 300L // 5 minutes

    private val dynamo = DynamoDbClient.builder()
        .region(Region.AP_SOUTH_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()

    private val sns = SnsClient.builder()
        .region(Region.AP_SOUTH_1)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build()

    /**
     * Generate a random 6-digit OTP, store it in DynamoDB, and send via SNS.
     */
    fun sendOtp(phone: String) {
        val otp = generateOtp()
        storeOtp(phone, otp)
        sendSms(phone, otp)
    }

    /**
     * Verify the OTP entered by the user against the stored value.
     * Deletes the OTP on successful verification.
     */
    fun verifyOtp(phone: String, otp: String): Boolean {
        val stored = getStoredOtp(phone) ?: return false

        // Check expiry
        val ttl = stored["ttl"]?.n()?.toLongOrNull() ?: return false
        if (System.currentTimeMillis() / 1000 > ttl) {
            deleteOtp(phone)
            return false
        }

        val storedOtp = stored["otp"]?.s() ?: return false
        if (storedOtp != otp) return false

        // OTP matched — delete so it can't be reused
        deleteOtp(phone)
        return true
    }

    // ── Private helpers ──

    private fun generateOtp(): String =
        Random.nextInt(100_000, 1_000_000).toString()

    private fun storeOtp(phone: String, otp: String) {
        val ttl = (System.currentTimeMillis() / 1000) + OTP_TTL_SECONDS
        val item = mapOf(
            "phone" to AttributeValue.builder().s(phone).build(),
            "otp"   to AttributeValue.builder().s(otp).build(),
            "ttl"   to AttributeValue.builder().n(ttl.toString()).build()
        )
        dynamo.putItem(
            PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build()
        )
    }

    private fun getStoredOtp(phone: String): Map<String, AttributeValue>? {
        val key = mapOf("phone" to AttributeValue.builder().s(phone).build())
        val resp = dynamo.getItem(
            GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build()
        )
        return if (resp.item().isNullOrEmpty()) null else resp.item()
    }

    private fun deleteOtp(phone: String) {
        val key = mapOf("phone" to AttributeValue.builder().s(phone).build())
        dynamo.deleteItem(
            DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build()
        )
    }

    private fun sendSms(phone: String, otp: String) {
        val message = "Your GuardianLink verification code is $otp. Valid for 5 minutes."

        val smsAttributes = mapOf(
            "AWS.SNS.SMS.SMSType" to MessageAttributeValue.builder()
                .stringValue("Transactional")
                .dataType("String")
                .build()
        )

        sns.publish(
            PublishRequest.builder()
                .phoneNumber(phone)
                .message(message)
                .messageAttributes(smsAttributes)
                .build()
        )
    }
}

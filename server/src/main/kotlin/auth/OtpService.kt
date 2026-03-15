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
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom

object OtpService {

    private val logger = LoggerFactory.getLogger(OtpService::class.java)

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
     * Generate a random 6-digit OTP, store it in DynamoDB (only if no valid OTP
     * already exists for this phone), and send via SNS.
     *
     * If a non-expired OTP is already stored we REUSE it instead of
     * generating a new one. This means a DB refresh / redeploy never
     * invalidates an OTP that was already sent to the user.
     */
    fun sendOtp(phone: String) {
        val nowSeconds = System.currentTimeMillis() / 1000

        // Check if a valid (non-expired) OTP already exists
        val existing = getStoredOtp(phone)
        val existingTtl = existing?.get("ttl")?.n()?.toLongOrNull() ?: 0L
        val existingOtp = existing?.get("otp")?.s()

        val otpToSend = if (existingOtp != null && nowSeconds < existingTtl) {
            // Re-use the existing OTP — do NOT overwrite it
            logger.info("Re-using existing valid OTP for {}", phone)
            existingOtp
        } else {
            // No valid OTP exists — generate and store a new one
            val otp = generateOtp()
            storeOtp(phone, otp)
            otp
        }
        sendSms(phone, otpToSend)
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
        if (!MessageDigest.isEqual(storedOtp.toByteArray(), otp.toByteArray())) return false

        // OTP matched — delete so it can't be reused
        deleteOtp(phone)
        return true
    }

    // ── Private helpers ──

    private val secureRandom = SecureRandom()

    private fun generateOtp(): String =
        (100_000 + secureRandom.nextInt(900_000)).toString()

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
        val message = "Your ResQ verification code is $otp. Valid for 5 minutes."

        val smsAttributes = buildMap {
            put(
                "AWS.SNS.SMS.SMSType",
                MessageAttributeValue.builder()
                    .stringValue("Transactional")
                    .dataType("String")
                    .build()
            )
            put(
                "AWS.SNS.SMS.SenderID",
                MessageAttributeValue.builder()
                    .stringValue("ResQ")
                    .dataType("String")
                    .build()
            )
        }

        logger.info("Sending OTP SMS to {}", phone)

        try {
            val result = sns.publish(
                PublishRequest.builder()
                    .phoneNumber(phone)
                    .message(message)
                    .messageAttributes(smsAttributes)
                    .build()
            )
            logger.info("SNS publish success – messageId={}", result.messageId())
        } catch (e: Exception) {
            logger.error("SNS publish FAILED for {} – {}", phone, e.message, e)
            throw e
        }
    }
}

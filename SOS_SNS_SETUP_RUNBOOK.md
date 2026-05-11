# SOS SNS Setup Runbook (From Scratch)

This runbook is privacy-first and **does not use DynamoDB** for SOS delivery state.

> Android-only mode: You can complete all required setup for Android now and skip iOS/APNs blocks. iOS sections are retained as-is for later.

## What you must finish before starting Step 4

Step 4 in this runbook creates SNS platform applications. Before that, complete these prerequisites:

1. AWS CLI is installed and authenticated to the correct account.
2. You know your Lambda/API identifiers:
   - API ID: `poscdjdphc`
   - Lambda: `guardianlink-api`
   - Lambda role: `guardianlink-lambda-role`
3. Firebase setup is ready for Android push:
   - Firebase project exists
   - Android app added in Firebase
   - Two Firebase files are available:
     - Service account JSON (for AWS SNS platform app credentials)
     - App config `google-services.json` (for device token generation in app)
4. (Optional for Android-only) Apple APNs setup is ready for iOS push:
   - `.p8` key file downloaded
   - APNs Key ID, Team ID, Bundle ID available
5. Local tools available: `aws`, `jq`, `python3`.

## Exact app identifiers for this repo

Use these exact values while configuring Firebase and APNs:

- Android package name (`applicationId`): `com.example.guardianlink`
- iOS bundle identifier (`PRODUCT_BUNDLE_IDENTIFIER`): `com.example.guardianlink.iosApp`

Quick verify from terminal:

```bash
grep -n "applicationId\|namespace" composeApp/build.gradle.kts
grep -n "PRODUCT_BUNDLE_IDENTIFIER" iosApp/iosApp.xcodeproj/project.pbxproj
```

### Mandatory gate before Step 4

If Firebase or APNs setup is incomplete, **do not continue to Step 4**.

Use this quick pre-check:

```bash
test -f "$FCM_SA_JSON_PATH" && echo "FCM service-account JSON found" || echo "FCM service-account JSON missing"
test -f "$APNS_P8_PATH" && echo "APNS .p8 key found" || echo "APNS .p8 key missing"
```

Android-only path: only the FCM JSON check is mandatory.

If you are doing both platforms, both files must exist.

## Firebase setup (must be completed before Step 4)

1. Open Firebase Console: https://console.firebase.google.com/
2. Create/select your Firebase project.
3. Add Android app with package name: `com.example.guardianlink`
4. Open **Project settings -> Service accounts**.
5. Click **Generate new private key** and download the JSON.
6. Save the file locally and note the absolute path (used as `FCM_SA_JSON_PATH`).

7. In Firebase **Project settings -> General -> Your apps -> Android app (`com.example.guardianlink`)**, download the app config file `google-services.json`.
8. Place that app config file at:
   - `composeApp/src/androidMain/assets/google-services.json`

Important: the service-account JSON and `google-services.json` are different files.

### If you are currently on Firebase screen: "Add Firebase SDK"

For this SNS runbook, there are **two valid paths**:

- **Path A (continue AWS setup now, fastest)**: You can proceed without integrating Firebase SDK yet. SNS platform app creation only needs the service-account JSON from Step 5.
- **Path B (already supported in this repo)**: ensure `google-services.json` is placed in `composeApp/src/androidMain/assets/`.

#### Path A: Skip SDK for now

If your immediate goal is AWS SNS setup, click **Next** in Firebase console and continue this runbook from Step 4.

#### Path B: App config placement (from scratch)

```bash
mkdir -p /Users/pavudi605@apac.comcast.com/Documents/composeApp/src/androidMain/assets
cp /ABSOLUTE/PATH/google-services.json /Users/pavudi605@apac.comcast.com/Documents/composeApp/src/androidMain/assets/google-services.json
```

##### Verify Path B

```bash
test -f composeApp/src/androidMain/assets/google-services.json && echo "google-services.json in assets" || echo "google-services.json missing"
jq -r '.project_info.project_id // "MISSING_project_info"' composeApp/src/androidMain/assets/google-services.json
./gradlew :composeApp:compileDebugKotlin
```

If project_id prints correctly and compile succeeds, token generation path is ready.

Optional docs reference:
- https://firebase.google.com/docs/cloud-messaging

## Apple APNs setup (must be completed before Step 4)

1. Open Apple Developer portal: https://developer.apple.com/account/
2. Go to **Certificates, Identifiers & Profiles -> Keys**.
3. Create a key with **Apple Push Notifications service (APNs)** enabled.
4. Download the `.p8` key file (one-time download).
5. Note and store:
   - APNs Key ID
   - Apple Team ID
   - iOS app Bundle ID: `com.example.guardianlink.iosApp`
6. Save the `.p8` path for `APNS_P8_PATH`.

---

## From Scratch: Full Terminal Steps with Verification

## Step 1: Verify local tooling and AWS identity

```bash
aws --version
jq --version
python3 --version
aws sts get-caller-identity
aws configure get region
```

Expected: account should be `291759414836`, region should be `ap-south-1`.

## Step 2: Export session variables

```bash
export AWS_REGION="ap-south-1"
export ACCOUNT_ID="291759414836"
export API_ID="poscdjdphc"
export LAMBDA_NAME="guardianlink-api"
export LAMBDA_ARN="arn:aws:lambda:${AWS_REGION}:${ACCOUNT_ID}:function:${LAMBDA_NAME}"
export ROLE_NAME="guardianlink-lambda-role"
export SNS_ANDROID_APP_NAME="resq-android-fcm"
export SNS_IOS_APP_NAME="resq-ios-apns-prod"
```

Android-only users can keep `SNS_IOS_APP_NAME` exported but skip iOS commands below.

Verify:

```bash
echo "$AWS_REGION"
echo "$LAMBDA_ARN"
echo "$ROLE_NAME"
```

## Step 3: Verify existing API + Lambda baseline

```bash
aws apigatewayv2 get-apis --region "$AWS_REGION" --query "Items[?ApiId=='${API_ID}'].[Name,ApiId,ProtocolType]" --output table
aws apigatewayv2 get-routes --api-id "$API_ID" --region "$AWS_REGION" --output table
aws lambda get-function-configuration --function-name "$LAMBDA_ARN" --region "$AWS_REGION" --query '{FunctionName:FunctionName,Role:Role,LastModified:LastModified}' --output table
```

## Step 4: Create SNS platform applications

> Android-only: proceed when Firebase is ready. You can skip Step 4B until you enroll in Apple Developer Program.

### 4A) Android platform app (FCM v1)

```bash
export FCM_SA_JSON_PATH="$(ls -t /Users/pavudi605@apac.comcast.com/Downloads/*.json | head -n 1)"
export SNS_ANDROID_APP_NAME="resq-android-fcm"
export AWS_REGION="ap-south-1"
```

Verify it resolved to a file:

```bash
echo "$FCM_SA_JSON_PATH"
echo "$SNS_ANDROID_APP_NAME"
echo "$AWS_REGION"
test -f "$FCM_SA_JSON_PATH" && echo "FCM JSON found" || echo "FCM JSON missing"
test -n "$SNS_ANDROID_APP_NAME" && echo "SNS app name set" || echo "SNS app name missing"
```

```bash
export FCM_SA_JSON_MINIFIED="$(python3 - <<'PY'
import json,os
p=os.environ["FCM_SA_JSON_PATH"]
print(json.dumps(json.load(open(p)), separators=(',',':')))
PY
)"
```

Create an attributes file (recommended; avoids CLI parsing errors with JSON commas/quotes):

```bash
jq -n --arg cred "$FCM_SA_JSON_MINIFIED" '{PlatformCredential:$cred}' > /tmp/sns-android-attrs.json
cat /tmp/sns-android-attrs.json | head -c 120 && echo "..."
```

```bash
aws sns create-platform-application \
  --name "$SNS_ANDROID_APP_NAME" \
  --platform GCM \
  --attributes file:///tmp/sns-android-attrs.json \
  --region "$AWS_REGION"
```

### 4B) iOS platform app (APNS token auth)

Optional for Android-only. Skip this block for now.

```bash
export APNS_P8_PATH="/ABSOLUTE/PATH/AuthKey_XXXXXX.p8"
export APNS_KEY_ID="YOUR_KEY_ID"
export APNS_TEAM_ID="YOUR_TEAM_ID"
export APNS_BUNDLE_ID="com.example.guardianlink.iosApp"
```

```bash
export APNS_P8_ESCAPED="$(awk '{printf "%s\\n",$0}' "$APNS_P8_PATH")"
```

```bash
aws sns create-platform-application \
  --name "$SNS_IOS_APP_NAME" \
  --platform APNS \
  --attributes "PlatformPrincipal=$APNS_KEY_ID,PlatformCredential=$APNS_P8_ESCAPED,ApplePlatformTeamID=$APNS_TEAM_ID,ApplePlatformBundleID=$APNS_BUNDLE_ID" \
  --region "$AWS_REGION"
```

Verify Step 4:

```bash
aws sns list-platform-applications --region "$AWS_REGION" --output table
```

## Step 5: Attach SNS-only policy to Lambda role

```bash
cat > /tmp/sns-publish-only-policy.json <<'JSON'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "SnsPublishOnly",
      "Effect": "Allow",
      "Action": [
        "sns:Publish",
        "sns:CreatePlatformEndpoint",
        "sns:SetEndpointAttributes",
        "sns:GetEndpointAttributes"
      ],
      "Resource": "*"
    }
  ]
}
JSON
```

```bash
aws iam put-role-policy \
  --role-name "$ROLE_NAME" \
  --policy-name "SnsPublishOnlyPolicy" \
  --policy-document file:///tmp/sns-publish-only-policy.json
```

Verify:

```bash
aws iam get-role-policy \
  --role-name "$ROLE_NAME" \
  --policy-name "SnsPublishOnlyPolicy" \
  --query 'PolicyDocument.Statement' \
  --output json
```

## Step 6: Capture platform app ARNs and store in Lambda environment

### 6A) Android-only command set (recommended now)

```bash
export SNS_ANDROID_PLATFORM_APP_ARN="$(aws sns list-platform-applications --region "$AWS_REGION" --query "PlatformApplications[?contains(PlatformApplicationArn, '${SNS_ANDROID_APP_NAME}')].PlatformApplicationArn | [0]" --output text)"
echo "$SNS_ANDROID_PLATFORM_APP_ARN"
```

```bash
export CURRENT_ENV_JSON="$(aws lambda get-function-configuration --function-name "$LAMBDA_ARN" --region "$AWS_REGION" --query 'Environment.Variables' --output json)"
export MERGED_ENV_JSON="$(jq -c \
  --arg a "$SNS_ANDROID_PLATFORM_APP_ARN" \
  '. + {"SNS_ANDROID_PLATFORM_APP_ARN":$a}' \
  <<< "$CURRENT_ENV_JSON")"

jq -n --argjson vars "$MERGED_ENV_JSON" '{Variables:$vars}' > /tmp/lambda-env.json
```

```bash
aws lambda update-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --environment file:///tmp/lambda-env.json \
  --region "$AWS_REGION"
```

Verify Android-only env:

```bash
aws lambda get-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --region "$AWS_REGION" \
  --query 'Environment.Variables.{SNS_ANDROID_PLATFORM_APP_ARN:SNS_ANDROID_PLATFORM_APP_ARN}' \
  --output json
```

### 6B) Dual-platform command set (keep for later)

```bash
export SNS_ANDROID_PLATFORM_APP_ARN="$(aws sns list-platform-applications --region "$AWS_REGION" --query "PlatformApplications[?contains(PlatformApplicationArn, '${SNS_ANDROID_APP_NAME}')].PlatformApplicationArn | [0]" --output text)"
export SNS_IOS_PLATFORM_APP_ARN="$(aws sns list-platform-applications --region "$AWS_REGION" --query "PlatformApplications[?contains(PlatformApplicationArn, '${SNS_IOS_APP_NAME}')].PlatformApplicationArn | [0]" --output text)"
```

```bash
echo "$SNS_ANDROID_PLATFORM_APP_ARN"
echo "$SNS_IOS_PLATFORM_APP_ARN"
```

```bash
export CURRENT_ENV_JSON="$(aws lambda get-function-configuration --function-name "$LAMBDA_ARN" --region "$AWS_REGION" --query 'Environment.Variables' --output json)"
export MERGED_ENV_JSON="$(jq -c \
  --arg a "$SNS_ANDROID_PLATFORM_APP_ARN" \
  --arg i "$SNS_IOS_PLATFORM_APP_ARN" \
  '. + {"SNS_ANDROID_PLATFORM_APP_ARN":$a,"SNS_IOS_PLATFORM_APP_ARN":$i}' \
  <<< "$CURRENT_ENV_JSON")"

jq -n --argjson vars "$MERGED_ENV_JSON" '{Variables:$vars}' > /tmp/lambda-env.json
```

```bash
aws lambda update-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --environment file:///tmp/lambda-env.json \
  --region "$AWS_REGION"
```

Verify:

```bash
aws lambda get-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --region "$AWS_REGION" \
  --query 'Environment.Variables.{SNS_ANDROID_PLATFORM_APP_ARN:SNS_ANDROID_PLATFORM_APP_ARN,SNS_IOS_PLATFORM_APP_ARN:SNS_IOS_PLATFORM_APP_ARN}' \
  --output json
```

## Step 7: Create API route `POST /sos/push` using existing Lambda integration

```bash
export EXISTING_INTEGRATION_ID="$(aws apigatewayv2 get-routes \
  --api-id "$API_ID" \
  --region "$AWS_REGION" \
  --query "Items[?RouteKey=='POST /signup'].Target | [0]" \
  --output text | sed 's#integrations/##')"
```

```bash
echo "$EXISTING_INTEGRATION_ID"
```

```bash
aws apigatewayv2 create-route \
  --api-id "$API_ID" \
  --region "$AWS_REGION" \
  --route-key "POST /sos/push" \
  --target "integrations/${EXISTING_INTEGRATION_ID}"
```

Verify:

```bash
aws apigatewayv2 get-routes \
  --api-id "$API_ID" \
  --region "$AWS_REGION" \
  --query "Items[?RouteKey=='POST /sos/push'].[RouteKey,Target]" \
  --output table
```

## Step 8: Add Lambda invoke permission for `/sos/push`

```bash
aws lambda remove-permission \
  --function-name "$LAMBDA_ARN" \
  --statement-id "allow-apigw-sos-push" \
  --region "$AWS_REGION" 2>/dev/null || true
```

```bash
aws lambda add-permission \
  --function-name "$LAMBDA_ARN" \
  --statement-id "allow-apigw-sos-push" \
  --action "lambda:InvokeFunction" \
  --principal "apigateway.amazonaws.com" \
  --source-arn "arn:aws:execute-api:${AWS_REGION}:${ACCOUNT_ID}:${API_ID}/*/POST/sos/push" \
  --region "$AWS_REGION"
```

Verify:

```bash
aws lambda get-policy \
  --function-name "$LAMBDA_ARN" \
  --region "$AWS_REGION" \
  --query 'Policy' \
  --output text | grep -F "/POST/sos/push"
```

## Step 9: Privacy hardening (short log retention)

```bash
aws logs create-log-group --log-group-name "/aws/lambda/${LAMBDA_NAME}" --region "$AWS_REGION" 2>/dev/null || true
aws logs put-retention-policy --log-group-name "/aws/lambda/${LAMBDA_NAME}" --retention-in-days 1 --region "$AWS_REGION"
```

Verify:

```bash
aws logs describe-log-groups \
  --log-group-name-prefix "/aws/lambda/${LAMBDA_NAME}" \
  --region "$AWS_REGION" \
  --query 'logGroups[0].{name:logGroupName,retention:retentionInDays}' \
  --output table
```

## Step 10: Smoke checks

```bash
export BASE_URL="https://${API_ID}.execute-api.${AWS_REGION}.amazonaws.com"
```

```bash
curl -i -X POST "$BASE_URL/voice/presign" \
  -H "Content-Type: application/json" \
  -d '{"username":"debug_user","phrase":"Help Me"}'
```

```bash
curl -i -X POST "$BASE_URL/sos/push" \
  -H "Content-Type: application/json" \
  -d '{"ping":"infra-check"}'
```

## Step 11: Real-time Android test for mobile number `9345771470`

This section tests end-to-end push and the UI timer-stop behavior.

### 11A) Get FCM token from contact phone (9345771470 device)

The app already logs token while running and on token refresh.

Read token from terminal:

```bash
adb logcat | grep FCM_TOKEN
```

If you see `FirebaseApp not initialized` or `No value for project_info`, you copied the wrong file.
Use app config `google-services.json` from Firebase General tab (not `firebase-adminsdk-*.json`).

### 11B) Create SNS endpoint for that token

```bash
export AWS_REGION="ap-south-1"
export SNS_ANDROID_PLATFORM_APP_ARN="arn:aws:sns:ap-south-1:291759414836:app/GCM/resq-android-fcm"
export FCM_TOKEN="PASTE_REAL_FCM_TOKEN_FROM_9345771470_DEVICE"
```

```bash
ENDPOINT_ARN_9345="$(aws sns create-platform-endpoint \
  --platform-application-arn "$SNS_ANDROID_PLATFORM_APP_ARN" \
  --token "$FCM_TOKEN" \
  --region "$AWS_REGION" \
  --query 'EndpointArn' \
  --output text)"

echo "$ENDPOINT_ARN_9345"
```

Verify endpoint enabled:

```bash
aws sns get-endpoint-attributes \
  --endpoint-arn "$ENDPOINT_ARN_9345" \
  --region "$AWS_REGION"
```

### 11C) Map phone number -> endpoint ARN in Lambda env

```bash
export LAMBDA_ARN="arn:aws:lambda:ap-south-1:291759414836:function:guardianlink-api"

CURRENT_ENV_JSON="$(aws lambda get-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --region "$AWS_REGION" \
  --query 'Environment.Variables' \
  --output json)"

MERGED_ENV_JSON="$(jq -c \
  --arg endpoint "$ENDPOINT_ARN_9345" \
  '. + {"SOS_PHONE_ENDPOINT_MAP_JSON":("{\"9345771470\":\""+$endpoint+"\"}")}' \
  <<< "$CURRENT_ENV_JSON")"

jq -n --argjson vars "$MERGED_ENV_JSON" '{Variables:$vars}' > /tmp/lambda-env.json

aws lambda update-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --environment file:///tmp/lambda-env.json \
  --region "$AWS_REGION"
```

Verify mapping:

```bash
aws lambda get-function-configuration \
  --function-name "$LAMBDA_ARN" \
  --region "$AWS_REGION" \
  --query 'Environment.Variables.SOS_PHONE_ENDPOINT_MAP_JSON' \
  --output text
```

To map multiple emergency contacts at once, use one JSON map:

```bash
MERGED_ENV_JSON="$(jq -c \
  --arg e1 "$ENDPOINT_ARN_9345" \
  --arg e2 "arn:aws:sns:ap-south-1:291759414836:endpoint/GCM/resq-android-fcm/REPLACE_2" \
  '. + {"SOS_PHONE_ENDPOINT_MAP_JSON":("{\"9345771470\":\""+$e1+"\",\"9876543210\":\""+$e2+"\"}")}' \
  <<< "$CURRENT_ENV_JSON")"
```

### 11D) Trigger SOS by phone number (no endpoint ARN in payload)

```bash
export BASE_URL="https://poscdjdphc.execute-api.ap-south-1.amazonaws.com"

curl -i -X POST "$BASE_URL/sos/push" \
  -H "Content-Type: application/json" \
  -d '{
    "victimUserId":"u-123",
    "victimName":"Test User",
    "location":{"permissionGranted":true,"gpsEnabled":true,"lat":12.9716,"lng":77.5946},
    "contacts":[
      {"contactName":"Contact 9345","phoneNumber":"9345771470","endpointArn":"","includeGPS":true}
    ]
  }'
```

Expected response:
- `sentCount = 1`
- `failedCount = 0`
- `allPublished = true`

### 11E) UI timer stop behavior (when user clicks Trigger SOS)

With the latest app code:
- On Home screen -> tap **Trigger SOS**.
- App calls backend `/sos/push` from `ActiveSOSActivity`.
- When backend returns `allPublished=true`, UI passes `stopTimer=true` to `ActiveSOSScreen` and timer stops.

Build app after pulling latest code:

```bash
./gradlew :composeApp:compileDebugKotlin
```

## Step 12: Deploy latest backend + app for real-time push detail page

After code updates, redeploy Lambda and reinstall app:

```bash
cd /Users/pavudi605@apac.comcast.com/Documents
./gradlew :server:fatJar
aws lambda update-function-code \
  --function-name arn:aws:lambda:ap-south-1:291759414836:function:guardianlink-api \
  --zip-file fileb:///Users/pavudi605@apac.comcast.com/Documents/server/build/libs/server-all-1.0.0.jar \
  --region ap-south-1
aws lambda wait function-updated \
  --function-name arn:aws:lambda:ap-south-1:291759414836:function:guardianlink-api \
  --region ap-south-1
./gradlew :composeApp:installDebug
```

Expected runtime behavior:
- SOS push is sent to all contacts included in payload (with valid endpoint mapping).
- On receiving device, push appears in notification tray.
- On tap, app opens SOS detail page showing:
  - victim name
  - constant help text (`<victimName> might need help`)
  - live location coordinates if shared

---

## Does terminal setup reflect in AWS Console?

Yes. CLI and Console use the same AWS control plane.

Check reflection here:
- SNS apps: SNS console -> Platform applications
- API route: API Gateway console -> `resq` -> Routes
- Lambda permission/env: Lambda console -> `guardianlink-api` -> Permissions / Configuration -> Environment variables
- IAM role inline policy: IAM console -> role `guardianlink-lambda-role`



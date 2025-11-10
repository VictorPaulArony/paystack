# Paystack Payment Integration - Spring Boot

Production-ready Paystack integration supporting STK Push payments and B2C transfers with easy environment switching.

## Features

- ✅ Customer payment initialization (STK Push equivalent)
- ✅ Transaction verification
- ✅ B2C transfers (Business to Customer)
- ✅ Transfer recipient management
- ✅ Webhook handling with signature verification
- ✅ Easy switching between test and production environments
- ✅ Production-ready with proper error handling
- ✅ Comprehensive logging
- ✅ No dummy data

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Paystack account (both test and production keys)

## Setup Instructions

### 1. Get Paystack API Keys

1. **Test Environment:**
   - Go to [Paystack Dashboard](https://dashboard.paystack.com/)
   - Navigate to Settings → API Keys & Webhooks
   - Copy your Test Secret Key (starts with `sk_test_`)
   - Copy your Test Public Key (starts with `pk_test_`)

2. **Production Environment:**
   - Complete Paystack verification process
   - Navigate to Settings → API Keys & Webhooks
   - Switch to "Live" mode
   - Copy your Live Secret Key (starts with `sk_live_`)
   - Copy your Live Public Key (starts with `pk_live_`)

3. **Webhook Secret:**
   - In Settings → API Keys & Webhooks
   - Find your Webhook Secret

### 2. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Test Keys
PAYSTACK_TEST_SECRET_KEY=sk_test_your_actual_test_key
PAYSTACK_TEST_PUBLIC_KEY=pk_test_your_actual_test_key

# Production Keys
PAYSTACK_PROD_SECRET_KEY=sk_live_your_actual_production_key
PAYSTACK_PROD_PUBLIC_KEY=pk_live_your_actual_production_key

# Environment (test or production)
PAYSTACK_ENV=test

# Webhook Secret
PAYSTACK_WEBHOOK_SECRET=your_webhook_secret

# App Base URL
APP_BASE_URL=http://localhost:8080
```

### 3. Build and Run

```bash
# Build the application
mvn clean install

# Run with test environment
PAYSTACK_ENV=test mvn spring-boot:run

# Run with production environment
PAYSTACK_ENV=production mvn spring-boot:run

# Or using Spring profiles
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # Uses test
mvn spring-boot:run -Dspring-boot.run.profiles=prod # Uses production
```

## API Endpoints

### 1. Initialize Payment (STK Push)

**Endpoint:** `POST /api/paystack/initialize`

**Request:**
```json
{
  "email": "customer@example.com",
  "amount": 10000,
  "currency": "NGN",
  "channels": ["mobile_money", "card", "bank"]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transaction initialized successfully",
  "data": {
    "authorization_url": "https://checkout.paystack.com/...",
    "access_code": "...",
    "reference": "TXN_..."
  },
  "environment": "test"
}
```

**Usage:** Send the `authorization_url` to the customer to complete payment.

### 2. Verify Transaction

**Endpoint:** `GET /api/paystack/verify/{reference}`

**Response:**
```json
{
  "success": true,
  "message": "Verification successful",
  "data": {
    "status": "success",
    "reference": "TXN_...",
    "amount": 1000000,
    "currency": "NGN",
    "customer": {
      "email": "customer@example.com",
      "first_name": "John",
      "last_name": "Doe"
    }
  },
  "environment": "test"
}
```

### 3. Create Transfer Recipient (for B2C)

**Endpoint:** `POST /api/paystack/recipient`

**Request:**
```json
{
  "type": "mobile_money",
  "name": "John Doe",
  "account_number": "0123456789",
  "bank_code": "MTN",
  "currency": "NGN"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Recipient created successfully",
  "data": {
    "recipient_code": "RCP_...",
    "type": "mobile_money",
    "name": "John Doe",
    "details": {
      "account_number": "0123456789",
      "bank_code": "MTN"
    }
  },
  "environment": "test"
}
```

### 4. Initiate B2C Transfer

**Endpoint:** `POST /api/paystack/transfer`

**Request:**
```json
{
  "recipient_code": "RCP_...",
  "amount": 5000,
  "reason": "Payment for services",
  "currency": "NGN"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transfer initiated successfully",
  "data": {
    "transfer_code": "TRF_...",
    "status": "pending",
    "amount": 500000,
    "recipient": "RCP_...",
    "reason": "Payment for services"
  },
  "environment": "test"
}
```

### 5. Webhook Handler

**Endpoint:** `POST /api/paystack/webhook`

**Headers:**
- `x-paystack-signature`: Webhook signature from Paystack

Handles events like:
- `charge.success` - Payment completed
- `transfer.success` - Transfer completed
- `transfer.failed` - Transfer failed

### 6. Check Current Environment

**Endpoint:** `GET /api/paystack/environment`

**Response:**
```json
{
  "environment": "test",
  "is_production": false
}
```

## Environment Switching

### Method 1: Environment Variable

```bash
# Switch to test
export PAYSTACK_ENV=test

# Switch to production
export PAYSTACK_ENV=production
```

### Method 2: Spring Profile

```bash
# Development (uses test)
java -jar app.jar --spring.profiles.active=dev

# Production (uses production)
java -jar app.jar --spring.profiles.active=prod
```

### Method 3: Application Properties

Update `application.yml`:
```yaml
paystack:
  active-env: production  # or test
```

## Bank/Mobile Money Codes for Nigeria

### Mobile Money Providers
- MTN: `MTN`
- Airtel: `ATL`
- Glo: `GLO`
- 9mobile: `ETI`

### Banks
- Access Bank: `044`
- GTBank: `058`
- Zenith Bank: `057`
- First Bank: `011`
- UBA: `033`

Get full list: [Paystack Banks List API](https://api.paystack.co/bank)

## Webhook Configuration

1. Go to Paystack Dashboard → Settings → API Keys & Webhooks
2. Add your webhook URL: `https://yourdomain.com/api/paystack/webhook`
3. Copy the webhook secret to `PAYSTACK_WEBHOOK_SECRET`

## Testing

### Test Mode
- Use test API keys
- No real money is charged
- Use test card numbers from Paystack docs
- Mobile money payments won't complete in test mode

### Production Mode
- Use live API keys
- Real money transactions
- Requires completed KYC verification
- All payments are live

## Production Deployment Checklist

- [ ] Set `PAYSTACK_ENV=production`
- [ ] Configure production API keys
- [ ] Set up HTTPS/SSL certificate
- [ ] Configure webhook endpoint
- [ ] Set up proper database
- [ ] Enable application logging
- [ ] Configure CORS if needed
- [ ] Set up monitoring and alerts
- [ ] Test webhook handling
- [ ] Verify environment switching works

## Security Best Practices

1. **Never commit API keys** - Use environment variables
2. **Verify webhook signatures** - Always validate incoming webhooks
3. **Use HTTPS in production** - Secure communication
4. **Validate amounts** - Check transaction amounts on your backend
5. **Log all transactions** - Keep audit trails
6. **Rate limiting** - Protect against abuse
7. **Idempotency** - Handle duplicate requests

## Error Handling

The integration includes comprehensive error handling for:
- Network failures
- Invalid API keys
- Insufficient balance (for transfers)
- Invalid recipient details
- Webhook signature verification failures

## Logging

Logs include:
- Transaction initialization
- Verification requests
- Transfer operations
- Webhook events
- Errors with full stack traces

Log files: `logs/paystack-service.log`

## Support

- Paystack Documentation: https://paystack.com/docs
- Paystack API Reference: https://paystack.com/docs/api
- Support: support@paystack.com

## License

This is a reference implementation. Customize according to your needs.
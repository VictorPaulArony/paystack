#!/bin/bash

# ============================================
# PAYSTACK KENYA INTEGRATION TEST SUITE
# ============================================
# Complete test suite for Paystack Kenya implementation
# Tests M-PESA, bank transfers, and card payments
# ============================================

BASE_URL="http://localhost:8080/api/paystack"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print section headers
print_header() {
    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${CYAN}${NC}"
    echo -e "${CYAN}========================================${NC}\n"
}

# Function to print test name
print_test() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}TEST $TOTAL_TESTS: ${NC}"
}

# Function to check if response is successful
check_success() {
    local response=
    local success=$(echo "$response" | jq -r '.success // false')
    
    if [ "$success" = "true" ]; then
        echo -e "${GREEN}✓ PASSED${NC}\n"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        echo "Response: $response"
        echo ""
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Start tests
clear
echo -e "${PURPLE}"
cat << "EOF"
╔═══════════════════════════════════════════════════════╗
║                                                       ║
║    🇰🇪  PAYSTACK KENYA TEST SUITE  🇰🇪                ║
║                                                       ║
║    Testing M-PESA, Banks & Card Payments             ║
║                                                       ║
╚═══════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Check if server is running
echo -e "${YELLOW}Checking if Spring Boot server is running...${NC}"
if curl -s "$BASE_URL/environment" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}\n"
else
    echo -e "${RED}✗ Server is not running. Please start your Spring Boot application.${NC}"
    echo -e "${YELLOW}Run: mvn spring-boot:run${NC}\n"
    exit 1
fi

# ============================================
# SECTION 1: ENVIRONMENT & CONFIGURATION
# ============================================
print_header "SECTION 1: ENVIRONMENT & CONFIGURATION"

print_test "Check Current Environment"
ENV_RESPONSE=$(curl -s -X GET "$BASE_URL/environment" \
  -H "Content-Type: application/json")
echo "$ENV_RESPONSE" | jq '.'
check_success "$ENV_RESPONSE"

CURRENT_ENV=$(echo "$ENV_RESPONSE" | jq -r '.environment')
IS_PROD=$(echo "$ENV_RESPONSE" | jq -r '.is_production')
echo -e "${YELLOW}📍 Running in: $CURRENT_ENV mode${NC}"
echo -e "${YELLOW}📍 Production: $IS_PROD${NC}\n"

# ============================================
# SECTION 2: M-PESA PAYMENT TESTS
# ============================================
print_header "SECTION 2: M-PESA PAYMENT TESTS"

# Test 2.1: M-PESA Payment - Safaricom
print_test "Initialize M-PESA Payment (Safaricom)"
MPESA_RESPONSE=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer.mpesa@example.com",
    "amount": 1000,
    "currency": "KES",
    "channels": ["mobile_money"]
  }')
echo "$MPESA_RESPONSE" | jq '.'

if check_success "$MPESA_RESPONSE"; then
    MPESA_REF=$(echo "$MPESA_RESPONSE" | jq -r '.data.reference')
    MPESA_URL=$(echo "$MPESA_RESPONSE" | jq -r '.data.authorization_url')
    echo -e "${GREEN}Reference: $MPESA_REF${NC}"
    echo -e "${GREEN}Payment URL: $MPESA_URL${NC}"
    echo -e "${YELLOW}💡 Visit this URL to complete M-PESA payment${NC}\n"
fi

# Test 2.2: M-PESA Payment - Small Amount
print_test "Initialize M-PESA Payment - Small Amount (KES 50)"
SMALL_MPESA=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "small.payment@example.com",
    "amount": 50,
    "currency": "KES",
    "channels": ["mobile_money"]
  }')
echo "$SMALL_MPESA" | jq '.'
check_success "$SMALL_MPESA"

# Test 2.3: M-PESA Payment - Large Amount
print_test "Initialize M-PESA Payment - Large Amount (KES 50,000)"
LARGE_MPESA=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "large.payment@example.com",
    "amount": 50000,
    "currency": "KES",
    "channels": ["mobile_money"]
  }')
echo "$LARGE_MPESA" | jq '.'
check_success "$LARGE_MPESA"

# ============================================
# SECTION 3: CARD PAYMENT TESTS
# ============================================
print_header "SECTION 3: CARD PAYMENT TESTS"

# Test 3.1: Card Payment - Local Cards
print_test "Initialize Card Payment - Kenyan Cards"
CARD_RESPONSE=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "card.customer@example.com",
    "amount": 2500,
    "currency": "KES",
    "channels": ["card"]
  }')
echo "$CARD_RESPONSE" | jq '.'

if check_success "$CARD_RESPONSE"; then
    CARD_REF=$(echo "$CARD_RESPONSE" | jq -r '.data.reference')
    CARD_URL=$(echo "$CARD_RESPONSE" | jq -r '.data.authorization_url')
    echo -e "${GREEN}Reference: $CARD_REF${NC}"
    echo -e "${GREEN}Payment URL: $CARD_URL${NC}\n"
fi

# Test 3.2: Multi-Channel Payment (M-PESA + Cards)
print_test "Initialize Multi-Channel Payment (M-PESA + Cards)"
MULTI_RESPONSE=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "multi.channel@example.com",
    "amount": 5000,
    "currency": "KES",
    "channels": ["mobile_money", "card"]
  }')
echo "$MULTI_RESPONSE" | jq '.'
check_success "$MULTI_RESPONSE"

# ============================================
# SECTION 4: BANK TRANSFER TESTS
# ============================================
print_header "SECTION 4: BANK TRANSFER TESTS"

# Test 4.1: Bank Payment - All Channels
print_test "Initialize Bank Payment - All Channels"
BANK_RESPONSE=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bank.customer@example.com",
    "amount": 10000,
    "currency": "KES",
    "channels": ["bank", "card", "mobile_money"]
  }')
echo "$BANK_RESPONSE" | jq '.'
check_success "$BANK_RESPONSE"

# ============================================
# SECTION 5: TRANSACTION VERIFICATION
# ============================================
print_header "SECTION 5: TRANSACTION VERIFICATION"

# Test 5.1: Verify M-PESA Transaction
if [ ! -z "$MPESA_REF" ]; then
    print_test "Verify M-PESA Transaction"
    VERIFY_MPESA=$(curl -s -X GET "$BASE_URL/verify/$MPESA_REF" \
      -H "Content-Type: application/json")
    echo "$VERIFY_MPESA" | jq '.'
    
    STATUS=$(echo "$VERIFY_MPESA" | jq -r '.data.status // "unknown"')
    echo -e "${YELLOW}Transaction Status: $STATUS${NC}\n"
else
    echo -e "${YELLOW}Skipping - No M-PESA reference available${NC}\n"
fi

# Test 5.2: Verify Card Transaction
if [ ! -z "$CARD_REF" ]; then
    print_test "Verify Card Transaction"
    VERIFY_CARD=$(curl -s -X GET "$BASE_URL/verify/$CARD_REF" \
      -H "Content-Type: application/json")
    echo "$VERIFY_CARD" | jq '.'
    echo ""
fi

# Test 5.3: Verify Non-Existent Transaction
print_test "Verify Non-Existent Transaction (Should Fail)"
VERIFY_INVALID=$(curl -s -X GET "$BASE_URL/verify/INVALID_REF_12345" \
  -H "Content-Type: application/json")
echo "$VERIFY_INVALID" | jq '.'
echo -e "${YELLOW}Expected to fail - This is correct behavior${NC}\n"

# ============================================
# SECTION 6: B2C - M-PESA TRANSFERS
# ============================================
print_header "SECTION 6: B2C - M-PESA TRANSFERS"

# Test 6.1: Create M-PESA Recipient - Safaricom
print_test "Create M-PESA Transfer Recipient - Safaricom"
MPESA_RECIPIENT=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "mobile_money",
    "name": "John Kamau Mwangi",
    "account_number": "254712345678",
    "bank_code": "mobile_money",
    "currency": "KES"
  }')
echo "$MPESA_RECIPIENT" | jq '.'

if check_success "$MPESA_RECIPIENT"; then
    MPESA_RECIPIENT_CODE=$(echo "$MPESA_RECIPIENT" | jq -r '.data.recipient_code')
    echo -e "${GREEN}M-PESA Recipient Code: $MPESA_RECIPIENT_CODE${NC}\n"
fi

# Test 6.2: Create M-PESA Recipient - Different Format
print_test "Create M-PESA Recipient - Alternative Number Format"
MPESA_RECIPIENT2=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "mobile_money",
    "name": "Jane Wanjiku Njeri",
    "account_number": "254722123456",
    "bank_code": "mobile_money",
    "currency": "KES"
  }')
echo "$MPESA_RECIPIENT2" | jq '.'
check_success "$MPESA_RECIPIENT2"

# Test 6.3: Initiate M-PESA Transfer
if [ ! -z "$MPESA_RECIPIENT_CODE" ] && [ "$MPESA_RECIPIENT_CODE" != "null" ]; then
    print_test "Initiate B2C Transfer to M-PESA"
    MPESA_TRANSFER=$(curl -s -X POST "$BASE_URL/transfer" \
      -H "Content-Type: application/json" \
      -d "{
        \"recipient_code\": \"$MPESA_RECIPIENT_CODE\",
        \"amount\": 500,
        \"reason\": \"Payment for freelance services\",
        \"currency\": \"KES\"
      }")
    echo "$MPESA_TRANSFER" | jq '.'
    
    if check_success "$MPESA_TRANSFER"; then
        TRANSFER_CODE=$(echo "$MPESA_TRANSFER" | jq -r '.data.transfer_code')
        TRANSFER_STATUS=$(echo "$MPESA_TRANSFER" | jq -r '.data.status')
        echo -e "${GREEN}Transfer Code: $TRANSFER_CODE${NC}"
        echo -e "${GREEN}Transfer Status: $TRANSFER_STATUS${NC}\n"
    fi
else
    echo -e "${YELLOW}Skipping - No valid M-PESA recipient code${NC}\n"
fi

# Test 6.4: Larger M-PESA Transfer
if [ ! -z "$MPESA_RECIPIENT_CODE" ] && [ "$MPESA_RECIPIENT_CODE" != "null" ]; then
    print_test "Initiate Larger M-PESA Transfer (KES 5,000)"
    LARGE_TRANSFER=$(curl -s -X POST "$BASE_URL/transfer" \
      -H "Content-Type: application/json" \
      -d "{
        \"recipient_code\": \"$MPESA_RECIPIENT_CODE\",
        \"amount\": 5000,
        \"reason\": \"Monthly salary payment\",
        \"currency\": \"KES\"
      }")
    echo "$LARGE_TRANSFER" | jq '.'
    check_success "$LARGE_TRANSFER"
fi

# ============================================
# SECTION 7: B2C - BANK TRANSFERS
# ============================================
print_header "SECTION 7: B2C - BANK TRANSFERS (KENYAN BANKS)"

# Test 7.1: Equity Bank Transfer
print_test "Create Transfer Recipient - Equity Bank"
EQUITY_RECIPIENT=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "nuban",
    "name": "Peter Omondi Otieno",
    "account_number": "0120123456789",
    "bank_code": "68",
    "currency": "KES"
  }')
echo "$EQUITY_RECIPIENT" | jq '.'

if check_success "$EQUITY_RECIPIENT"; then
    EQUITY_CODE=$(echo "$EQUITY_RECIPIENT" | jq -r '.data.recipient_code')
    echo -e "${GREEN}Equity Bank Recipient: $EQUITY_CODE${NC}\n"
fi

# Test 7.2: KCB Bank Transfer
print_test "Create Transfer Recipient - KCB Bank"
KCB_RECIPIENT=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "nuban",
    "name": "Mary Akinyi Odhiambo",
    "account_number": "1234567890",
    "bank_code": "63",
    "currency": "KES"
  }')
echo "$KCB_RECIPIENT" | jq '.'

if check_success "$KCB_RECIPIENT"; then
    KCB_CODE=$(echo "$KCB_RECIPIENT" | jq -r '.data.recipient_code')
    echo -e "${GREEN}KCB Bank Recipient: $KCB_CODE${NC}\n"
fi

# Test 7.3: Co-operative Bank Transfer
print_test "Create Transfer Recipient - Co-operative Bank"
COOP_RECIPIENT=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "nuban",
    "name": "James Kipchoge Koech",
    "account_number": "01123456789012",
    "bank_code": "66",
    "currency": "KES"
  }')
echo "$COOP_RECIPIENT" | jq '.'
check_success "$COOP_RECIPIENT"

# Test 7.4: Standard Chartered Transfer
print_test "Create Transfer Recipient - Standard Chartered"
STANCHART_RECIPIENT=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "nuban",
    "name": "Grace Wambui Kariuki",
    "account_number": "0100123456789",
    "bank_code": "67",
    "currency": "KES"
  }')
echo "$STANCHART_RECIPIENT" | jq '.'
check_success "$STANCHART_RECIPIENT"

# Test 7.5: Initiate Bank Transfer
if [ ! -z "$EQUITY_CODE" ] && [ "$EQUITY_CODE" != "null" ]; then
    print_test "Initiate B2C Transfer to Equity Bank"
    BANK_TRANSFER=$(curl -s -X POST "$BASE_URL/transfer" \
      -H "Content-Type: application/json" \
      -d "{
        \"recipient_code\": \"$EQUITY_CODE\",
        \"amount\": 10000,
        \"reason\": \"Supplier payment\",
        \"currency\": \"KES\"
      }")
    echo "$BANK_TRANSFER" | jq '.'
    check_success "$BANK_TRANSFER"
fi

# ============================================
# SECTION 8: CALLBACK TESTS
# ============================================
print_header "SECTION 8: PAYMENT CALLBACK TESTS"

# Test 8.1: Valid Callback
if [ ! -z "$MPESA_REF" ]; then
    print_test "Test Payment Callback - Valid Reference"
    CALLBACK=$(curl -s -X GET "$BASE_URL/callback?reference=$MPESA_REF" \
      -H "Content-Type: application/json")
    echo "$CALLBACK" | jq '.'
    echo ""
fi

# Test 8.2: Invalid Callback
print_test "Test Payment Callback - Invalid Reference"
INVALID_CALLBACK=$(curl -s -X GET "$BASE_URL/callback?reference=INVALID_REF" \
  -H "Content-Type: application/json")
echo "$INVALID_CALLBACK" | jq '.'
echo -e "${YELLOW}Expected to fail - This is correct behavior${NC}\n"

# ============================================
# SECTION 9: WEBHOOK TESTS
# ============================================
print_header "SECTION 9: WEBHOOK EVENT TESTS"

# Test 9.1: Charge Success Event
print_test "Webhook Event - Charge Success"
WEBHOOK_SUCCESS=$(curl -s -X POST "$BASE_URL/webhook" \
  -H "Content-Type: application/json" \
  -H "x-paystack-signature: test_signature" \
  -d '{
    "event": "charge.success",
    "data": {
      "reference": "TXN_TEST_123456",
      "status": "success",
      "amount": 100000,
      "currency": "KES",
      "channel": "mobile_money",
      "customer": {
        "email": "customer@example.com"
      }
    }
  }')
echo "$WEBHOOK_SUCCESS"
echo -e "${YELLOW}Note: Will fail signature verification in test mode${NC}\n"

# Test 9.2: Transfer Success Event
print_test "Webhook Event - Transfer Success"
WEBHOOK_TRANSFER=$(curl -s -X POST "$BASE_URL/webhook" \
  -H "Content-Type: application/json" \
  -H "x-paystack-signature: test_signature" \
  -d '{
    "event": "transfer.success",
    "data": {
      "reference": "TRF_TEST_123456",
      "status": "success",
      "amount": 50000,
      "currency": "KES",
      "recipient": "RCP_TEST_123"
    }
  }')
echo "$WEBHOOK_TRANSFER"
echo -e "${YELLOW}Note: Will fail signature verification in test mode${NC}\n"

# ============================================
# SECTION 10: ERROR HANDLING TESTS
# ============================================
print_header "SECTION 10: ERROR HANDLING & EDGE CASES"

# Test 10.1: Invalid Email
print_test "Initialize Payment - Invalid Email Format"
INVALID_EMAIL=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "not-an-email",
    "amount": 1000,
    "currency": "KES"
  }')
echo "$INVALID_EMAIL" | jq '.'
echo -e "${YELLOW}Expected to fail - Invalid email format${NC}\n"

# Test 10.2: Missing Email
print_test "Initialize Payment - Missing Email"
MISSING_EMAIL=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "currency": "KES"
  }')
echo "$MISSING_EMAIL" | jq '.'
echo -e "${YELLOW}Expected to fail - Missing required field${NC}\n"

# Test 10.3: Zero Amount
print_test "Initialize Payment - Zero Amount"
ZERO_AMOUNT=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "amount": 0,
    "currency": "KES"
  }')
echo "$ZERO_AMOUNT" | jq '.'
echo -e "${YELLOW}Expected to fail - Invalid amount${NC}\n"

# Test 10.4: Negative Amount
print_test "Initialize Payment - Negative Amount"
NEGATIVE_AMOUNT=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "amount": -1000,
    "currency": "KES"
  }')
echo "$NEGATIVE_AMOUNT" | jq '.'
echo -e "${YELLOW}Expected to fail - Negative amount${NC}\n"

# Test 10.5: Invalid Currency
print_test "Initialize Payment - Invalid Currency"
INVALID_CURRENCY=$(curl -s -X POST "$BASE_URL/initialize" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "amount": 1000,
    "currency": "INVALID"
  }')
echo "$INVALID_CURRENCY" | jq '.'
echo -e "${YELLOW}Expected to fail - Invalid currency${NC}\n"

# Test 10.6: Invalid Phone Number Format for M-PESA
print_test "Create Recipient - Invalid M-PESA Number"
INVALID_PHONE=$(curl -s -X POST "$BASE_URL/recipient" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "mobile_money",
    "name": "Test User",
    "account_number": "0712345678",
    "bank_code": "mpesa",
    "currency": "KES"
  }')
echo "$INVALID_PHONE" | jq '.'
echo -e "${YELLOW}Note: May fail - Phone should start with 254${NC}\n"

# ============================================
# FINAL SUMMARY
# ============================================
print_header "TEST SUMMARY"

echo -e "${CYAN}Total Tests Run: ${NC}$TOTAL_TESTS"
echo -e "${GREEN}Passed: ${NC}$PASSED_TESTS"
echo -e "${RED}Failed: ${NC}$FAILED_TESTS"

SUCCESS_RATE=$(awk "BEGIN {printf \"%.1f\", ($PASSED_TESTS/$TOTAL_TESTS)*100}")
echo -e "${CYAN}Success Rate: ${NC}${SUCCESS_RATE}%\n"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! Your Kenya integration is working perfectly!${NC}\n"
else
    echo -e "${YELLOW}⚠️  Some tests failed. This may be expected for:${NC}"
    echo -e "${YELLOW}   - Tests running in test mode${NC}"
    echo -e "${YELLOW}   - Transfers requiring sufficient balance${NC}"
    echo -e "${YELLOW}   - Webhook signature verification${NC}"
    echo -e "${YELLOW}   - Intentional error tests${NC}\n"
fi

# ============================================
# USEFUL INFORMATION
# ============================================
print_header "USEFUL INFORMATION FOR KENYA"

echo -e "${CYAN}📱 M-PESA Phone Number Format:${NC}"
echo "   - Must start with 254 (Kenya country code)"
echo "   - Example: 254712345678"
echo ""

echo -e "${CYAN}🏦 Common Kenyan Bank Codes:${NC}"
echo "   - Equity Bank: 68"
echo "   - KCB Bank: 63"
echo "   - Co-operative Bank: 66"
echo "   - Standard Chartered: 67"
echo "   - Barclays/Absa: 62"
echo "   - NCBA Bank: 7"
echo ""

echo -e "${CYAN}💳 Payment Channels:${NC}"
echo "   - mobile_money: M-PESA payments"
echo "   - card: Visa, Mastercard, Amex"
echo "   - bank: Bank transfers"
echo ""

echo -e "${CYAN}💰 Transaction Fees (Kenya):${NC}"
echo "   - Local cards: 2.9%"
echo "   - International cards: 3.8%"
echo "   - M-PESA: 1.5%"
echo ""

echo -e "${CYAN}🔗 Next Steps:${NC}"
if [ ! -z "$MPESA_URL" ]; then
    echo -e "   1. Visit: ${GREEN}$MPESA_URL${NC}"
    echo "   2. Complete test M-PESA payment"
fi
if [ ! -z "$CARD_URL" ]; then
    echo -e "   3. Visit: ${GREEN}$CARD_URL${NC}"
    echo "   4. Test with Paystack test cards"
fi
echo "   5. Check Paystack dashboard for transactions"
echo "   6. Configure webhook URL in production"
echo " 7. Complete KYC for production access"
echo ""

echo -e "${CYAN}📚 Documentation:${NC}"
echo "   - Paystack Docs: https://paystack.com/docs"
echo "   - Kenya Guide: https://paystack.com/docs/payments/accept-payments/#kenya"
echo ""

echo -e "${GREEN}Test suite completed at $(date)${NC}\n"
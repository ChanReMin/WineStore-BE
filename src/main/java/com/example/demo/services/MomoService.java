package com.example.demo.services;

import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.commons.enums.PaymentStatus;
import com.example.demo.commons.enums.TransactionStatus;
import com.example.demo.configs.MomoConfig;
import com.example.demo.entities.Order;
import com.example.demo.entities.PaymentMethod;
import com.example.demo.entities.PaymentTransaction;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.OrderCommandRepository;
import com.example.demo.repositories.commands.PaymentTransactionCommandRepository;
import com.example.demo.repositories.queries.OrderQueryRepository;
import com.example.demo.repositories.queries.PaymentMethodQueryRepository;
import com.example.demo.services.queries.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MomoService implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(MomoService.class);

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderQueryRepository orderQueryRepository;
    private final OrderCommandRepository orderCommandRepository;
    private final PaymentMethodQueryRepository paymentMethodQueryRepository;
    private final PaymentTransactionCommandRepository paymentTransactionCommandRepository;

    /**
     * Create payment URL for Momo
     *
     * ✅ IMPORTANT: Use orderId (database ID) in extraData, not requestId
     * This way we can retrieve the correct order when handling IPN
     */
    @Override
    public String createPaymentUrl(HttpServletRequest request, Long orderId, long amount) {
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Payment for order " + orderId;
        String redirectUrl = momoConfig.getReturnUrl();
        String ipnUrl = momoConfig.getIpnUrl();

        // ✅ CRITICAL: Store real orderId in extraData for IPN handling
        String extraData = String.valueOf(orderId);

        log.info("🔐 Creating Momo payment");
        log.info("  - Order ID (database): {}", orderId);
        log.info("  - Request ID (for Momo): {}", requestId);
        log.info("  - Extra Data (for IPN): {}", extraData);
        log.info("  - Amount: {}", amount);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("accessKey", momoConfig.getAccessKey());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", String.valueOf(amount));
        requestBody.put("orderId", requestId);  // Momo's unique order ID
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("extraData", extraData);  // ✅ Real orderId here
        requestBody.put("requestType", "captureWallet");

        // Build rawHmac for signature
        String rawHmac = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + requestId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        String signature = hmacSHA256(rawHmac, momoConfig.getSecretKey());
        requestBody.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            log.info("📤 Momo API Request: {}", jsonRequestBody);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);
            String response = restTemplate.postForObject(momoConfig.getEndpoint(), entity, String.class);

            log.info("📥 Momo API Response: {}", response);

            Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
            String payUrl = responseMap.get("payUrl");

            log.info("✓ Payment URL created: {}", payUrl);
            return payUrl;

        } catch (IOException e) {
            log.error("❌ IOException processing Momo response: {}", e.getMessage(), e);
            return null;
        } catch (RestClientException e) {
            log.error("❌ RestClientException communicating with Momo: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("❌ Unexpected error creating Momo payment: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Handle Momo IPN (Instant Payment Notification)
     *
     * ✅ IMPORTANT: Extract orderId from extraData field
     */
    public void handleMomoIpn(Map<String, Object> payload) {
        log.info("📨 Received Momo IPN");
        log.info("  - Payload: {}", payload);

        // Validate signature
        String signature = (String) payload.get("signature");

        SortedMap<String, Object> sortedPayload = new TreeMap<>(payload);
        sortedPayload.remove("signature");
        sortedPayload.put("accessKey", momoConfig.getAccessKey());

        String rawHmac = sortedPayload.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String expectedSignature = hmacSHA256(rawHmac, momoConfig.getSecretKey());

        if (!expectedSignature.equals(signature)) {
            log.error("❌ Invalid Momo IPN signature");
            log.error("  - Expected: {}", expectedSignature);
            log.error("  - Actual: {}", signature);
            throw new BadRequestException("Invalid Momo IPN signature");
        }

        log.info("✓ IPN signature verified");

        Integer resultCode = (Integer) payload.get("resultCode");

        if (resultCode == 0) {
            // ✅ CRITICAL: Extract orderId from extraData
            String extraData = (String) payload.get("extraData");
            Long orderId = Long.valueOf(extraData);

            String momoOrderId = (String) payload.get("orderId");
            String transId = String.valueOf(payload.get("transId"));
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            log.info("💳 Payment successful");
            log.info("  - Order ID (database): {}", orderId);
            log.info("  - Momo Order ID: {}", momoOrderId);
            log.info("  - Transaction ID: {}", transId);
            log.info("  - Amount: {}", amount);

            // Find order by database ID
            Order order = orderQueryRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("❌ Order not found: {}", orderId);
                        return new ResourceNotFoundException("Order not found: " + orderId);
                    });

            log.info("✓ Order found: {}", order.getId());

            // Prevent duplicate processing
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                log.warn("⚠️ Order already paid, skipping duplicate IPN");
                return;
            }

            // Update order payment status
            PaymentMethod paymentMethod = paymentMethodQueryRepository.findByCode("MOMO")
                    .orElseThrow(() -> {
                        log.error("❌ Payment method MOMO not found");
                        return new ResourceNotFoundException("Payment method not found");
                    });

            order.setStatus(OrderStatus.PAID);
            orderCommandRepository.save(order);
            log.info("✓ Order payment status updated to PAID");

            // Create payment transaction record
            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                    .order(order)
                    .paymentMethod(paymentMethod)
                    .providerTxnCode(transId)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .build();
            paymentTransactionCommandRepository.save(paymentTransaction);

            log.info("✓ Payment transaction created");
            log.info("✅ Momo IPN handled successfully");

        } else {
            log.warn("⚠️ Payment failed with result code: {}", resultCode);
            log.warn("  - Message: {}", payload.get("message"));
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    private String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("❌ Error generating HMAC-SHA256: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
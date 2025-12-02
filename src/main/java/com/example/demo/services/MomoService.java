package com.example.demo.services;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderQueryRepository orderQueryRepository;
    private final OrderCommandRepository orderCommandRepository;
    private final PaymentMethodQueryRepository paymentMethodQueryRepository;
    private final PaymentTransactionCommandRepository paymentTransactionCommandRepository;


    @Override
    public String createPaymentUrl(HttpServletRequest request, Long orderId, long amount) {
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Payment for order " + orderId;
        String redirectUrl = momoConfig.getReturnUrl();
        String ipnUrl = momoConfig.getIpnUrl();
        String extraData = ""; // Must not be null

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("accessKey", momoConfig.getAccessKey());
        requestBody.put("requestId", requestId);
        requestBody.put("amount", String.valueOf(amount));
        requestBody.put("orderId", orderId.toString());
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", "captureWallet");

        String rawHmac = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + "captureWallet";

        String signature = hmacSHA256(rawHmac, momoConfig.getSecretKey());
        requestBody.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

            String response = restTemplate.postForObject(momoConfig.getEndpoint(), entity, String.class);

            Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
            return responseMap.get("payUrl");
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
            return null;
        }

    }

    public void handleMomoIpn(Map<String, Object> payload) {
        String signature = (String) payload.get("signature");

        // Use a SortedMap to ensure keys are in alphabetical order
        SortedMap<String, Object> sortedPayload = new TreeMap<>(payload);
        sortedPayload.remove("signature");

        // Add the accessKey for signature calculation
        sortedPayload.put("accessKey", momoConfig.getAccessKey());

        // Build the raw HMAC string from the sorted map
        String rawHmac = sortedPayload.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String expectedSignature = hmacSHA256(rawHmac, momoConfig.getSecretKey());

        if (!expectedSignature.equals(signature)) {
            throw new BadRequestException("Invalid Momo IPN signature");
        }

        Integer resultCode = (Integer) payload.get("resultCode");
        if (resultCode == 0) {
            String orderIdStr = (String) payload.get("orderId");
            Long orderId = Long.valueOf(orderIdStr);
            String transId = String.valueOf(payload.get("transId"));
            // Ensure amount is correctly converted to BigDecimal
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());


            Order order = orderQueryRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                // To prevent processing the same IPN multiple times
                return;
            }

            PaymentMethod paymentMethod = paymentMethodQueryRepository.findByCode("MOMO")
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

            order.setPaymentStatus(PaymentStatus.PAID);
            orderCommandRepository.save(order);

            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                    .order(order)
                    .paymentMethod(paymentMethod)
                    .providerTxnCode(transId)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .build();
            paymentTransactionCommandRepository.save(paymentTransaction);
        }
    }


    private String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
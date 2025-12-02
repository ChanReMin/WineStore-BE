package com.example.demo.services;

import com.example.demo.commons.enums.PaymentStatus;
import com.example.demo.commons.enums.TransactionStatus;
import com.example.demo.configs.VnpayConfig;
import com.example.demo.entities.Order;
import com.example.demo.entities.PaymentMethod;
import com.example.demo.entities.PaymentTransaction;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.OrderCommandRepository;
import com.example.demo.repositories.commands.PaymentTransactionCommandRepository;
import com.example.demo.repositories.queries.OrderQueryRepository;
import com.example.demo.repositories.queries.PaymentMethodQueryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService implements PaymentService {

    private final VnpayConfig vnpayConfig;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderCommandRepository orderCommandRepository;
    private final PaymentMethodQueryRepository paymentMethodQueryRepository;
    private final PaymentTransactionCommandRepository paymentTransactionCommandRepository;

    // Define timezone and formatter
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String createPaymentUrl(HttpServletRequest request, Long orderId, long amount) {
        String vnp_Version = vnpayConfig.getVersion();
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(orderId);
        String vnp_OrderInfo = "Payment for order " + orderId;
        String orderType = "other";
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = vnpayConfig.getTmnCode();

        // Use ZonedDateTime with Vietnam timezone
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        String vnp_CreateDate = now.format(VNPAY_DATE_FORMATTER);

        // Add 15 minutes for expire date
        ZonedDateTime expireTime = now.plusMinutes(15);
        String vnp_ExpireDate = expireTime.format(VNPAY_DATE_FORMATTER);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort and build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Log for debugging
        System.out.println("===== VNPAY PARAMS =====");
        System.out.println("Current Vietnam Time: " + now);
        System.out.println("vnp_CreateDate = " + vnp_CreateDate);
        System.out.println("vnp_ExpireDate = " + vnp_ExpireDate);
        System.out.println("Expire in minutes = " + java.time.Duration.between(now, expireTime).toMinutes());

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnpayConfig.getUrl() + "?" + queryUrl;
    }

    public String handleVnpayCallback(Map<String, String> queryParams) {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");

        Map<String, String> fields = new HashMap<>(queryParams);
        fields.remove("vnp_SecureHash");

        // Sort fields alphabetically
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        // Build hash data
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                hashData.append('&');
            }
        }
        hashData.deleteCharAt(hashData.length() - 1);

        String secureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());

        if (secureHash.equals(vnp_SecureHash)) {
            Order order = orderQueryRepository.findById(Long.valueOf(vnp_TxnRef))
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            PaymentMethod paymentMethod = paymentMethodQueryRepository.findByCode("VNPAY")
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

            if ("00".equals(vnp_ResponseCode)) {
                order.setPaymentStatus(PaymentStatus.PAID);
                PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                        .order(order)
                        .paymentMethod(paymentMethod)
                        .providerTxnCode(vnp_TxnRef)
                        .amount(order.getTotalAmount())
                        .status(TransactionStatus.SUCCESS)
                        .build();
                paymentTransactionCommandRepository.save(paymentTransaction);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }
            orderCommandRepository.save(order);
            return "Payment status updated successfully";
        } else {
            throw new BadRequestException("Invalid signature");
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
}
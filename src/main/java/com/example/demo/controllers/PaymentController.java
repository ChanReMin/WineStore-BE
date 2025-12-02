package com.example.demo.controllers;

import com.example.demo.services.MomoService;
import com.example.demo.services.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VnpayService vnpayService;
    private final MomoService momoService;

    @GetMapping("/vnpay-callback")
    public ResponseEntity<String> vnpayCallback(@RequestParam Map<String, String> queryParams) {
        String result = vnpayService.handleVnpayCallback(queryParams);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody Map<String, Object> payload) {
        momoService.handleMomoIpn(payload);
        return ResponseEntity.noContent().build();
    }
}


package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.BookingItemDto;
import com.lankatrails.lankatrails_backend.dtos.request.AcceptRejectDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderInfoDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ApproveLicenseResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderViewInfoResponse;
import com.lankatrails.lankatrails_backend.service.AuthService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final BookingService bookingService;

    @PutMapping("/approve-provider/{providerId}")
    public ResponseEntity<APIResponse<String>> approveProvider(@PathVariable Long providerId) {
        APIResponse<String> response = authService.approveProvider(providerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    //Get all the licenses
//    @GetMapping("/approve-provider-service")
//    public ResponseEntity<APIResponse<ApproveLicenseResponse>> approveProviderService() {
//        APIResponse<ApproveLicenseResponse> response = authService.approveProviderService();
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    //Load a provider details
    @GetMapping("/approve-provider/service-category/{providerId}")
    public ResponseEntity<APIResponse<ProviderViewInfoResponse>> approveProviderServiceCategory(@PathVariable Long providerId) {
        APIResponse<ProviderViewInfoResponse> response = authService.loadAllRequestedProviders(providerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //load all providers
    @GetMapping("/approve-provider/providers")
    public ResponseEntity<APIResponse<ProviderInfoResponse>> getProviderInfo() {
        APIResponse<ProviderInfoResponse> response = authService.getBasicProviderInfo();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Approve or Reject request
    @PutMapping("/approve-provider/providers/{providerId}")
    public ResponseEntity<APIResponse<String>> approveOrReject(@RequestBody AcceptRejectDTO acceptRejectDTO) {
        APIResponse<String> approvalStatus = authService.approveOrRejectRequest(acceptRejectDTO);
        return new ResponseEntity<>(approvalStatus, HttpStatus.OK);
    }

    // Admin Booking Endpoints

    // Get all bookings in the system
    @GetMapping("/bookings")
    public ResponseEntity<APIResponse<List<BookingItemDto>>> getAllBookings() {
        APIResponse<List<BookingItemDto>> response = bookingService.getAllBookings();
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get bookings by status (PENDING, CONFIRMED, CANCELLED, etc.)
    @GetMapping("/bookings/status/{status}")
    public ResponseEntity<APIResponse<List<BookingItemDto>>> getBookingsByStatus(@PathVariable String status) {
        APIResponse<List<BookingItemDto>> response = bookingService.getBookingsByStatus(status);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get bookings within a date range
    @GetMapping("/bookings/date-range")
    public ResponseEntity<APIResponse<List<BookingItemDto>>> getBookingsByDateRange(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        APIResponse<List<BookingItemDto>> response = bookingService.getBookingsByDateRange(from, to);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get booking statistics dashboard
    @GetMapping("/bookings/statistics")
    public ResponseEntity<APIResponse<Map<String, Object>>> getBookingStatistics() {
        APIResponse<Map<String, Object>> response = bookingService.getBookingStatistics();
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get recent bookings (default 10, can specify limit)
    @GetMapping("/bookings/recent")
    public ResponseEntity<APIResponse<List<BookingItemDto>>> getRecentBookings(
            @RequestParam(defaultValue = "10") Integer limit) {
        APIResponse<List<BookingItemDto>> response = bookingService.getRecentBookings(limit);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ========== ANALYTICS ENDPOINTS ==========

    // Get tourist count analytics (unique tourists, new tourists, etc.)
    @GetMapping("/analytics/tourists")
    public ResponseEntity<APIResponse<Map<String, Object>>> getTouristAnalytics() {
        APIResponse<Map<String, Object>> response = bookingService.getTouristAnalytics();
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get booking count analytics for a specific date range
    @GetMapping("/analytics/bookings")
    public ResponseEntity<APIResponse<Map<String, Object>>> getBookingAnalytics(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        APIResponse<Map<String, Object>> response = bookingService.getBookingAnalytics(from, to);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get revenue and payment analytics for a specific date range
    @GetMapping("/analytics/revenue")
    public ResponseEntity<APIResponse<Map<String, Object>>> getRevenueAnalytics(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        APIResponse<Map<String, Object>> response = bookingService.getRevenueAnalytics(from, to);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get service provider analytics (top providers by bookings)
    @GetMapping("/analytics/providers")
    public ResponseEntity<APIResponse<Map<String, Object>>> getServiceProviderAnalytics() {
        APIResponse<Map<String, Object>> response = bookingService.getServiceProviderAnalytics();
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get monthly analytics for a specific year
    @GetMapping("/analytics/monthly/{year}")
    public ResponseEntity<APIResponse<Map<String, Object>>> getMonthlyAnalytics(@PathVariable int year) {
        APIResponse<Map<String, Object>> response = bookingService.getMonthlyAnalytics(year);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get top services analytics by booking count
    @GetMapping("/analytics/top-services")
    public ResponseEntity<APIResponse<Map<String, Object>>> getTopServicesAnalytics(
            @RequestParam(defaultValue = "10") Integer limit) {
        APIResponse<Map<String, Object>> response = bookingService.getTopServicesAnalytics(limit);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Get overall system analytics dashboard
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<APIResponse<Map<String, Object>>> getDashboardAnalytics() {
        APIResponse<Map<String, Object>> response = bookingService.getDashboardAnalytics();
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

}
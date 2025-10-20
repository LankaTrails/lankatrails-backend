package com.lankatrails.lankatrails_backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Payment;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripBudgetCategory;
import com.lankatrails.lankatrails_backend.model.TripExpense;
import com.lankatrails.lankatrails_backend.model.TripExpenseShare;
import com.lankatrails.lankatrails_backend.model.TripParticipant;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.PaymentStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.BookingRepository;
import com.lankatrails.lankatrails_backend.repositories.PaymentRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.TripBudgetCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseRepository;
import com.lankatrails.lankatrails_backend.repositories.TripExpenseShareRepository;
import com.lankatrails.lankatrails_backend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TripExpenseRepository tripExpenseRepository;

    @Autowired
    TripExpenseShareRepository tripExpenseShareRepository;

    @Autowired
    TripBudgetCategoryRepository tripBudgetCategoryRepository;

    @Override
    @Transactional
    public PaymentIntent createPaymentIntent(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Booking not found"));

        Provider provider = booking.getTripItem().getService().getProvider();
        if (provider == null) {
            throw new BadRequestException("Provider not found for the service");
        }

        BigDecimal totalAmount = booking.getTotalPrice();
        BigDecimal commission = calculateCommission(totalAmount);
        BigDecimal providerAmount = totalAmount.subtract(commission);

        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
        long providerAmountInCents = providerAmount.multiply(BigDecimal.valueOf(100)).longValue();
        long commissionInCents = commission.multiply(BigDecimal.valueOf(100)).longValue();

        // Create PaymentIntent with Stripe API (pseudo-code)
        Map<String, Object> params = Map.of(
                "amount", amountInCents,
                "currency", "USD",
                "payment_method_types", List.of("card"),
                "application_fee_amount", commissionInCents,
                "transfer_data", Map.of("destination", provider.getStripeAccountId()),
                "metadata", Map.of("bookingId", bookingId.toString())
        );

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment details in your database
            Payment payment = Payment.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .status(PaymentStatus.PENDING)
                    .currency("usd")
                    .amount(totalAmount)
                    .commission(commission)
                    .providerAmount(providerAmount)
                    .stripeFee(null)
                    .stripeChargeId(null)
                    .stripeTransferId(null)
                    .booking(booking)
                    .createdAt(LocalDate.now().atTime(LocalTime.now()))
                    .build();
            payment = paymentRepository.save(payment);

            return paymentIntent;
        } catch (StripeException e) {
            log.error("Error creating PaymentIntent", e);
            throw new RuntimeException("Failed to create payment intent" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public APIResponse<String> confirmPayment(String paymentIntentId) {
        try {
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new BadRequestException("Payment not found"));

            payment.setStatus(PaymentStatus.COMPLETED);

            // Retrieve PaymentIntent first
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Get the latest charge ID from the PaymentIntent
            String latestChargeId = paymentIntent.getLatestCharge();
            if (latestChargeId != null) {
                // Retrieve charge with expanded balance transaction
                Map<String, Object> chargeParams = Map.of(
                        "expand", List.of("balance_transaction")
                );
                Charge charge = Charge.retrieve(latestChargeId, chargeParams, null);

                payment.setStripeChargeId(charge.getId());

                // Handle Stripe fee safely
                if (charge.getBalanceTransactionObject() != null) {
                    payment.setStripeFee(
                            BigDecimal.valueOf(charge.getBalanceTransactionObject().getFee())
                                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                    );
                } else {
                    // If balance transaction is not available, retrieve it separately
                    try {
                        String balanceTransactionId = charge.getBalanceTransaction();
                        if (balanceTransactionId != null) {
                            com.stripe.model.BalanceTransaction balanceTransaction =
                                    com.stripe.model.BalanceTransaction.retrieve(balanceTransactionId);
                            payment.setStripeFee(
                                    BigDecimal.valueOf(balanceTransaction.getFee())
                                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                            );
                        } else {
                            log.warn("No balance transaction ID found for charge: {}", charge.getId());
                            payment.setStripeFee(BigDecimal.ZERO);
                        }
                    } catch (StripeException ex) {
                        log.warn("Could not retrieve balance transaction: {}", ex.getMessage());
                        payment.setStripeFee(BigDecimal.ZERO); // Set to zero if can't retrieve
                    }
                }

                payment.setStripeTransferId(
                        paymentIntent.getTransferData() != null ? paymentIntent.getTransferData().getDestination() : null
                );
            } else {
                log.warn("No charges found for PaymentIntent: {}", paymentIntentId);
                payment.setStripeFee(BigDecimal.ZERO);
            }

            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaidAmount(booking.getPaidAmount().add(payment.getAmount()));
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            // Create expense for the trip after successful payment
            try {
                createExpenseForBooking(booking, payment);
                log.info("Expense created successfully for booking: {}", booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to create expense for booking: {}, error: {}", booking.getBookingId(), e.getMessage());
                // Don't fail the payment confirmation if expense creation fails
            }

            log.info("Payment confirmed for booking: {}", booking.getBookingId());
            return new APIResponse<>(true, "Payment confirmed successfully", paymentIntentId);
        } catch (StripeException e) {
            log.error("Stripe API error confirming payment: {}", e.getMessage());
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }

    /**
     * Creates an expense for the trip after successful booking payment
     */
    private void createExpenseForBooking(Booking booking, Payment payment) {
        try {
            log.info("Creating expense for booking payment - BookingId: {}, PaymentAmount: {}", 
                     booking.getBookingId(), payment.getAmount());

            // Validate that we have a trip and participant
            Trip trip = booking.getTripItem().getTrip();
            TripParticipant bookingParticipant = booking.getTripParticipant();
            
            if (trip == null) {
                log.error("Cannot create expense: Trip not found for booking {}", booking.getBookingId());
                throw new BadRequestException("Trip not found for booking");
            }
            
            if (bookingParticipant == null) {
                log.error("Cannot create expense: Trip participant not found for booking {}", booking.getBookingId());
                throw new BadRequestException("Trip participant not found for booking");
            }

            // Get service category and map to budget category
            ServiceCategory serviceCategory = booking.getTripItem().getService().getCategory().getCategoryName();
            BudgetCategory budgetCategory = mapServiceCategoryToBudgetCategory(serviceCategory);
            
            log.info("Mapping service category {} to budget category {}", serviceCategory, budgetCategory);

            // Find or create budget category for this trip
            TripBudgetCategory tripBudgetCategory = trip.getTripBudgetCategories().stream()
                    .filter(category -> category.getBudgetCategory() == budgetCategory)
                    .findFirst()
                    .orElse(null);

            if (tripBudgetCategory == null) {
                // Create new budget category with no limit
                tripBudgetCategory = TripBudgetCategory.builder()
                        .budgetCategory(budgetCategory)
                        .limitAmount(BigDecimal.ZERO) // No budget limit for payment expenses
                        .spentAmount(BigDecimal.ZERO)
                        .trip(trip)
                        .build();
                tripBudgetCategory = tripBudgetCategoryRepository.save(tripBudgetCategory);
                trip.getTripBudgetCategories().add(tripBudgetCategory);
                log.info("Created new budget category {} for trip {}", budgetCategory, trip.getTripId());
            }

            // Create the trip expense
            Double expenseAmount = payment.getAmount().doubleValue();
            TripExpense tripExpense = TripExpense.builder()
                    .expenseName("Payment: " + booking.getTripItem().getService().getServiceName())
                    .budgetCategory(budgetCategory)
                    .expenseDateTime(LocalDateTime.now())
                    .trip(trip)
                    .createdByParticipant(bookingParticipant)
                    .totalExpenseAmount(expenseAmount)
                    .amount(payment.getAmount())
                    .isThroughApp(true) // Set as requested by user
                    .build();

            // Save the expense
            TripExpense savedExpense = tripExpenseRepository.save(tripExpense);
            log.info("Created trip expense: ID={}, Amount={}, Category={}", 
                     savedExpense.getExpenseId(), expenseAmount, budgetCategory);

            // Create expense share for the participant who made the payment
            TripExpenseShare expenseShare = TripExpenseShare.builder()
                    .amount(expenseAmount)
                    .tripExpense(savedExpense)
                    .tripParticipant(bookingParticipant)
                    .build();
            tripExpenseShareRepository.save(expenseShare);
            log.info("Created expense share for participant: {}", bookingParticipant.getParticipantId());

            // Update budget category spent amount
            tripBudgetCategory.setSpentAmount(tripBudgetCategory.getSpentAmount().add(payment.getAmount()));
            tripBudgetCategoryRepository.save(tripBudgetCategory);

            log.info("Successfully created expense for booking payment - ExpenseId: {}", savedExpense.getExpenseId());

        } catch (Exception e) {
            log.error("Error creating expense for booking {}: {}", booking.getBookingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create expense for booking: " + e.getMessage(), e);
        }
    }

    /**
     * Maps service category to budget category as per user requirements
     */
    private BudgetCategory mapServiceCategoryToBudgetCategory(ServiceCategory serviceCategory) {
        return switch (serviceCategory) {
            case ACCOMMODATION -> BudgetCategory.ACCOMMODATION;
            case TRANSPORT -> BudgetCategory.TRANSPORT;
            case FOOD_BEVERAGE -> BudgetCategory.FOOD;
            case ACTIVITY -> BudgetCategory.ACTIVITY;
            case TOUR_GUIDE -> BudgetCategory.MISCELLANEOUS;
        };
    }

    private BigDecimal calculateCommission(BigDecimal totalAmount) {
        // Implement your commission calculation logic
        // This could be a percentage or fixed amount
        return totalAmount.multiply(BigDecimal.valueOf(0.10)); // 10% commission
    }


}

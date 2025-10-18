package com.lankatrails.lankatrails_backend.service.utils;

import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.TripParticipant;
import com.lankatrails.lankatrails_backend.model.enums.BudgetCategory;
import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetValidator {

    private final TripPrivilegeUtils tripPrivilegeUtils;

    public void validateUserPrivileges(Trip trip, Long userId, TripPrivilege privilege) {
        TripParticipant participant = trip.getParticipants().stream()
                .filter(p -> p.getTourist().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("User {} is not a participant of trip {}", userId, trip.getTripId());
                    return new BadRequestException("Tourist not part of the trip");
                });

        if (!tripPrivilegeUtils.hasPrivilege(participant.getTripRole(), privilege)) {
            log.error("User {} does not have privilege {} for trip {}", userId, privilege, trip.getTripId());
            throw new UnauthorizedException("No permission to set budget limits");
        }
    }

    public void validateLimitAmount(BigDecimal limitAmount) {
        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Limit amount must be greater than zero");
        }
    }

    public void validateAgainstSpent(BigDecimal spentAmount, BigDecimal newLimit) {
        if (spentAmount.compareTo(newLimit) > 0) {
            throw new BadRequestException("Limit cannot be less than already spent amount");
        }
    }

    public void validateAgainstTotalBudget(Trip trip, BigDecimal proposedCategoryTotal) {
        if (trip.getTotalBudgetLimit().compareTo(BigDecimal.ZERO) > 0 &&
                proposedCategoryTotal.compareTo(trip.getTotalBudgetLimit()) > 0) {
            log.error("Proposed category total {} exceeds trip total limit {} for trip {}",
                    proposedCategoryTotal, trip.getTotalBudgetLimit(), trip.getTripId());
            throw new BadRequestException("Category limits cannot exceed total trip budget limit");
        }
    }

    public void validateAgainstTotalBudgetCategory(BigDecimal totalBudgetCategoryLimit, BigDecimal newTotalBudgetLimit) {
        if (totalBudgetCategoryLimit.compareTo(BigDecimal.ZERO) > 0 &&
                newTotalBudgetLimit.compareTo(totalBudgetCategoryLimit) < 0) {
            log.error("New total budget limit {} is less than existing category limit {}",
                    newTotalBudgetLimit, totalBudgetCategoryLimit);
            throw new BadRequestException("New total budget limit cannot be less than existing total category limits");
        }
    }

    public void ensureCategoryNotExists(Trip trip, BudgetCategory category) {
        if (trip.getTripBudgetCategories().stream()
                .anyMatch(limit -> limit.getBudgetCategory() == category)) {
            throw new BadRequestException("Budget limit for this category already exists");
        }
    }
}

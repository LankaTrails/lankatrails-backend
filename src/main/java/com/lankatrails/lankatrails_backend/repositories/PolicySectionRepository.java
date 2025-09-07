package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.dtos.request.PolicySectionRequest;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicySectionRepository extends JpaRepository<PolicySection,Long> {
    List<PolicySection> findByServices_ServiceId(Long Id);
    PolicySection findByHeading(String heading);
    List<PolicySection> findByProvider_UserIdAndCategoryIsNull(Long providerId);
    List<PolicySection> findByProvider_UserIdAndCategory_CategoryIdOrCategoryIsNull(Long providerId, Long Id);
//    List<PolicySection> findByCategory_CategoryId(Integer categoryId);
    List<PolicySection> findByProvider_UserIdAndCategory_CategoryId(Long providerId, Long Id);

    @Query("SELECT p FROM PolicySection p WHERE p.provider.userId = :providerId AND (p.category IS NULL OR p.category.categoryId = :categoryId)")
    List<PolicySection> findByProviderIdAndCategoryIdOrNull(@Param("providerId") Long providerId, @Param("categoryId") Long categoryId);

}

package com.lankatrails.lankatrails_backend.security.service;

import com.lankatrails.lankatrails_backend.model.ContactPerson;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Resource-level authorization checks.
 * <p>
 * Used from {@code @PreAuthorize("@ownership.isServiceOwner(#id)")} on controllers and
 * from the {@code assert*} methods inside services. Lookups for ids that do not exist
 * return {@code true} so the handler can answer with its usual 404.
 */
@Component("ownership")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnershipService {

    private final ServiceRepository serviceRepository;
    private final PolicySectionRepository policySectionRepository;
    private final TabsSectionRepository tabsSectionRepository;
    private final ProviderRepository providerRepository;
    private final LicenseRepository licenseRepository;

    public boolean isSelf(Long userId) {
        Long current = currentUserId();
        return current != null && current.equals(userId);
    }

    public boolean isServiceOwner(Long serviceId) {
        if (serviceId == null) {
            return false;
        }
        return serviceRepository.findById(serviceId)
                .map(this::ownsService)
                .orElse(true);
    }

    public boolean isPolicyOwner(Long policyId) {
        if (policyId == null) {
            return false;
        }
        return policySectionRepository.findById(policyId)
                .map(this::ownsPolicy)
                .orElse(true);
    }

    public boolean isTabOwner(Long tabId) {
        if (tabId == null) {
            return false;
        }
        return tabsSectionRepository.findById(tabId)
                .map(tab -> ownsService(tab.getService()))
                .orElse(true);
    }

    /**
     * Whether a private upload (identity document, licence, business registration)
     * belongs to the current provider. {@code fileUrl} is the stored "/uploads/..." path.
     */
    public boolean isOwnPrivateDocument(String fileUrl) {
        Long current = currentUserId();
        if (current == null || fileUrl == null) {
            return false;
        }
        Provider provider = providerRepository.findByUserId(current).orElse(null);
        if (provider == null) {
            return false;
        }
        if (fileUrl.equals(provider.getBusinessRegistrationUrl())) {
            return true;
        }
        ContactPerson contactPerson = provider.getContactPerson();
        if (contactPerson != null && fileUrl.equals(contactPerson.getIdentityDocumentUrl())) {
            return true;
        }
        return licenseRepository.findByProvider_UserId(current).stream()
                .map(License::getLicenseUrl)
                .anyMatch(fileUrl::equals);
    }

    public void assertOwnsService(Service service) {
        if (!ownsService(service)) {
            throw new AccessDeniedException("Service does not belong to the current provider");
        }
    }

    /** A policy may only be attached to or edited through a service of the same provider. */
    public void assertPolicyBelongsTo(PolicySection policy, Service service) {
        if (policy.getProvider() == null || service.getProvider() == null
                || !Objects.equals(policy.getProvider().getUserId(), service.getProvider().getUserId())) {
            throw new AccessDeniedException("Policy does not belong to this provider");
        }
    }

    public void assertTabBelongsTo(TabsSection tab, Service service) {
        if (tab.getService() == null
                || !Objects.equals(tab.getService().getServiceId(), service.getServiceId())) {
            throw new AccessDeniedException("Tab does not belong to this service");
        }
    }

    public void assertOwnsTab(TabsSection tab) {
        assertOwnsService(tab.getService());
    }

    public void assertOwnsImage(Image image) {
        assertOwnsService(image.getService());
    }

    private boolean ownsService(Service service) {
        Long current = currentUserId();
        return current != null
                && service != null
                && service.getProvider() != null
                && current.equals(service.getProvider().getUserId());
    }

    private boolean ownsPolicy(PolicySection policy) {
        Long current = currentUserId();
        return current != null
                && policy.getProvider() != null
                && current.equals(policy.getProvider().getUserId());
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl user) {
            return user.getId();
        }
        return null;
    }
}

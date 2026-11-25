package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.ConflictException;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.ServiceStatus;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.impl.AuthServiceImpl;
import com.lankatrails.lankatrails_backend.service.impl.PaymentServiceImpl;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import com.lankatrails.lankatrails_backend.service.impl.ProviderServiceImpl;
import com.lankatrails.lankatrails_backend.service.impl.serviceImpl;
import com.lankatrails.lankatrails_backend.service.payment.ProviderOnboardingService;
import com.lankatrails.lankatrails_backend.service.payment.StripeGateway;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProviderAndPaymentFixesTest {

    private static final long PROVIDER_ID = 10L;

    static Provider provider() {
        Provider provider = new Provider();
        provider.setUserId(PROVIDER_ID);
        provider.setEmail("provider@test.dev");
        provider.setBusinessName("Ella Stays");
        provider.setAccommodationApprovalStatus(ApprovalStatus.APPROVED);
        provider.setTransportApprovalStatus(ApprovalStatus.PENDING);
        return provider;
    }

    static License licence(long id, ServiceCategory category, Provider owner) {
        License licence = new License();
        licence.setLicenseId(id);
        licence.setLicenseNumber("LIC-" + id);
        licence.setExpiryDate(LocalDate.of(2027, 1, 1));
        licence.setLicenseUrl("/uploads/licence/" + id + ".pdf");
        licence.setCategory(new Category(category));
        licence.setProvider(owner);
        return licence;
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PolicyDelete {
        @Mock PolicySectionRepository policySectionRepository;
        @InjectMocks PolicyImpl policyImpl;

        @Test
        void detachesThePolicyFromItsServicesBeforeDeleting() {
            PolicySection policy = new PolicySection();
            policy.setId(5L);
            Service service = new Service();
            service.getPolicies().add(policy);
            policy.getServices().add(service);
            when(policySectionRepository.findById(5L)).thenReturn(Optional.of(policy));

            policyImpl.removePolicies(5L);

            assertThat(service.getPolicies()).doesNotContain(policy);
            assertThat(policy.getServices()).isEmpty();
            verify(policySectionRepository).delete(policy);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class TourGuideDelete {
        @Mock ServiceRepository serviceRepository;
        @InjectMocks serviceImpl servicesForAll;

        @Test
        void deactivationIsPersisted() {
            Service service = new Service();
            service.setServiceId(3L);
            service.setStatus(ServiceStatus.ACTIVE);
            when(serviceRepository.findById(3L)).thenReturn(Optional.of(service));

            servicesForAll.removeService(3L);

            assertThat(service.getStatus()).isEqualTo(ServiceStatus.INACTIVE);
            verify(serviceRepository).save(service);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ProviderProfileAndLicences {
        @Mock AuthUtils authUtils;
        @Mock ProviderRepository providerRepository;
        @Mock LicenseRepository licenseRepository;
        @Mock FileUploadService fileUploadService;
        @InjectMocks ProviderServiceImpl providerService;

        @Test
        void contactPersonIsCreatedWithUploadedIdentityDocument() {
            Provider provider = provider();
            when(authUtils.loggedInUserId()).thenReturn(PROVIDER_ID);
            when(providerRepository.findByUserId(PROVIDER_ID)).thenReturn(Optional.of(provider));
            MockMultipartFile id = new MockMultipartFile("identityDocument", "nic.pdf", "application/pdf", new byte[]{1});
            when(fileUploadService.storeFile(id, UploadCategory.IDENTIFICATION, null)).thenReturn("/uploads/identification/nic.pdf");

            ContactPersonDTO request = new ContactPersonDTO();
            request.setName(" Nimal Perera ");
            request.setEmail("nimal@test.dev");
            request.setPhoneNumber("0771234567");
            request.setPosition("Manager");

            var response = providerService.updateContactPerson(request, id);

            assertThat(provider.getContactPerson().getName()).isEqualTo("Nimal Perera");
            assertThat(provider.getContactPerson().getIdentityDocumentUrl()).isEqualTo("/uploads/identification/nic.pdf");
            assertThat(response.getData().getIdentityDocumentUrl()).isEqualTo("/uploads/identification/nic.pdf");
            verify(providerRepository).save(provider);
        }

        @Test
        void contactPersonNeedsAName() {
            assertThatThrownBy(() -> providerService.updateContactPerson(new ContactPersonDTO(), null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void getLicenseReturnsTheLicenceWithItsCategoryApprovalStatus() {
            when(licenseRepository.findById(1L)).thenReturn(Optional.of(licence(1L, ServiceCategory.TRANSPORT, provider())));

            LicenseDTO dto = providerService.getLicense(1L).getData().getContent().get(0);

            assertThat(dto.getLicenseNumber()).isEqualTo("LIC-1");
            assertThat(dto.getStatus()).isEqualTo(ApprovalStatus.PENDING);
            assertThat(dto.getBusinessName()).isEqualTo("Ella Stays");
            assertThat(dto.getProviderId()).isEqualTo(PROVIDER_ID);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class AdminLicenceReview {
        @Mock ProviderRepository providerRepository;
        @Mock LicenseRepository licenseRepository;
        @InjectMocks AuthServiceImpl authService;

        @Test
        void groupsTheProvidersLicencesByCategory() {
            Provider provider = provider();
            when(providerRepository.findByUserId(PROVIDER_ID)).thenReturn(Optional.of(provider));
            when(licenseRepository.findByProvider_UserId(PROVIDER_ID)).thenReturn(List.of(
                    licence(1L, ServiceCategory.ACCOMMODATION, provider),
                    licence(2L, ServiceCategory.TRANSPORT, provider)));

            var content = authService.approveProviderService(PROVIDER_ID).getData().getContent();

            assertThat(content.getAccommodation()).extracting(LicenseDTO::getLicenseId).containsExactly(1L);
            assertThat(content.getAccommodation().get(0).getStatus()).isEqualTo(ApprovalStatus.APPROVED);
            assertThat(content.getTransport()).extracting(LicenseDTO::getLicenseId).containsExactly(2L);
            assertThat(content.getActivity()).isEmpty();
        }
    }

    @Nested
    class PaymentGuard {
        private final PaymentServiceImpl paymentService = new PaymentServiceImpl();

        @Test
        void providerWithoutStripeAccountGetsConflict() {
            assertThatThrownBy(() -> paymentService.assertProviderCanReceivePayments(provider()))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("no Stripe Connect account");
        }

        @Test
        void providerWithStripeAccountIsAccepted() {
            Provider provider = provider();
            provider.setStripeAccountId("acct_123");
            paymentService.assertProviderCanReceivePayments(provider);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ConnectOnboarding {
        @Mock StripeGateway stripeGateway;
        @Mock ProviderRepository providerRepository;
        @Mock AuthUtils authUtils;
        @InjectMocks ProviderOnboardingService onboardingService;

        private void configure() {
            ReflectionTestUtils.setField(onboardingService, "connectCountry", "US");
            ReflectionTestUtils.setField(onboardingService, "providerDashboardUrl", "https://provider.example.com");
            when(authUtils.loggedInUserId()).thenReturn(PROVIDER_ID);
        }

        @Test
        void firstCallCreatesTheAccountWithAnIdempotencyKey() throws Exception {
            configure();
            Provider provider = provider();
            when(providerRepository.findByUserId(PROVIDER_ID)).thenReturn(Optional.of(provider));
            when(stripeGateway.createExpressAccount("provider@test.dev", "US", "connect-account-provider-10")).thenReturn("acct_new");
            when(stripeGateway.createOnboardingLink(eq("acct_new"), anyString(), anyString())).thenReturn("https://connect.stripe.com/x");

            assertThat(onboardingService.createOnboardingLink()).isEqualTo("https://connect.stripe.com/x");
            assertThat(provider.getStripeAccountId()).isEqualTo("acct_new");
            verify(stripeGateway).createOnboardingLink("acct_new",
                    "https://provider.example.com/provider/onboarding/refresh",
                    "https://provider.example.com/provider/onboarding/return");
        }

        @Test
        void laterCallsReuseTheExistingAccount() throws Exception {
            configure();
            Provider provider = provider();
            provider.setStripeAccountId("acct_existing");
            when(providerRepository.findByUserId(PROVIDER_ID)).thenReturn(Optional.of(provider));
            when(stripeGateway.createOnboardingLink(eq("acct_existing"), anyString(), anyString())).thenReturn("https://connect.stripe.com/y");

            onboardingService.createOnboardingLink();

            verify(stripeGateway, never()).createExpressAccount(anyString(), anyString(), anyString());
            verify(providerRepository, never()).save(any());
        }
    }
}

package com.lankatrails.lankatrails_backend.web;

import com.lankatrails.lankatrails_backend.config.WebSecurityConfig;
import com.lankatrails.lankatrails_backend.controller.AdminController;
import com.lankatrails.lankatrails_backend.controller.ChatRestController;
import com.lankatrails.lankatrails_backend.controller.ComplaintController;
import com.lankatrails.lankatrails_backend.controller.ProviderController;
import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.dtos.request.AcceptRejectDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ComplaintDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.security.jwt.AuthEntryPointJwt;
import com.lankatrails.lankatrails_backend.security.jwt.AuthTokenFilter;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.OwnershipService;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsServiceImpl;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AuthService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.ChatService;
import com.lankatrails.lankatrails_backend.service.ComplaintService;
import com.lankatrails.lankatrails_backend.service.ProviderInsightsService;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import com.lankatrails.lankatrails_backend.service.TypingService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The request shapes the mobile app and dashboards actually send must bind to the backend. */
@WebMvcTest(controllers = {ComplaintController.class, AdminController.class, ChatRestController.class, ProviderController.class})
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, AuthTokenFilter.class, JwtUtils.class, OwnershipService.class})
@TestPropertySource(properties = {
        "spring.app.jwtSecret=bGFua2F0cmFpbHMtdGVzdC1vbmx5LXNpZ25pbmcta2V5LTAxMjM0NTY3ODktYWJjZGVmZ2hpamtsbW5vcA==",
        "spring.app.jwtExpirationMs=3600000",
        "spring.app.jwtRefreshExpirationMs=604800000",
        "spring.app.emailVerificationTokenExpirationMs=900000",
        "spring.app.jwtCookieName=lankatrails_jwt",
        "spring.app.refreshTokenCookieName=lankatrails_refresh_token",
        "app.cors.allowed-origins=http://localhost:5173"
})
class ApiContractTest {

    @TempDir
    static Path uploadRoot;

    @DynamicPropertySource
    static void uploadRootProperty(DynamicPropertyRegistry registry) {
        registry.add("app.upload.root-path", () -> uploadRoot.toString());
    }

    @Autowired private MockMvc mvc;

    @MockitoBean private ComplaintService complaintService;
    @MockitoBean private FileUploadService fileUploadService;
    @MockitoBean private AuthService authService;
    @MockitoBean private BookingService bookingService;
    @MockitoBean private ChatService chatService;
    @MockitoBean private TypingService typingService;
    @MockitoBean private AuthUtils authUtils;
    @MockitoBean private PolicyImpl policyImpl;
    @MockitoBean private ProviderService providerService;
    @MockitoBean private ProviderInsightsService providerInsightsService;
    @MockitoBean private ProviderRepository providerRepository;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private ServiceRepository serviceRepository;
    @MockitoBean private PolicySectionRepository policySectionRepository;
    @MockitoBean private TabsSectionRepository tabsSectionRepository;
    @MockitoBean private LicenseRepository licenseRepository;

    private final UserDetailsImpl tourist = principal(1L, UserRole.ROLE_TOURIST);
    private final UserDetailsImpl provider = principal(10L, UserRole.ROLE_PROVIDER);
    private final UserDetailsImpl admin = principal(99L, UserRole.ROLE_ADMIN);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void stubs() {
        APIResponse ok = APIResponse.builder().success(true).build();
        when(complaintService.addNewComplaint(any())).thenReturn(ok);
        when(authService.approveOrRejectRequest(any())).thenReturn(ok);
        when(authService.approveProviderService(anyLong())).thenReturn(ok);
        when(policyImpl.removePolicies(anyLong())).thenReturn(ok);
        when(providerService.updateContactPerson(any(), any())).thenReturn(ok);
        when(authUtils.loggedInUserId()).thenReturn(10L);
    }

    @Test
    void serviceComplaintBindsServiceIdFromPathAndJsonBody() throws Exception {
        mvc.perform(post("/api/tourist/make-complaint/{serviceId}", 55)
                        .with(user(tourist))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Dirty room\",\"bookingId\":7,\"complaintImgs\":[]}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<ComplaintDTO> dto = ArgumentCaptor.forClass(ComplaintDTO.class);
        verify(complaintService).addNewComplaint(dto.capture());
        assertThat(dto.getValue().getServiceId()).isEqualTo(55L);
        assertThat(dto.getValue().getDescription()).isEqualTo("Dirty room");
        assertThat(dto.getValue().getBookingId()).isEqualTo(7L);
    }

    @Test
    void approveOrRejectTakesTheProviderFromThePath() throws Exception {
        mvc.perform(put("/api/admin/approve-provider/providers/{providerId}", 10)
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"providerId\":999,\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AcceptRejectDTO> dto = ArgumentCaptor.forClass(AcceptRejectDTO.class);
        verify(authService).approveOrRejectRequest(dto.capture());
        assertThat(dto.getValue().getProviderId()).isEqualTo(10L);
    }

    @Test
    void adminCanReviewAProvidersLicencesByCategory() throws Exception {
        mvc.perform(get("/api/admin/approve-provider-service/{providerId}", 10).with(user(admin)))
                .andExpect(status().isOk());
        verify(authService).approveProviderService(10L);
    }

    @Test
    void providerDashboardFileUploadShapeIsAccepted() throws Exception {
        // PRV/services/chatService.ts: POST /chat/rooms/{roomId}/send-file, "messageData" as a plain string
        MockMultipartFile file = new MockMultipartFile("file", "menu.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1});
        mvc.perform(multipart("/api/chat/rooms/{roomId}/send-file", 5)
                        .file(file)
                        .param("messageData", "{\"messageType\":\"IMAGE\",\"content\":\"menu\",\"caption\":\"ignored\"}")
                        .with(user(provider)))
                .andExpect(status().isCreated());

        ArgumentCaptor<ChatMessageDto> dto = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(chatService).processMessage(dto.capture(), eq(10L), any());
        assertThat(dto.getValue().getChatRoomId()).isEqualTo(5L);
        assertThat(dto.getValue().getMessageType()).isEqualTo(ChatMessageType.IMAGE);
    }

    @Test
    void mobileFileUploadShapeIsAccepted() throws Exception {
        // MOB/services/chatService.ts: POST /chat/{roomId}/send-file, "message" as a JSON part
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
        MockMultipartFile message = new MockMultipartFile("message", "", MediaType.APPLICATION_JSON_VALUE,
                "{\"messageType\":\"FILE\",\"chatRoomId\":123}".getBytes());
        mvc.perform(multipart("/api/chat/{roomId}/send-file", 5).file(file).file(message).with(user(tourist)))
                .andExpect(status().isCreated());

        ArgumentCaptor<ChatMessageDto> dto = ArgumentCaptor.forClass(ChatMessageDto.class);
        verify(chatService).processMessage(dto.capture(), any(), any());
        assertThat(dto.getValue().getChatRoomId()).as("path wins over body").isEqualTo(5L);
    }

    @Test
    void markAsReadPassesTheRealUser() throws Exception {
        mvc.perform(put("/api/chat/messages/{id}/read", "m1").with(user(provider))).andExpect(status().isOk());
        verify(chatService).markMessageAsRead("m1", 10L);
    }

    @Test
    void policyDeleteUsesDeleteWithPathId() throws Exception {
        mvc.perform(delete("/api/provider/delete/policy/{id}", 8).with(user(provider))).andExpect(status().isOk());
        verify(policyImpl).removePolicies(8L);
    }

    @Test
    void contactPersonUpdateAcceptsTheDashboardMultipart() throws Exception {
        MockMultipartFile contact = new MockMultipartFile("contactPerson", "", MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Nimal\",\"position\":\"Manager\",\"email\":\"n@test.dev\",\"phoneNumber\":\"0771234567\"}".getBytes());
        mvc.perform(multipart("/api/provider/contact-person").file(contact)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(user(provider)))
                .andExpect(status().isOk());

        ArgumentCaptor<ContactPersonDTO> dto = ArgumentCaptor.forClass(ContactPersonDTO.class);
        verify(providerService).updateContactPerson(dto.capture(), isNull());
        assertThat(dto.getValue().getName()).isEqualTo("Nimal");
    }

    @Test
    void uploadsAreServedFromTheConfiguredRoot() throws Exception {
        Path dir = Files.createDirectories(uploadRoot.resolve("service-pictures"));
        Files.write(dir.resolve("hotel.jpg"), new byte[]{9, 8, 7});

        mvc.perform(get("/uploads/service-pictures/hotel.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{9, 8, 7}));
    }

    private static UserDetailsImpl principal(long id, UserRole role) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(role.name().toLowerCase() + "@test.dev");
        user.setPassword("n/a");
        user.setEmailVerified(true);
        user.setRole(role);
        return UserDetailsImpl.build(user);
    }
}

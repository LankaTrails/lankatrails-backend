package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.BusinessDetailDTO;
import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.BusinessResponseDTO;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponse;
import com.lankatrails.lankatrails_backend.dtos.response.LicenseResponseDTO;
import com.lankatrails.lankatrails_backend.exception.ResourceNotFoundException;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ProviderService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProviderServiceImpl implements ProviderService {
    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public APIResponse<BusinessResponseDTO> getBusinessDetails() {
        BusinessDetailDTO businessDetail = new BusinessDetailDTO();
        // Assuming the provider is fetched from the AuthUtils
        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + authUtils.loggedInUserId()));

        if (provider == null) {
            return APIResponse.<BusinessResponseDTO>builder()
                    .success(false)
                    .message("Provider not found")
                    .build();
        }

        // Populate business details from the provider
        businessDetail.setProviderId(provider.getUserId());
        businessDetail.setBusinessType(provider.getBusinessType());
        businessDetail.setBusinessRegistrationNumber(provider.getBusinessRegistrationNumber());
        businessDetail.setBusinessRegistrationUrl(provider.getBusinessRegistrationUrl());
        businessDetail.setContactPerson(modelMapper.map(provider.getContactPerson(), ContactPersonDTO.class));


        List<License> licenses = licenseRepository.findByProvider_UserId(authUtils.loggedInUserId());
//                .orElseThrow(()->new ResourceNotFoundException("License", authUtils.loggedInUserId()));

        List<LicenseDTO> licenseDTOList = new ArrayList<>();
        for (License license : licenses) {
            LicenseDTO licenseDTO = new LicenseDTO();
            licenseDTO.setLicenseUrl(license.getLicenseUrl());
            licenseDTO.setCategory(license.getCategory().getCategoryName());
            licenseDTOList.add(licenseDTO);
        }
        businessDetail.setLicenseDTOList(licenseDTOList);

        BusinessResponseDTO businessResponseDTO = new BusinessResponseDTO();
        businessResponseDTO.setContent(businessDetail);

        return APIResponse.<BusinessResponseDTO>builder()
                .success(true)
                .message("Business details retrieved successfully")
                .data(businessResponseDTO)
                .build();
    }
//    @Override
//    public APIResponse<BusinessResponseDTO> getBusinessDetails2() {
//        BusinessDetailDTO businessDetail = new BusinessDetailDTO();
//        // Assuming the provider is fetched from the AuthUtils
//        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
//                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + authUtils.loggedInUserId()));
//
////        if (provider == null) {
////            return APIResponse.<BusinessDetailDTO>builder()
////                    .success(false)
////                    .message("Provider not found")
////                    .build();
////        }
//
//        // Populate business details from the provider
//        businessDetail.setProviderId(provider.getUserId());
//        businessDetail.setBusinessType(provider.getBusinessType());
//        businessDetail.setBusinessRegistrationNumber(provider.getBusinessRegistrationNumber());
//        businessDetail.setBusinessRegistrationUrl(provider.getBusinessRegistrationUrl());
//        businessDetail.setContactPerson(modelMapper.map(provider.getContactPerson(), ContactPersonDTO.class));
//
//        List<License> licenses = licenseRepository.findByProvider_UserId(authUtils.loggedInUserId());

    /// /                .orElseThrow(()->new ResourceNotFoundException("License", authUtils.loggedInUserId()));
//
//        List<LicenseDTO> licenseDTOList = new ArrayList<>();
//        for (License license:licenses){
//            LicenseDTO licenseDTO = new LicenseDTO();
//            licenseDTO.setLicenseUrl(license.getLicenseUrl());
//            licenseDTO.setCategory(license.getCategory().getCategoryName());
//            licenseDTOList.add(licenseDTO);
//        }
//        businessDetail.setLicenseDTOList(licenseDTOList);
//
//        BusinessResponseDTO businessResponseDTO =new BusinessResponseDTO();
//        businessResponseDTO.setContent(businessDetail);
//
//        return APIResponse.<BusinessResponseDTO>builder()
//                .success(true)
//                .message("Business details retrieved successfully")
//                .data(businessResponseDTO)
//                .build();
//    }
    @Override
    public APIResponse<String> licenseRenewal(List<LicenseDTO> licenseDTO, List<MultipartFile> licenseFiles) {
        Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId()).orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));

        //Add all the licenses to the license repository
        if (!licenseDTO.isEmpty()) {
            // Handle license files upload
            List<LicenseDTO> licenses = new ArrayList<>(licenseDTO);
            if (licenseFiles != null && !licenseFiles.isEmpty()) {
                if (licenseFiles.size() != licenses.size()) {
                    return APIResponse.<String>builder()
                            .success(false)
                            .message("Number of license files must match the number of licenses")
                            .data("")
                            .build();
                }

                for (int i = 0; i < licenseFiles.size(); i++) {
                    LicenseDTO license = licenses.get(i);
                    MultipartFile licenseFile = licenseFiles.get(i);

                    // Upload file and set URL
                    String fileUrl = fileUploadService.storeFile(licenseFile, UploadCategory.LICENCE, license.getCategory().getDisplayName().toLowerCase());
                    license.setLicenseUrl(fileUrl);
                    log.info("License file uploaded successfully for license number {}: {}", license.getLicenseNumber(), license.getLicenseUrl());

                    // Additional validation
                    if (license.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                        return APIResponse.<String>builder()
                                .success(false)
                                .message(String.format("License %s expired on %s", license.getLicenseNumber(), license.getExpiryDate()))
                                .data("")
                                .build();
                    }
                }
            }

            for (LicenseDTO license : licenses) {
                Category category = categoryRepository.findByCategoryName(license.getCategory()).orElseThrow(() -> new ResourceNotFoundException("Category", license.getCategory().getDisplayName()));
                License setLicense = new License();
                setLicense.setLicenseNumber(license.getLicenseNumber());
                setLicense.setExpiryDate(license.getExpiryDate());
                setLicense.setLicenseUrl(license.getLicenseUrl());
                setLicense.setCategory(category);
                setLicense.setProvider(provider);

                //Update the status of the provider's respective category to RENEWAL
                if (category.getCategoryName() == ServiceCategory.ACCOMMODATION) {
                    provider.setAccommodationApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.TOUR_GUIDE) {
                    provider.setTourGuideApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.TRANSPORT) {
                    provider.setTransportApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.FOOD_BEVERAGE) {
                    provider.setFoodApprovalStatus(ApprovalStatus.RENEWAL);
                } else if (category.getCategoryName() == ServiceCategory.ACTIVITY) {
                    provider.setActivityApprovalStatus(ApprovalStatus.RENEWAL);
                }
                //save the license
                licenseRepository.save(setLicense);
                //update the provider entity
                providerRepository.save(provider);

            }
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Licenses provided for verification")
                    .data("")
                    .build();

        } else {
            return APIResponse.<String>builder()
                    .success(false)
                    .message("No licenses added")
                    .data("")
                    .build();
        }


    }

    public APIResponse<String> requestApproval(List<LicenseDTO> licenseDTO, List<MultipartFile> licenseFiles) {
        Provider provider = providerRepository.findByUserId(authUtils.loggedInUserId()).orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));

        //Add all the licenses to the license repository
        if (!licenseDTO.isEmpty()) {
            // Handle license files upload
            List<LicenseDTO> licenses = new ArrayList<>(licenseDTO);
            if (licenseFiles != null && !licenseFiles.isEmpty()) {
                if (licenseFiles.size() != licenses.size()) {
                    return APIResponse.<String>builder()
                            .success(false)
                            .message("Number of license files must match the number of licenses")
                            .data("")
                            .build();
                }

                for (int i = 0; i < licenseFiles.size(); i++) {
                    LicenseDTO license = licenses.get(i);
                    MultipartFile licenseFile = licenseFiles.get(i);

                    // Upload file and set URL
                    String fileUrl = fileUploadService.storeFile(licenseFile, UploadCategory.LICENCE, license.getCategory().getDisplayName().toLowerCase());
                    license.setLicenseUrl(fileUrl);
                    log.info("License file uploaded successfully for license number {}: {}", license.getLicenseNumber(), license.getLicenseUrl());

                    // Additional validation
                    if (license.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                        return APIResponse.<String>builder()
                                .success(false)
                                .message(String.format("License %s expired on %s", license.getLicenseNumber(), license.getExpiryDate()))
                                .data("")
                                .build();
                    }
                }
            }

            for (LicenseDTO license : licenses) {
                Category category = categoryRepository.findByCategoryName(license.getCategory()).orElseThrow(() -> new ResourceNotFoundException("Category", license.getCategory().getDisplayName()));
                License setLicense = new License();
                setLicense.setLicenseNumber(license.getLicenseNumber());
                setLicense.setExpiryDate(license.getExpiryDate());
                setLicense.setLicenseUrl(license.getLicenseUrl());
                setLicense.setCategory(category);
                setLicense.setProvider(provider);

                //Update the status of the provider's respective category to PENDING
                if (category.getCategoryName() == ServiceCategory.ACCOMMODATION) {
                    provider.setAccommodationApprovalStatus(ApprovalStatus.PENDING);
                } else if (category.getCategoryName() == ServiceCategory.TOUR_GUIDE) {
                    provider.setTourGuideApprovalStatus(ApprovalStatus.PENDING);
                } else if (category.getCategoryName() == ServiceCategory.TRANSPORT) {
                    provider.setTransportApprovalStatus(ApprovalStatus.PENDING);
                } else if (category.getCategoryName() == ServiceCategory.FOOD_BEVERAGE) {
                    provider.setFoodApprovalStatus(ApprovalStatus.PENDING);
                } else if (category.getCategoryName() == ServiceCategory.ACTIVITY) {
                    provider.setActivityApprovalStatus(ApprovalStatus.PENDING);
                }
                //save the license
                licenseRepository.save(setLicense);
                //update the provider entity
                providerRepository.save(provider);

            }
            return APIResponse.<String>builder()
                    .success(true)
                    .message("Licenses provided for verification")
                    .data("")
                    .build();

        } else {
            return APIResponse.<String>builder()
                    .success(false)
                    .message("No licenses added")
                    .data("")
                    .build();
        }


    }

    @Override
    @Transactional
    public APIResponse<LicenseResponseDTO> getLicenses() {
        List<License> licenses = licenseRepository.findAll();
        List<LicenseDTO> licenseDTOList = new ArrayList<>();
        for (License license : licenses) {
            LicenseDTO licenseDTO = new LicenseDTO();

//            licenseDTO.setCategoryName(license.getCategory().getCategoryName().getDisplayName());

            if (license.getCategory().getCategoryName() == ServiceCategory.ACTIVITY) {
                if (license.getProvider().getActivityApprovalStatus() == ApprovalStatus.PENDING ||
                        license.getProvider().getActivityApprovalStatus() == ApprovalStatus.RENEWAL) {
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setCategoryName("Activity");
                    licenseDTO.setStatus(license.getProvider().getActivityApprovalStatus());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTOList.add(licenseDTO);
                }
            }
            if (license.getCategory().getCategoryName() == ServiceCategory.ACCOMMODATION) {
                if (license.getProvider().getAccommodationApprovalStatus() == ApprovalStatus.PENDING ||
                        license.getProvider().getAccommodationApprovalStatus() == ApprovalStatus.RENEWAL) {
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setCategoryName("Accommodation");
                    licenseDTO.setStatus(license.getProvider().getAccommodationApprovalStatus());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTOList.add(licenseDTO);
                }
            }

            if (license.getCategory().getCategoryName() == ServiceCategory.FOOD_BEVERAGE) {
                if (license.getProvider().getFoodApprovalStatus() == ApprovalStatus.PENDING ||
                        license.getProvider().getFoodApprovalStatus() == ApprovalStatus.RENEWAL) {
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setCategoryName("Food & Beverage");
                    licenseDTO.setStatus(license.getProvider().getFoodApprovalStatus());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTOList.add(licenseDTO);
                }
            }

            if (license.getCategory().getCategoryName() == ServiceCategory.TOUR_GUIDE) {
                if (license.getProvider().getTourGuideApprovalStatus() == ApprovalStatus.PENDING ||
                        license.getProvider().getTourGuideApprovalStatus() == ApprovalStatus.RENEWAL) {
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setCategoryName("Tour Guide");
                    licenseDTO.setStatus(license.getProvider().getTourGuideApprovalStatus());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTOList.add(licenseDTO);
                }
            }

            if (license.getCategory().getCategoryName() == ServiceCategory.TRANSPORT) {
                if (license.getProvider().getTransportApprovalStatus() == ApprovalStatus.PENDING ||
                        license.getProvider().getTransportApprovalStatus() == ApprovalStatus.RENEWAL) {
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setCategoryName("Transport");
                    licenseDTO.setStatus(license.getProvider().getTransportApprovalStatus());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setBusinessName(license.getProvider().getBusinessName());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTOList.add(licenseDTO);
                }
            }
        }
        LicenseResponseDTO licenseResponseDTO = new LicenseResponseDTO();
        licenseResponseDTO.setContent(licenseDTOList);
        return APIResponse.<LicenseResponseDTO>builder()
                .success(true)
                .message("Loaded licenses successfully")
                .data(licenseResponseDTO)
                .build();
    }


    @Override
    public APIResponse<LicenseResponseDTO> getLicense(Long id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License", id));
        //find the provider details
        return null;
    }

    @Override
    public APIResponse<Set<LicenseResponse>> getLicenseForProvider() {
        Provider provider = providerRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", authUtils.loggedInUserId()));

        Set<LicenseResponse> allLicenseResponses = new HashSet<>();
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            List<License> licenses = licenseRepository.findByProvider_UserIdAndCategory_CategoryId(provider.getUserId(), category.getCategoryId());

            if (!licenses.isEmpty()) {
                // Convert licenses to LicenseDTO list
                List<LicenseDTO> licenseDTOList = licenses.stream().map(license -> {
                    LicenseDTO licenseDTO = new LicenseDTO();
                    licenseDTO.setLicenseId(license.getLicenseId());
                    licenseDTO.setLicenseNumber(license.getLicenseNumber());
                    licenseDTO.setLicenseUrl(license.getLicenseUrl());
                    licenseDTO.setExpiryDate(license.getExpiryDate());
                    licenseDTO.setCategory(license.getCategory().getCategoryName());
                    return licenseDTO;
                }).collect(Collectors.toList());

                // Get approval status for the category
                ApprovalStatus approvalStatus = getApprovalStatusForCategory(provider, category.getCategoryName());

                // Create license response
                LicenseResponse licenseResponse = new LicenseResponse();
                licenseResponse.setLicenses(licenseDTOList);
                licenseResponse.setServiceCategory(category.getCategoryName());
                licenseResponse.setApprovalStatus(approvalStatus);

                allLicenseResponses.add(licenseResponse);
            }
        }

        return APIResponse.<Set<LicenseResponse>>builder()
                .success(true)
                .message("Licenses retrieved successfully")
                .data(allLicenseResponses)
                .build();
    }

    private ApprovalStatus getApprovalStatusForCategory(Provider provider, ServiceCategory category) {
        return switch (category) {
            case ACCOMMODATION -> provider.getAccommodationApprovalStatus();
            case TOUR_GUIDE -> provider.getTourGuideApprovalStatus();
            case TRANSPORT -> provider.getTransportApprovalStatus();
            case FOOD_BEVERAGE -> provider.getFoodApprovalStatus();
            case ACTIVITY -> provider.getActivityApprovalStatus();
            default -> null;
        };
    }
}

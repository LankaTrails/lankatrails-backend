package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    private Integer childCount;
    private Integer adultCount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private BookingStatus bookingStatus;
}

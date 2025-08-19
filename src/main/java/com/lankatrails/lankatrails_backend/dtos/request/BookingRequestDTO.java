package com.lankatrails.lankatrails_backend.dtos.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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

//    @JsonDeserialize(using = LocalTimeDeserializer.class)
//    @JsonSerialize(using = LocalTimeSerializer.class)
//    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime fromTime;

//    @JsonDeserialize(using = LocalTimeDeserializer.class)
//    @JsonSerialize(using = LocalTimeSerializer.class)
//    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime toTime;

    private BookingStatus bookingStatus;
}

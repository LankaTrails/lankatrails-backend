package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.ComplaintViewDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintViewResponse {
    private List<ComplaintViewDTO> content;
}

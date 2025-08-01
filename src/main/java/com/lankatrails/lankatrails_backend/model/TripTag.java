package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.TripTagType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_tags")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_name", nullable = false, unique = true)
    private TripTagType tagName;

    public TripTag(TripTagType tagType) {
        this.tagName = tagType;
    }
}

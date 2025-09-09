package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "group_chat_room")
@Getter
@Setter
@NoArgsConstructor
public class GroupChatRoom extends ChatRoom {
    @ManyToMany
    @JoinTable(
            name = "group_chat_room_participants",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    private List<TripParticipant> participants;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", referencedColumnName = "trip_id")
    private Trip trip;
}

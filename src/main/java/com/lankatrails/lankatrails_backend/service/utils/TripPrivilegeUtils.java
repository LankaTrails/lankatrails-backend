package com.lankatrails.lankatrails_backend.service.utils;

import com.lankatrails.lankatrails_backend.model.enums.TripPrivilege;
import com.lankatrails.lankatrails_backend.model.enums.TripRole;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class TripPrivilegeUtils {

    // Default privileges for each trip role
    public static final Map<TripRole, Set<TripPrivilege>> DEFAULT_PRIVILEGES = Map.of(
        TripRole.ADMIN, Set.of(
            TripPrivilege.EDIT_TRIP_DETAILS,
            TripPrivilege.DELETE_TRIP,
            TripPrivilege.ADD_TRIP_ITEMS,
            TripPrivilege.EDIT_TRIP_ITEMS,
            TripPrivilege.DELETE_TRIP_ITEMS,
            TripPrivilege.SET_BUDGET_LIMITS,
            TripPrivilege.ADD_EXPENSES,
            TripPrivilege.EDIT_EXPENSES,
            TripPrivilege.DELETE_EXPENSES,
            TripPrivilege.INVITE_MEMBERS,
            TripPrivilege.REMOVE_MEMBERS,
            TripPrivilege.MANAGE_ROLES,
            TripPrivilege.MANAGE_PARTICIPANTS,
            TripPrivilege.SEND_MESSAGES,
            TripPrivilege.DELETE_MESSAGES,
            TripPrivilege.ADD_BOOKINGS,
            TripPrivilege.CANCEL_BOOKINGS
        ),
        TripRole.EDITOR, Set.of(
                TripPrivilege.SET_BUDGET_LIMITS,
            TripPrivilege.EDIT_TRIP_DETAILS,
            TripPrivilege.ADD_TRIP_ITEMS,
            TripPrivilege.EDIT_TRIP_ITEMS,
            TripPrivilege.DELETE_TRIP_ITEMS,
            TripPrivilege.ADD_EXPENSES,
            TripPrivilege.EDIT_EXPENSES,
            TripPrivilege.SEND_MESSAGES,
            TripPrivilege.DELETE_MESSAGES
        ),
        TripRole.MEMBER, Set.of(
            TripPrivilege.SEND_MESSAGES,
            TripPrivilege.DELETE_MESSAGES,
            TripPrivilege.ADD_EXPENSES,
            TripPrivilege.EDIT_EXPENSES
        ),
        TripRole.VIEWER, Set.of()
    );

    // Get default privileges for a specific trip role
    public Set<TripPrivilege> getDefaultPrivileges(TripRole role) {
        return DEFAULT_PRIVILEGES.getOrDefault(role, Set.of());
    }

    // Check if a role has a specific privilege
    public boolean hasPrivilege(TripRole role, TripPrivilege privilege) {
        Set<TripPrivilege> privileges = getDefaultPrivileges(role);
        return privileges.contains(privilege);
    }

    // Add a privilege to a role
    public Set<TripPrivilege> addPrivilege(Set<TripPrivilege> privileges, TripPrivilege privilege) {
        privileges.add(privilege);
        return privileges;
    }

    // Remove a privilege from a role
    public Set<TripPrivilege> removePrivilege(Set<TripPrivilege> privileges, TripPrivilege privilege) {
        privileges.remove(privilege);
        return privileges;
    }
}

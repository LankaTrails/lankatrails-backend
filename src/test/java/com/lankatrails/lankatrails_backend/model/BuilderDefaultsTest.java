package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.dtos.UserPreferencesDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lombok builders skip field initialisers unless the field is marked {@code @Builder.Default}.
 * Every default the no-args constructor (used by JPA and Jackson) sets must also be set by
 * {@code builder().build()}, so objects built in services don't end up with null collections or flags.
 */
class BuilderDefaultsTest {

    @ParameterizedTest
    @ValueSource(classes = {
            AvailableTime.class, Booking.class, ChatMessage.class, Location.class, Place.class,
            Service.class, Trip.class, TripBudgetCategory.class, TripParticipant.class,
            UserPreferences.class, UserPreferencesDTO.class
    })
    void builderAppliesTheSameDefaultsAsTheConstructor(Class<?> type) throws Exception {
        Object constructed = type.getDeclaredConstructor().newInstance();
        Object builder = type.getMethod("builder").invoke(null);
        Object built = builder.getClass().getMethod("build").invoke(builder);

        int defaults = 0;
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object expected = field.get(constructed);
            if (expected == null) continue;
            defaults++;

            Object actual = field.get(built);
            assertThat(actual).as("%s.%s from builder()", type.getSimpleName(), field.getName()).isNotNull();
            if (expected instanceof Collection<?> collection) {
                assertThat((Collection<?>) actual).as("%s.%s", type.getSimpleName(), field.getName())
                        .isEmpty();
                assertThat(collection).isEmpty();
            } else if (!(expected instanceof Temporal)) {
                // timestamps such as Instant.now() differ between the two calls; only presence matters
                assertThat(actual).as("%s.%s", type.getSimpleName(), field.getName()).isEqualTo(expected);
            }
        }
        assertThat(defaults).as("%s has defaults to check", type.getSimpleName()).isPositive();
    }
}

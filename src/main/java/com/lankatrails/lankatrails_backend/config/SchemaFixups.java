package com.lankatrails.lankatrails_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Column changes that {@code ddl-auto=update} cannot apply to an existing database (it never alters
 * column types). Each statement is guarded so it runs once. Replace with Flyway migrations once a
 * baseline exists.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaFixups implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private record WidenToText(String table, String column) {}

    private static final List<WidenToText> TEXT_COLUMNS = List.of(
            new WidenToText("tabs_section", "content"));

    @Override
    public void run(ApplicationArguments args) {
        for (WidenToText fix : TEXT_COLUMNS) {
            List<String> types = jdbcTemplate.queryForList(
                    "SELECT data_type FROM information_schema.columns "
                            + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
                    String.class, fix.table(), fix.column());
            if (!types.isEmpty() && !"text".equals(types.get(0))) {
                // identifiers come from the constant list above, never from input
                jdbcTemplate.execute("ALTER TABLE " + fix.table() + " ALTER COLUMN " + fix.column() + " TYPE text");
                log.info("Widened {}.{} from {} to text", fix.table(), fix.column(), types.get(0));
            }
        }
    }
}

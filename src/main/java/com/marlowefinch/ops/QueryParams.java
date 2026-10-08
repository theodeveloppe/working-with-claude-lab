package com.marlowefinch.ops;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses and checks the query parameters shared by the dashboard endpoints (TODO-232).
 *
 * Problems are collected rather than thrown one at a time, so a request with a bad
 * {@code from} and a bad {@code limit} gets both messages back. Call {@link #validate()}
 * once every parameter has been read; it throws {@link InvalidParametersException} if
 * anything was wrong, which {@link ApiExceptionHandler} turns into a 400.
 */
final class QueryParams {

    static final int MAX_RANGE_DAYS = 366;
    static final int MIN_LIMIT = 1;
    static final int MAX_LIMIT = 500;

    private static final Pattern ISO_DATE = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private final List<String> errors = new ArrayList<>();

    /**
     * Resolves {@code from}/{@code to}, defaulting a missing bound to the last
     * {@value DateRange#DEFAULT_DAYS} days ending today (from {@code clock}).
     * The range checks only run when both bounds are usable dates.
     * The span limit is an inclusive day count: {@code from} and {@code to} both count.
     */
    DateRange dateRange(String from, String to, Clock clock) {
        LocalDate today = LocalDate.now(clock);
        LocalDate end = isMissing(to) ? today : parseDate("to", to);
        LocalDate start = isMissing(from) ? today.minusDays(DateRange.DEFAULT_DAYS) : parseDate("from", from);
        if (start == null || end == null) {
            return null;
        }
        if (start.isAfter(end)) {
            errors.add("from must be on or before to");
        } else if (ChronoUnit.DAYS.between(start, end) + 1 > MAX_RANGE_DAYS) {
            errors.add("the range from..to may span at most " + MAX_RANGE_DAYS + " days");
        }
        return new DateRange(start, end);
    }

    /** Parses {@code limit}, falling back to {@code defaultLimit} when it is missing. */
    int limit(String raw, int defaultLimit) {
        if (isMissing(raw)) {
            return defaultLimit;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value >= MIN_LIMIT && value <= MAX_LIMIT) {
                return value;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        errors.add("limit must be an integer between " + MIN_LIMIT + " and " + MAX_LIMIT);
        return defaultLimit;
    }

    /** Throws if any parameter read so far was invalid. */
    void validate() {
        if (!errors.isEmpty()) {
            throw new InvalidParametersException(errors);
        }
    }

    private LocalDate parseDate(String name, String raw) {
        if (ISO_DATE.matcher(raw).matches()) {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                // e.g. 2026-02-30: right shape, not a real date
            }
        }
        errors.add(name + " must be an ISO date (YYYY-MM-DD)");
        return null;
    }

    private static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}

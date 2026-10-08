package com.marlowefinch.ops;

import java.time.Clock;
import java.time.LocalDate;

/**
 * A closed date range for the query endpoints.
 *
 * The record itself does not validate: repository code and tests may build any range,
 * and a backwards one simply matches nothing. Request parameters go through
 * {@link #resolve} (or {@link QueryParams} directly), which defaults missing bounds to
 * "the last 30 days ending today" and rejects malformed dates, {@code from} after
 * {@code to}, and spans over {@value QueryParams#MAX_RANGE_DAYS} days. See TODO-232.
 */
public record DateRange(LocalDate from, LocalDate to) {

    public static final int DEFAULT_DAYS = 30;

    /** Resolves and validates request parameters; throws {@link InvalidParametersException} if invalid. */
    public static DateRange resolve(String from, String to, Clock clock) {
        QueryParams params = new QueryParams();
        DateRange range = params.dateRange(from, to, clock);
        params.validate();
        return range;
    }
}

import { bucketOf, groupByBucket, TIME_BUCKETS } from "./timeBucket";

describe("bucketOf", () => {
    test("00:00 -> morning (inclusive lower bound)", () => {
        expect(bucketOf("00:00")).toBe("morning");
    });

    test("11:59 -> morning (inclusive upper bound)", () => {
        expect(bucketOf("11:59")).toBe("morning");
    });

    test("12:00 -> afternoon (inclusive lower bound)", () => {
        expect(bucketOf("12:00")).toBe("afternoon");
    });

    test("16:59 -> afternoon (inclusive upper bound)", () => {
        expect(bucketOf("16:59")).toBe("afternoon");
    });

    test("17:00 -> evening (inclusive lower bound)", () => {
        expect(bucketOf("17:00")).toBe("evening");
    });

    test("23:59 -> evening (inclusive upper bound)", () => {
        expect(bucketOf("23:59")).toBe("evening");
    });

    test("09:30 -> morning", () => {
        expect(bucketOf("09:30")).toBe("morning");
    });

    test("HH:mm:ss is accepted (only HH:mm is used)", () => {
        expect(bucketOf("14:15:00")).toBe("afternoon");
    });

    test("returns null for invalid input", () => {
        expect(bucketOf("")).toBeNull();
        expect(bucketOf(null)).toBeNull();
        expect(bucketOf(undefined)).toBeNull();
        expect(bucketOf("ab:cd")).toBeNull();
    });
});

describe("groupByBucket", () => {
    const appt = (time) => ({ appointmentId: time, date: "2026-04-21", time });

    test("returns empty buckets for empty input", () => {
        const result = groupByBucket([]);
        expect(result).toEqual({ morning: [], afternoon: [], evening: [] });
    });

    test("returns empty buckets for non-array input", () => {
        expect(groupByBucket(null)).toEqual({ morning: [], afternoon: [], evening: [] });
        expect(groupByBucket(undefined)).toEqual({ morning: [], afternoon: [], evening: [] });
    });

    test("groups items into correct buckets", () => {
        const items = [appt("09:00"), appt("13:30"), appt("18:45")];
        const result = groupByBucket(items);
        expect(result.morning).toHaveLength(1);
        expect(result.afternoon).toHaveLength(1);
        expect(result.evening).toHaveLength(1);
        expect(result.morning[0].time).toBe("09:00");
        expect(result.afternoon[0].time).toBe("13:30");
        expect(result.evening[0].time).toBe("18:45");
    });

    test("sorts items within a bucket chronologically even when input is unsorted", () => {
        const items = [appt("11:00"), appt("08:00"), appt("09:30")];
        const result = groupByBucket(items);
        expect(result.morning.map((item) => item.time)).toEqual(["08:00", "09:30", "11:00"]);
        expect(result.afternoon).toEqual([]);
        expect(result.evening).toEqual([]);
    });

    test("skips items with invalid time", () => {
        const items = [appt("09:00"), appt("invalid"), { appointmentId: "x", date: "2026-04-21" }];
        const result = groupByBucket(items);
        expect(result.morning).toHaveLength(1);
        expect(result.afternoon).toEqual([]);
        expect(result.evening).toEqual([]);
    });
});

describe("TIME_BUCKETS", () => {
    test("has morning, afternoon, evening in order with English labels", () => {
        expect(TIME_BUCKETS.map((b) => b.id)).toEqual(["morning", "afternoon", "evening"]);
        expect(TIME_BUCKETS.map((b) => b.label)).toEqual(["Morning", "Afternoon", "Evening"]);
    });
});

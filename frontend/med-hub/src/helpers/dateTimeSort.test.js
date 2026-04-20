import {
    formatTimeHm,
    isFutureSlot,
    sortByDateTimeAsc,
    sortIsoDateAsc,
    sortTimeAsc,
    toDateTimeKey,
} from "./dateTimeSort";

describe("dateTimeSort", () => {
    test("toDateTimeKey returns numeric timestamp for valid date and time", () => {
        const key = toDateTimeKey("2026-04-15", "08:30:00");
        expect(Number.isFinite(key)).toBe(true);
    });

    test("sortIsoDateAsc sorts ISO dates chronologically", () => {
        const dates = ["2026-12-01", "2026-01-10", "2026-01-02"];
        expect(dates.sort(sortIsoDateAsc)).toEqual(["2026-01-02", "2026-01-10", "2026-12-01"]);
    });

    test("sortTimeAsc sorts times chronologically", () => {
        const times = ["12:30:00", "08:15:00", "09:00:00"];
        expect(times.sort(sortTimeAsc)).toEqual(["08:15:00", "09:00:00", "12:30:00"]);
    });

    test("sortByDateTimeAsc sorts mixed day and time rows", () => {
        const rows = [
            { rawDate: "2026-05-12", rawTime: "11:30:00" },
            { rawDate: "2026-05-10", rawTime: "18:00:00" },
            { rawDate: "2026-05-10", rawTime: "07:30:00" },
        ];

        rows.sort(sortByDateTimeAsc);

        expect(rows).toEqual([
            { rawDate: "2026-05-10", rawTime: "07:30:00" },
            { rawDate: "2026-05-10", rawTime: "18:00:00" },
            { rawDate: "2026-05-12", rawTime: "11:30:00" },
        ]);
    });
});

describe("formatTimeHm", () => {
    test("returns HH:mm unchanged", () => {
        expect(formatTimeHm("09:00")).toBe("09:00");
    });

    test("trims seconds", () => {
        expect(formatTimeHm("09:00:00")).toBe("09:00");
    });

    test("trims seconds and milliseconds", () => {
        expect(formatTimeHm("23:59:59.999")).toBe("23:59");
    });

    test("returns empty string for falsy input", () => {
        expect(formatTimeHm("")).toBe("");
        expect(formatTimeHm(null)).toBe("");
        expect(formatTimeHm(undefined)).toBe("");
    });

    test("returns empty string for non-string input", () => {
        expect(formatTimeHm(900)).toBe("");
        expect(formatTimeHm({})).toBe("");
    });

    test("returns original string when prefix does not match HH:mm", () => {
        expect(formatTimeHm("noon")).toBe("noon");
        expect(formatTimeHm("9:00")).toBe("9:00");
    });
});

describe("isFutureSlot", () => {
    const now = new Date("2026-04-15T12:00:00");

    test("returns true for slot strictly in the future (same day)", () => {
        expect(isFutureSlot("2026-04-15", "12:00:01", now)).toBe(true);
        expect(isFutureSlot("2026-04-15", "15:30:00", now)).toBe(true);
    });

    test("returns false for slot at current time (not strictly after)", () => {
        expect(isFutureSlot("2026-04-15", "12:00:00", now)).toBe(false);
    });

    test("returns false for slot earlier on the same day", () => {
        expect(isFutureSlot("2026-04-15", "09:00:00", now)).toBe(false);
        expect(isFutureSlot("2026-04-15", "11:59:59", now)).toBe(false);
    });

    test("returns true for any time on a future day", () => {
        expect(isFutureSlot("2026-04-16", "00:00:00", now)).toBe(true);
    });

    test("returns false for any time on a past day", () => {
        expect(isFutureSlot("2026-04-14", "23:59:59", now)).toBe(false);
    });

    test("returns false for missing inputs", () => {
        expect(isFutureSlot(null, "10:00:00", now)).toBe(false);
        expect(isFutureSlot("2026-04-15", null, now)).toBe(false);
    });
});

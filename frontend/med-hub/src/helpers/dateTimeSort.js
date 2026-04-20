const ISO_DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/;

export const toDateTimeKey = (date, time) => {
    if (!date || !time) {
        return Number.POSITIVE_INFINITY;
    }
    const key = Date.parse(`${date}T${time}`);
    return Number.isNaN(key) ? Number.POSITIVE_INFINITY : key;
};

export const sortIsoDateAsc = (left, right) => {
    if (!left && !right) return 0;
    if (!left) return 1;
    if (!right) return -1;

    if (ISO_DATE_REGEX.test(left) && ISO_DATE_REGEX.test(right)) {
        return left.localeCompare(right);
    }
    return toDateTimeKey(left, "00:00:00") - toDateTimeKey(right, "00:00:00");
};

export const sortTimeAsc = (left, right) => {
    if (!left && !right) return 0;
    if (!left) return 1;
    if (!right) return -1;
    return left.localeCompare(right);
};

export const formatTimeHm = (time) => {
    if (!time || typeof time !== "string") return "";
    const match = /^(\d{2}:\d{2})/.exec(time);
    return match ? match[1] : time;
};

export const isFutureSlot = (date, time, now = new Date()) => {
    const key = toDateTimeKey(date, time);
    if (!Number.isFinite(key)) return false;
    return key > now.getTime();
};

export const sortByDateTimeAsc = (left, right) => {
    const leftKey = toDateTimeKey(left.rawDate ?? left.date, left.rawTime ?? left.time);
    const rightKey = toDateTimeKey(right.rawDate ?? right.date, right.rawTime ?? right.time);
    return leftKey - rightKey;
};

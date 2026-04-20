import { sortTimeAsc } from "./dateTimeSort";

export const TIME_BUCKETS = [
    { id: "morning", label: "Morning", from: "00:00", to: "11:59" },
    { id: "afternoon", label: "Afternoon", from: "12:00", to: "16:59" },
    { id: "evening", label: "Evening", from: "17:00", to: "23:59" },
];

const TIME_REGEX = /^(\d{2}):(\d{2})/;

const toMinutes = (time) => {
    if (!time) return null;
    const match = TIME_REGEX.exec(time);
    if (!match) return null;
    const hours = Number(match[1]);
    const minutes = Number(match[2]);
    if (Number.isNaN(hours) || Number.isNaN(minutes)) return null;
    return hours * 60 + minutes;
};

export const bucketOf = (time) => {
    const minutes = toMinutes(time);
    if (minutes === null) return null;
    for (const bucket of TIME_BUCKETS) {
        if (minutes >= toMinutes(bucket.from) && minutes <= toMinutes(bucket.to)) {
            return bucket.id;
        }
    }
    return null;
};

export const groupByBucket = (appointments) => {
    const buckets = TIME_BUCKETS.reduce((acc, bucket) => {
        acc[bucket.id] = [];
        return acc;
    }, {});

    if (!Array.isArray(appointments)) return buckets;

    for (const appointment of appointments) {
        const bucketId = bucketOf(appointment?.time);
        if (bucketId) {
            buckets[bucketId].push(appointment);
        }
    }

    for (const bucketId of Object.keys(buckets)) {
        buckets[bucketId].sort((left, right) => sortTimeAsc(left.time, right.time));
    }

    return buckets;
};

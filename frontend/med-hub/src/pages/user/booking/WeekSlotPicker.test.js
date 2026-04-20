import React from "react";
import { fireEvent, render, screen, within } from "@testing-library/react";
import "@testing-library/jest-dom";

import WeekSlotPicker from "./WeekSlotPicker";

const pad = (value) => String(value).padStart(2, "0");
const toIsoDate = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const addDays = (date, days) => {
    const copy = new Date(date);
    copy.setHours(0, 0, 0, 0);
    copy.setDate(copy.getDate() + days);
    return copy;
};

const buildAppointment = (date, time) => ({
    appointmentId: `${toIsoDate(date)}T${time}`,
    date: toIsoDate(date),
    time,
});

describe("WeekSlotPicker", () => {
    const today = addDays(new Date(), 0);

    const buildGrouped = () => {
        const todayIso = toIsoDate(today);
        const dayPlusTwoIso = toIsoDate(addDays(today, 2));
        const dayPlusEightIso = toIsoDate(addDays(today, 8));
        return {
            [todayIso]: [
                buildAppointment(today, "09:00"),
                buildAppointment(today, "13:30"),
                buildAppointment(today, "18:45"),
            ],
            [dayPlusTwoIso]: [buildAppointment(addDays(today, 2), "10:15")],
            [dayPlusEightIso]: [buildAppointment(addDays(today, 8), "08:00")],
        };
    };

    test("renders 7 day columns starting from today", () => {
        render(<WeekSlotPicker groupedAppointments={buildGrouped()} onSelect={() => {}} />);

        for (let offset = 0; offset < 7; offset += 1) {
            const iso = toIsoDate(addDays(today, offset));
            expect(screen.getByTestId(`day-column-${iso}`)).toBeInTheDocument();
        }
    });

    test("groups times into Morning / Afternoon / Evening sections for today's column", () => {
        render(<WeekSlotPicker groupedAppointments={buildGrouped()} onSelect={() => {}} />);

        const todayColumn = screen.getByTestId(`day-column-${toIsoDate(today)}`);
        expect(within(todayColumn).getByText("Morning")).toBeInTheDocument();
        expect(within(todayColumn).getByText("Afternoon")).toBeInTheDocument();
        expect(within(todayColumn).getByText("Evening")).toBeInTheDocument();
        expect(within(todayColumn).getByText("09:00")).toBeInTheDocument();
        expect(within(todayColumn).getByText("13:30")).toBeInTheDocument();
        expect(within(todayColumn).getByText("18:45")).toBeInTheDocument();
    });

    test("shows 'No slots' for days without appointments", () => {
        render(<WeekSlotPicker groupedAppointments={buildGrouped()} onSelect={() => {}} />);

        const emptyDayIso = toIsoDate(addDays(today, 1));
        const emptyColumn = screen.getByTestId(`day-column-${emptyDayIso}`);
        expect(within(emptyColumn).getByText("No slots")).toBeInTheDocument();
    });

    test("clicking a chip calls onSelect with the matching appointment", () => {
        const handleSelect = jest.fn();
        render(<WeekSlotPicker groupedAppointments={buildGrouped()} onSelect={handleSelect} />);

        fireEvent.click(screen.getByLabelText(`Book 13:30 on ${toIsoDate(today)}`));

        expect(handleSelect).toHaveBeenCalledTimes(1);
        expect(handleSelect).toHaveBeenCalledWith(
            expect.objectContaining({
                appointmentId: `${toIsoDate(today)}T13:30`,
                time: "13:30",
                date: toIsoDate(today),
            })
        );
    });

    test("Previous week button is disabled initially, Next shifts the window by 7 days", () => {
        render(<WeekSlotPicker groupedAppointments={buildGrouped()} onSelect={() => {}} />);

        const previousBtn = screen.getByRole("button", { name: /previous week/i });
        const nextBtn = screen.getByRole("button", { name: /next week/i });
        expect(previousBtn).toBeDisabled();

        fireEvent.click(nextBtn);

        const firstAfterJump = toIsoDate(addDays(today, 7));
        expect(screen.getByTestId(`day-column-${firstAfterJump}`)).toBeInTheDocument();
        expect(screen.queryByTestId(`day-column-${toIsoDate(today)}`)).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: /previous week/i })).not.toBeDisabled();
    });

    test("trims backend HH:mm:ss to HH:mm for display (chip label and aria-label)", () => {
        const appt = buildAppointment(today, "14:00:00");
        const grouped = { [toIsoDate(today)]: [appt] };
        render(<WeekSlotPicker groupedAppointments={grouped} onSelect={() => {}} />);

        const chip = screen.getByLabelText(`Book 14:00 on ${toIsoDate(today)}`);
        expect(chip).toBeInTheDocument();
        expect(chip).toHaveTextContent("14:00");
        expect(chip).not.toHaveTextContent("14:00:00");
    });

    test("shows empty-range message when grouped payload has no slots in current window", () => {
        render(<WeekSlotPicker groupedAppointments={{}} onSelect={() => {}} />);

        expect(screen.getByTestId("week-slot-picker-empty")).toHaveTextContent(
            "No available slots in this range"
        );
        expect(screen.queryByTestId("week-slot-picker-grid")).not.toBeInTheDocument();
    });
});

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";

jest.mock("axios", () => ({
    __esModule: true,
    default: {
        defaults: { headers: { post: {} } },
        interceptors: { response: { use: jest.fn() } },
    },
}));

jest.mock("jwt-decode", () => ({ __esModule: true, jwtDecode: jest.fn(() => ({})) }));

const mockRequest = jest.fn();
jest.mock("../../../helpers/axiosHelper", () => ({
    request: (...args) => mockRequest(...args),
    getAuthToken: jest.fn(() => "token"),
    getUserRole: jest.fn(() => "ROLE_PATIENT"),
    clearAuthStorage: jest.fn(),
    setAuthHeader: jest.fn(),
    unwrapPage: (data) => (Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []),
}));

// Avoid rendering the full AuthenticatedLayout shell and nav.
jest.mock("../../../layouts/AuthenticatedLayout", () =>
    ({ children }) => <div>{children}</div>
);
jest.mock("./WeekSlotPicker", () => () => null);
jest.mock("./ConfirmBookingDialog", () => () => null);

import Booking from "./Booking";

const CITIES = ["Warsaw", "Krakow"];
const SPECIALIZATIONS = [
    { specializationId: 10, specializationName: "Cardiology" },
    { specializationId: 11, specializationName: "Neurology" },
];

const renderWithPrefill = (state) =>
    render(
        <MemoryRouter initialEntries={[{ pathname: "/booking", state }]}>
            <Routes>
                <Route path="/booking" element={<Booking />} />
            </Routes>
        </MemoryRouter>
    );

describe("Booking — AI prefill", () => {
    beforeEach(() => {
        mockRequest.mockReset();
    });

    it("auto-selects city and matching specialization from router state", async () => {
        // Respond to /v1/locations/cities/distinct and /v1/specializations/by-city
        mockRequest
            .mockResolvedValueOnce({ data: CITIES })
            .mockResolvedValueOnce({ data: SPECIALIZATIONS });

        renderWithPrefill({
            prefillCity: "Warsaw",
            prefillSpecializationId: 10,
            prefillSpecializationName: "Cardiology",
        });

        // City field should be pre-filled.
        await waitFor(() => {
            expect(screen.getByDisplayValue("Warsaw")).toBeInTheDocument();
        });

        // Specialization Autocomplete should show the matched option.
        await waitFor(() => {
            expect(screen.getByDisplayValue("Cardiology")).toBeInTheDocument();
        });
    });

    it("shows a warning toast when specialization is not available in selected city", async () => {
        mockRequest
            .mockResolvedValueOnce({ data: CITIES })
            // Specializations for Warsaw do NOT include Neurology (id=11).
            .mockResolvedValueOnce({ data: [{ specializationId: 10, specializationName: "Cardiology" }] });

        renderWithPrefill({
            prefillCity: "Warsaw",
            prefillSpecializationId: 11,
            prefillSpecializationName: "Neurology",
        });

        await waitFor(() => {
            expect(screen.getByText(/No "Neurology" available in Warsaw/i)).toBeInTheDocument();
        });
    });
});

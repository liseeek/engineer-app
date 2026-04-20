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

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
}));

const mockRequest = jest.fn();
jest.mock("../../../helpers/axiosHelper", () => ({
    request: (...args) => mockRequest(...args),
    getAuthToken: jest.fn(() => "token"),
    getUserRole: jest.fn(() => "ROLE_PATIENT"),
    clearAuthStorage: jest.fn(),
    setAuthHeader: jest.fn(),
}));
jest.mock("../../../context/AuthContext", () => ({
    useAuth: () => ({ role: "ROLE_PATIENT" }),
}));
jest.mock("../../../layouts/AuthenticatedLayout", () =>
    ({ children }) => <div data-testid="layout">{children}</div>
);

import DoctorProfile from "./DoctorProfile";

const DOCTOR = {
    doctorId: 42,
    name: "Anna",
    surname: "Nowak",
    bio: "Experienced cardiologist.",
    avatarUrl: null,
    specializations: [{ specializationId: 5, specializationName: "Cardiology" }],
    locations: [
        { locationId: 3, locationName: "Heart Clinic", address: "Main St 1", city: "Krakow" },
    ],
};

const renderProfile = () =>
    render(
        <MemoryRouter initialEntries={["/doctors/42"]}>
            <Routes>
                <Route path="/doctors/:id" element={<DoctorProfile />} />
            </Routes>
        </MemoryRouter>
    );

describe("DoctorProfile", () => {
    beforeEach(() => {
        mockRequest.mockReset();
        mockRequest.mockResolvedValue({ data: DOCTOR });
    });

    it("renders the doctor name after loading", async () => {
        renderProfile();
        await waitFor(() => expect(screen.getByText("Anna Nowak")).toBeInTheDocument());
    });

    it("renders specialization chip", async () => {
        renderProfile();
        await waitFor(() => expect(screen.getByText("Cardiology")).toBeInTheDocument());
    });

    it("renders bio text", async () => {
        renderProfile();
        await waitFor(() =>
            expect(screen.getByText("Experienced cardiologist.")).toBeInTheDocument()
        );
    });

    it("renders facility card with location name", async () => {
        renderProfile();
        await waitFor(() => expect(screen.getByText("Heart Clinic")).toBeInTheDocument());
    });

    it("book appointment button navigates with correct prefill state", async () => {
        renderProfile();
        await waitFor(() => screen.getByText("Book appointment"));
        screen.getByText("Book appointment").closest("button").click();
        expect(mockNavigate).toHaveBeenCalledWith("/booking", {
            state: expect.objectContaining({
                prefillDoctorId: 42,
                prefillCity: "Krakow",
            }),
        });
    });
});

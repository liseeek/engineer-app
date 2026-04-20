import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
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
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => jest.fn(),
}));

const mockRequest = jest.fn();
jest.mock("../../../helpers/axiosHelper", () => ({
    request: (...args) => mockRequest(...args),
    getAuthToken: jest.fn(() => "token"),
    getUserRole: jest.fn(() => "ROLE_WORKER"),
    clearAuthStorage: jest.fn(),
    setAuthHeader: jest.fn(),
}));

let mockRole = "ROLE_PATIENT";
jest.mock("../../../context/AuthContext", () => ({
    useAuth: () => ({ role: mockRole }),
}));
jest.mock("../../../helpers/roles", () => ({
    ROLES: {
        ADMIN: "ROLE_ADMIN",
        WORKER: "ROLE_WORKER",
        PATIENT: "ROLE_PATIENT",
        DOCTOR: "ROLE_DOCTOR",
    },
}));
jest.mock("../../../layouts/AuthenticatedLayout", () =>
    ({ children }) => <div data-testid="layout">{children}</div>
);

import LocationProfile from "./LocationProfile";

const LOCATION = {
    locationId: 7,
    locationName: "City Medical",
    address: "Broad St 10",
    city: "Warsaw",
    country: "PL",
    description: "A great clinic founded in 2005.",
    yearEstablished: 2005,
    phoneNumber: "+48 500 111 222",
    email: "info@citymedical.pl",
};

const renderLocation = () =>
    render(
        <MemoryRouter initialEntries={["/locations/7"]}>
            <Routes>
                <Route path="/locations/:id" element={<LocationProfile />} />
            </Routes>
        </MemoryRouter>
    );

describe("LocationProfile", () => {
    beforeEach(() => {
        mockRequest.mockReset();
        mockRole = "ROLE_PATIENT";
        mockRequest
            .mockResolvedValueOnce({ data: LOCATION })
            .mockResolvedValue({ data: { content: [], totalPages: 0 } });
    });

    it("renders location name", async () => {
        renderLocation();
        await waitFor(() => expect(screen.getByText("City Medical")).toBeInTheDocument());
    });

    it("renders description text", async () => {
        renderLocation();
        await waitFor(() =>
            expect(screen.getByText("A great clinic founded in 2005.")).toBeInTheDocument()
        );
    });

    it("renders established year", async () => {
        renderLocation();
        await waitFor(() => expect(screen.getByText(/Est\. 2005/)).toBeInTheDocument());
    });

    it("does NOT show Edit button for PATIENT", async () => {
        mockRole = "ROLE_PATIENT";
        renderLocation();
        await waitFor(() => screen.getByText("City Medical"));
        expect(screen.queryByText("Edit facility")).not.toBeInTheDocument();
    });

    it("shows Edit button for ADMIN", async () => {
        mockRole = "ROLE_ADMIN";
        mockRequest.mockReset();
        mockRequest
            .mockResolvedValueOnce({ data: LOCATION })
            .mockResolvedValue({ data: { content: [] } });

        renderLocation();
        await waitFor(() => expect(screen.getByText("Edit facility")).toBeInTheDocument());
    });

    it("opens edit dialog when Edit facility is clicked", async () => {
        mockRole = "ROLE_ADMIN";
        mockRequest.mockReset();
        mockRequest
            .mockResolvedValueOnce({ data: LOCATION })
            .mockResolvedValue({ data: { content: [] } });

        renderLocation();
        await waitFor(() => screen.getByText("Edit facility"));
        fireEvent.click(screen.getByText("Edit facility"));
        expect(screen.getByText("Edit facility information")).toBeInTheDocument();
    });
});

import React from "react";
import { render, screen, waitFor, fireEvent, act } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
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
    getUserRole: jest.fn(() => "ROLE_DOCTOR"),
    clearAuthStorage: jest.fn(),
    setAuthHeader: jest.fn(),
}));
jest.mock("../../../context/AuthContext", () => ({
    useAuth: () => ({ role: "ROLE_DOCTOR" }),
}));
jest.mock("../../../layouts/AuthenticatedLayout", () =>
    ({ children }) => <div data-testid="layout">{children}</div>
);

import DoctorOwnProfile from "./DoctorOwnProfile";

const PROFILE = {
    doctorId: 9,
    name: "Marek",
    surname: "Kowal",
    bio: "Experienced surgeon.",
    avatarUrl: "https://example.com/photo.jpg",
    specializations: [],
    locations: [],
};

describe("DoctorOwnProfile", () => {
    beforeEach(() => {
        mockRequest.mockReset();
        mockRequest.mockResolvedValue({ data: PROFILE });
    });

    it("renders doctor name after loading", async () => {
        render(<MemoryRouter><DoctorOwnProfile /></MemoryRouter>);
        await waitFor(() => expect(screen.getByText("Marek Kowal")).toBeInTheDocument());
    });

    it("populates bio field with existing value", async () => {
        render(<MemoryRouter><DoctorOwnProfile /></MemoryRouter>);
        await waitFor(() => {
            const bioField = screen.getByLabelText("About / Bio");
            expect(bioField.value).toBe("Experienced surgeon.");
        });
    });

    it("populates avatar URL field with existing value", async () => {
        render(<MemoryRouter><DoctorOwnProfile /></MemoryRouter>);
        await waitFor(() => {
            const urlField = screen.getByLabelText("Avatar URL");
            expect(urlField.value).toBe("https://example.com/photo.jpg");
        });
    });

    it("calls PATCH with updated bio on save", async () => {
        const updatedProfile = { ...PROFILE, bio: "Updated bio." };
        mockRequest
            .mockResolvedValueOnce({ data: PROFILE })
            .mockResolvedValueOnce({ data: updatedProfile });

        render(<MemoryRouter><DoctorOwnProfile /></MemoryRouter>);
        await waitFor(() => screen.getByLabelText("About / Bio"));

        await act(async () => {
            fireEvent.change(screen.getByLabelText("About / Bio"), {
                target: { value: "Updated bio." },
            });
            fireEvent.click(screen.getByText("Save changes"));
        });

        await waitFor(() =>
            expect(mockRequest).toHaveBeenCalledWith("patch", "/v1/doctor/me/profile", expect.objectContaining({ bio: "Updated bio." }))
        );
    });
});

import React from "react";
import { render, screen, fireEvent, waitFor, within, act } from "@testing-library/react";
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

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
    ...jest.requireActual("react-router-dom"),
    useNavigate: () => mockNavigate,
}));

const mockRequest = jest.fn();
jest.mock("../../helpers/axiosHelper", () => ({
    request: (...args) => mockRequest(...args),
    getAuthToken: jest.fn(() => null),
    getUserRole: jest.fn(() => null),
    clearAuthStorage: jest.fn(),
    setAuthHeader: jest.fn(),
}));

import SymptomCheckerWidget from "./SymptomCheckerWidget";

const CITIES = ["Warsaw", "Krakow", "Gdansk"];
const RECOMMENDATIONS = [
    { specializationId: 10, specializationName: "Cardiology", confidence: "HIGH", reasoning: "Chest pain." },
];

const renderWidget = () =>
    render(
        <MemoryRouter>
            <SymptomCheckerWidget />
        </MemoryRouter>
    );

// Open the widget and wait for step 0 to be visible.
const openWidget = async () => {
    await act(async () => {
        fireEvent.click(screen.getByRole("button", { name: /Symptom Checker/i }));
    });
    await waitFor(() =>
        expect(screen.getByText("Tell us about yourself so we can suggest the right specialist.")).toBeInTheDocument()
    );
};

describe("SymptomCheckerWidget", () => {
    beforeEach(() => {
        mockRequest.mockReset();
        mockNavigate.mockReset();
        // First request is always the cities fetch on mount.
        mockRequest.mockResolvedValue({ data: CITIES });
    });

    it("opens the widget and shows step 0 form", async () => {
        renderWidget();
        await openWidget();
        expect(screen.getByText("Find the right specialist")).toBeInTheDocument();
    });

    it("renders a city Autocomplete in step 0", async () => {
        renderWidget();
        await openWidget();
        expect(screen.getByLabelText("Preferred city")).toBeInTheDocument();
    });

    it("Next button is initially disabled before any selection", async () => {
        renderWidget();
        await openWidget();
        expect(screen.getByRole("button", { name: /^next$/i })).toBeDisabled();
    });

    it("navigates to /booking with prefill state when Book appointment is clicked", async () => {
        renderWidget();
        // Wait for cities to load.
        await waitFor(() => expect(mockRequest).toHaveBeenCalledWith("get", "/v1/locations/cities/distinct"));

        await openWidget();

        // --- Step 0: fill gender (radio), age (MUI Select), city (Autocomplete) ---

        // Gender radio is straightforward.
        fireEvent.click(screen.getByLabelText("Male"));

        // MUI Select: find all comboboxes, pick the one inside the Age range FormControl.
        // The Preferred city Autocomplete also has role=combobox but its input is text.
        // Age Range Select has a hidden native <input value="">.
        // We target comboboxes and choose the one whose container has text "Age range".
        const comboboxes = screen.getAllByRole("combobox");
        // The first combobox rendered is the Age range Select; the second is the city Autocomplete.
        const ageRangeCombobox = comboboxes[0];
        fireEvent.mouseDown(ageRangeCombobox);
        await waitFor(() => expect(screen.getByText("18–30")).toBeInTheDocument());
        fireEvent.click(screen.getByText("18–30"));

        // City Autocomplete.
        const cityInput = screen.getByLabelText("Preferred city");
        fireEvent.change(cityInput, { target: { value: "Warsaw" } });
        await waitFor(() => expect(screen.getByRole("option", { name: "Warsaw" })).toBeInTheDocument());
        fireEvent.click(screen.getByRole("option", { name: "Warsaw" }));

        // Next should now be enabled.
        await waitFor(() =>
            expect(screen.getByRole("button", { name: /^next$/i })).not.toBeDisabled()
        );
        fireEvent.click(screen.getByRole("button", { name: /^next$/i }));

        // --- Step 1: select a symptom and analyze ---
        await waitFor(() => expect(screen.getByText("What symptoms are you experiencing?")).toBeInTheDocument());
        fireEvent.click(screen.getByLabelText("Headache"));

        mockRequest.mockResolvedValueOnce({ data: { recommendations: RECOMMENDATIONS, disclaimer: "" } });
        fireEvent.click(screen.getByRole("button", { name: /analyze/i }));

        // --- Step 2: results ---
        await waitFor(() => expect(screen.getByText("Cardiology")).toBeInTheDocument());

        fireEvent.click(screen.getByRole("button", { name: /book appointment/i }));

        expect(mockNavigate).toHaveBeenCalledWith("/booking", {
            state: {
                prefillCity: "Warsaw",
                prefillSpecializationId: 10,
                prefillSpecializationName: "Cardiology",
            },
        });
    });
});

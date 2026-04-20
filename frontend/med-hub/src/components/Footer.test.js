import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import Footer from "./Footer";

describe("Footer", () => {
    it("renders copyright with current year and contentinfo role", () => {
        render(<Footer />);

        const footer = screen.getByRole("contentinfo");
        expect(footer).toBeInTheDocument();
        expect(footer).toHaveTextContent("MedHub. All rights reserved.");
        expect(footer).toHaveTextContent(String(new Date().getFullYear()));
    });
});

import React from "react";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import "@testing-library/jest-dom";

jest.mock("axios", () => {
    const instance = {
        defaults: { headers: { post: {} } },
        interceptors: { response: { use: jest.fn() } },
    };
    return { __esModule: true, default: instance };
});


import ProtectedRoute from "./protectedRoute";
import { ROLES } from "./roles";
import { AuthProvider } from "../context/AuthContext";

const base64url = (obj) =>
    Buffer.from(JSON.stringify(obj), "utf-8")
        .toString("base64")
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/, "");
const makeJwt = (payload) =>
    `${base64url({ alg: "HS256", typ: "JWT" })}.${base64url(payload)}.signature`;

const FUTURE = () => Math.floor(Date.now() / 1000) + 60 * 60;
const PAST = () => Math.floor(Date.now() / 1000) - 60;

const renderWithRoute = ({ initialEntries = ["/protected"], requiredRoles } = {}) =>
    render(
        <MemoryRouter initialEntries={initialEntries}>
            <AuthProvider>
                <Routes>
                    <Route path="/unauthorized" element={<div>Access Denied</div>} />
                    <Route element={<ProtectedRoute requiredRoles={requiredRoles} />}>
                        <Route path="/protected" element={<div>Secret Page</div>} />
                    </Route>
                </Routes>
            </AuthProvider>
        </MemoryRouter>
    );

describe("ProtectedRoute", () => {
    beforeEach(() => {
        window.localStorage.clear();
    });

    it("redirects to /unauthorized when no token is present", () => {
        renderWithRoute();
        expect(screen.getByText("Access Denied")).toBeInTheDocument();
        expect(screen.queryByText("Secret Page")).not.toBeInTheDocument();
    });

    it("redirects and clears storage when token is expired", () => {
        window.localStorage.setItem("auth_token", makeJwt({ exp: PAST() }));
        window.localStorage.setItem("user_role", ROLES.PATIENT);

        renderWithRoute({ requiredRoles: [ROLES.PATIENT] });

        expect(screen.getByText("Access Denied")).toBeInTheDocument();
        expect(window.localStorage.getItem("auth_token")).toBeNull();
        expect(window.localStorage.getItem("user_role")).toBeNull();
    });

    it("redirects when role does not match required roles", () => {
        window.localStorage.setItem("auth_token", makeJwt({ exp: FUTURE() }));
        window.localStorage.setItem("user_role", ROLES.PATIENT);

        renderWithRoute({ requiredRoles: [ROLES.ADMIN] });

        expect(screen.getByText("Access Denied")).toBeInTheDocument();
        expect(screen.queryByText("Secret Page")).not.toBeInTheDocument();
    });

    it("renders the protected outlet when token is valid and role matches", () => {
        window.localStorage.setItem("auth_token", makeJwt({ exp: FUTURE() }));
        window.localStorage.setItem("user_role", ROLES.PATIENT);

        renderWithRoute({ requiredRoles: [ROLES.PATIENT] });

        expect(screen.getByText("Secret Page")).toBeInTheDocument();
        expect(screen.queryByText("Access Denied")).not.toBeInTheDocument();
    });

    it("renders the outlet when no requiredRoles are specified and token is valid", () => {
        window.localStorage.setItem("auth_token", makeJwt({ exp: FUTURE() }));
        window.localStorage.setItem("user_role", ROLES.WORKER);

        renderWithRoute();

        expect(screen.getByText("Secret Page")).toBeInTheDocument();
    });
});

import { useMemo } from "react"; 

export const usePasswordValidation = (password) => {
    const validatePassword = (password) => {
        const minLength = password.length >= 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecialChar = /[@#$%^&+=!]/.test(password);
        const noSpaces = !/\s/.test(password);

        const checks = {
            minLength,
            hasUpperCase,
            hasNumber,
            hasSpecialChar,
            noSpaces,
        };

        const isValid = minLength && hasUpperCase && hasNumber && hasSpecialChar && noSpaces;

        const passedChecks = Object.values(checks).filter(Boolean).length;

        return {
            isValid,
            strength: passedChecks === 5 ? 'strong' : passedChecks === 4 ? 'medium' : 'weak',
            checks,
        };
    };

    const result = useMemo(() => validatePassword(password), [password]);

    return result;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    fullName?: string | null;
}

export interface AuthResponse {
    token: string | null;
    username: string;
    fullName: string | null;
    message: string;
}

export interface ApiErrorResponse {
    timestamp: string;
    status: number;
    message: string;
    fieldErrors: Record<string, string> | null;
}


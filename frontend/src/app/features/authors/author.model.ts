export interface Author {
    id: number;
    firstName: string;
    lastName: string;
    biography: string | null;
}

export interface AuthorRequest {
    firstName: string;
    lastName: string;
    biography: string | null;
}


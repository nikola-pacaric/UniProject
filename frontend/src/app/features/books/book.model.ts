export interface Book {
    id: number;
    title: string;
    isbn: string;
    publicationYear: number;
    totalCopies: number;
    availableCopies: number;
    authorId: number;
    categoryId: number;
}

export interface BookRequest {
    title: string;
    isbn: string;
    publicationYear: number;
    totalCopies: number;
    authorId: number;
    categoryId: number;
}


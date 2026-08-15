export type LoanStatus = 'ACTIVE' | 'RETURNED' | 'OVERDUE';

export interface Loan {
    id: number;
    bookId: number | null;
    bookTitleAtLoan: string;
    memberId: number;
    loanDate: string;
    dueDate: string;
    returnDate: string | null;
    status: LoanStatus;
}

export interface LoanRequest {
    bookId: number;
    memberId: number;
}


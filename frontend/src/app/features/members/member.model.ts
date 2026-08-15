export interface Member {
    id: number;
    firstName: string;
    lastName: string;
    membershipCardNumber: string;
    email: string;
    phone: string | null;
    active: boolean;
}

export interface MemberRequest {
    firstName: string;
    lastName: string;
    membershipCardNumber: string;
    email: string;
    phone: string | null;
}
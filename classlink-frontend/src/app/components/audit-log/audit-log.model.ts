export interface AuditLog {
    id: number | string;
    actorId: string;         // UUID
    action: string;          // z.B. "USER_LOGIN", "PROJECT_CREATE"
    entity?: string;         // z.B. "Project#42"
    createdAt: string;       // ISO-String
    details?: string;
    severity?: 'info' | 'warn' | 'error';
}
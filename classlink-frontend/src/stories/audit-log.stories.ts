
import type { Meta, StoryObj } from '@storybook/angular';
import { action} from "storybook/actions";
import { AuditLog as AuditLogCmp } from '../app/components/audit-log/audit-log';

type Severity = 'info' | 'warn' | 'error';
interface AuditLogRow {
    id: number | string;
    actorId: string;
    action: string;
    entity?: string;
    createdAt: string;
    severity?: Severity;
}

const MOCK: AuditLogRow[] = [
    { id: 1, actorId: '5e2c...', action: 'USER_LOGIN', entity: 'Auth', createdAt: new Date().toISOString(), severity: 'info' },
    { id: 2, actorId: '9a3d...', action: 'PROJECT_CREATE', entity: 'Project#42', createdAt: new Date(Date.now()-3600_000).toISOString(), severity: 'warn' },
    { id: 3, actorId: 'a1b2...', action: 'USER_DELETE', entity: 'User#17', createdAt: new Date(Date.now()-86_400_000).toISOString(), severity: 'error' },
];

const meta: Meta<AuditLogCmp> = {
    title: 'Components/AuditLog',
    component: AuditLogCmp,
    args: {
        logs: MOCK,
        loading: false,
        paginator: true,
        rows: 10,
        rowsPerPageOptions: [10, 20, 50],
        defaultSortField: 'createdAt',
    },
    argTypes: { rowClick: { action: 'rowClick' } },
    render: (args: any) => ({ props: { ...args, rowClick: action('rowClick') } }),
};
export default meta;

type Story = StoryObj<AuditLogCmp>;

export const Default: Story = {};
export const Loading: Story = { args: { loading: true, logs: [] } };
export const ManyRows: Story = {
    args: {
        logs: Array.from({ length: 75 }).map((_, i) => ({
            ...MOCK[i % MOCK.length],
            id: i + 1,
            createdAt: new Date(Date.now() - i * 60000).toISOString(),
        })),
        rows: 20,
    },
};
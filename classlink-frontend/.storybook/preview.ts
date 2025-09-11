import type { Preview } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { providePrimeNG } from 'primeng/config';

import Aura from '@primeuix/themes/aura';

export const decorators = [
    applicationConfig({
        providers: [
            providePrimeNG({
                theme: { preset: Aura },
            }),
        ],
    }),
];

export const parameters: Preview['parameters'] = {
    controls: { expanded: true },
};
export default {};
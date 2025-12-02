// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = tseslint.config(
    {
        files: ["**/*.ts"],
        ignores: ["src/app/api/**"],
        extends: [
            eslint.configs.recommended,
            ...tseslint.configs.recommended,
            ...tseslint.configs.stylistic,
            ...angular.configs.tsRecommended
        ],
        processor: angular.processInlineTemplates,
        rules: {
            "@typescript-eslint/no-explicit-any": [
                "warn",
                {ignoreRestArgs: true}
            ],
            "@typescript-eslint/no-unused-vars": [
                "warn",
                {argsIgnorePattern: "^_", varsIgnorePattern: "^_"}
            ],
            "@angular-eslint/directive-selector": [
                "warn",
                {type: "attribute", prefix: ["app", "admin", "teacher", "student"], style: "camelCase"}
            ],
            "@angular-eslint/component-selector": [
                "warn",
                {type: "element", prefix: ["app", "admin", "teacher", "student"], style: "kebab-case"}
            ]
        }
    },
    {
        files: ["**/*.html"],
        extends: [
            ...angular.configs.templateRecommended
        ],
        rules: {
            "@angular-eslint/template/click-events-have-key-events": "off",
            "@angular-eslint/template/interactive-supports-focus": "off"
        }
    }
);
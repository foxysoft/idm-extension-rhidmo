# Rhidmo&reg; Support Roadmap
Any notable milestones in Rhidmo's software lifecycle, such as planned future changes, enhancements, deprecation or end of life of any or all Rhidmo releases will be documented in this file.

The latest version of this document can be found at https://github.com/foxysoft/idm-extension-rhidmo/blob/master/ROADMAP.md

## Support Status
Rhidmo's maintainers are committed to provide free community support for Rhidmo at least until the official End of Extended Maintenance set by SAP® for their product SAP Identity Management 8.0, which is the foundation that Rhidmo builds on. Support for Rhidmo will hence end **not earlier than 2030-12-31**.

At the time of this writing, Rhidmo is in maintenance mode. That means that any upcoming changes to the software will focus on improving security, robustness and maintainability, but we do not plan to add any user-facing changes or functional enhancements unless it turns out to be mandatory for security or regulatory compliance reasons.

## Upcoming Changes
All statements in this section are non-binding. Rhidmo's maintainers plan to implement these measures within the specified time frame, but give no guarantees either for the dates or for the provision at all.

| What will be changed? | When do we plan to release? | Is this a breaking change? | Category |
| --- | --- | --- | --- |
| Implement reproducible builds | 2025 Q3 | - | Security |
| Drop support for Java 6 | 2025 Q3 | X | Developer Experience |
| Add a Software Bill of Materials (SBOM) | 2025 Q3 | - | Security |
| Update build environment to latest Java LTS| 2025 Q4 | - | Developer Experience |
| Add static code analysis to build pipeline | 2025 Q4 | - | Security |
| [SLSA Level 3](https://slsa.dev/) compliance | 2026 | - | Security |
| Improve code coverage of unit tests | 2026 | - | Robustness |

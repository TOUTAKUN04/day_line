# Security Policy

## Scope
This policy applies to the entire `day_line` repository, including source
code, build scripts, release artifacts, and generated app data.

## Security And Data Integrity Rules
- Do not modify, tamper with, falsify, or delete application data outside
  normal app workflows.
- Do not bypass or disable permission checks, alarms, notifications, service
  guards, integrity checks, or security-related controls.
- Do not patch or repackage releases in a way that changes behavior while
  presenting them as official builds.
- Do not expose secrets, tokens, signing material, or private build artifacts.
- Keep dependencies and build tooling updated to supported secure versions.

## Responsible Disclosure
If you find a security issue:
1. Do not publish exploit details publicly first.
2. Open a private GitHub security advisory for this repository.
3. Include reproduction steps, affected version/tag, and potential impact.

## Response Targets
- Initial acknowledgement: within 7 days
- Triage/update: within 14 days
- Fix or mitigation target: as soon as reasonably possible based on severity

## Enforcement
Violations of this security policy or the project license may result in
revocation of access/usage rights and removal of distributed materials.

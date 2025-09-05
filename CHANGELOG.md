# Rhidmo&reg; Change Log
All notable changes to Rhidmo&reg; will be documented in this file.
Rhidmo&reg; adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The latest version of this change log can be found at https://github.com/foxysoft/idm-extension-rhidmo/blob/master/CHANGELOG.md

## [Unreleased]
- Nothing at this time

## [1.3.0] - 2025-09-05
### Added
- Add ROADMAP document
- `mvn clean verify` now generates coverage reports in `./servlet-jar/target/site/jacoco/index.html`
- Add Linux shell script rhidmo-repro-build.sh for reproducible builds
- Add separate BUILD document and refer to that from README

### Changed
- Building from source now requires Java 8+ and Maven 3.9.x+; build output still targets Java 6

### Fixed
- Missing default language in demo content
- Cleanup in installation manual
- Update production dependency Byte Buddy to 1.17.5

## [1.2.0] - 2025-06-05
- Add Rhidmo example packages
- Add this CHANGELOG

## [1.1.2] - 2025-04-28
### Added
- Animated GIF in README.md

### Fixed
- Fix documentation regarding available support options

## [1.1.1] - 2023-07-29
### Added
- Add issue template to GitHub project

### Fixed
- PackageScript.equals incorrectly compares strings #10
- Bump H2 Database from 2.1.212 to 2.2.220 in test dependencies (fixes CVE-2022-45868)

## [1.1.0] - 2022-06-24
### Added
- Add support for using scripts from packages other than the form's own #5

### Fixed
- Update external SAP&reg; IDM hyperlink in README.md
- Bump junit version from 4.12 to 4.13.1 in test dependencies
- Fix resource leak from not closing JDBC connections in global functions #7
- Fix wrong severity DEBUG instead of ERROR in global function uError #8
- Fix deploy warning from JLinEE #6
- Misc. cleanup (use UNIX line-endings, add missing license headers, optimize pom.xml)

## [1.0.1] - 2018-05-04
### Added
- Add new global function uSendSMTPMessage
- Support more cryptographic algorithms in addition to DES3CBC in global functions uEncrypt and uDecrypt:
   - AES128CBC
   - AES192CBC
   - AES256CBC

### Fixed
- Only fetch script source with mcEnabled=1 and mcIsObsoleted=0, required for SAP&reg; IDM 8.0 SP6+
- Build fails with "class file for java.lang.AutoCloseable not found" #1
- mc_package_scripts.mcisobsoleted doesn't exist in all support package levels #2

## [1.0.0] - 2018-01-25
### Added
- First public release

[Unreleased]: ../../compare/1.3.0...HEAD
[1.3.0]:      ../../compare/1.2.0...1.3.0
[1.2.0]:      ../../compare/1.1.2...1.2.0
[1.1.2]:      ../../compare/1.1.1...1.1.2
[1.1.1]:      ../../compare/1.1.0...1.1.1
[1.1.0]:      ../../compare/1.0.1...1.1.0
[1.0.1]:      ../../compare/1.0.0...1.0.1
[1.0.0]:      ../../releases/tag/1.0.0

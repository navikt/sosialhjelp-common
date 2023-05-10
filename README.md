[![Publish artifacts](https://github.com/navikt/sosialhjelp-common/actions/workflows/release.yml/badge.svg)](https://github.com/navikt/sosialhjelp-common/actions/workflows/release.yml)

Sosialhjelp-common
================

Felles-komponenter for applikasjoner som tilhører teamdigisos.

---

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

# Innhold

Felles teknologi:
* Kotlin
* JDK 17
* Gradle

Krav:
* JDK 17

### sosialhjelp-common-api
Felles api-modeller for integrasjoner mot eksterne tjenester:
- Fiks API

### sosialhjelp-common-selftest
Selftest-bibliotek. SelftestService gir en rapport over avhengigheter som implementerer `DependencyCheck`.

Teknologi:
* Coroutines
* Jackson
* Micrometer

### sosialhjelp-common-kotlin-utils
Felles hjelpemetoder for logging og retry. 

Konvertering av utvalgte filtyper til PDF. Se egen README i pakken.

Teknologi:
* Coroutines

## Ktlint
Hvordan kjøre Ktlint:
* Fra IDEA: Kjør Gradle Task: sosialhjelp-common -> Tasks -> formatting -> ktlintFormat
* Fra terminal:
    * Kun formater: `./gradlew ktlintFormat`
    * Formater og bygg: `./gradlew ktlintFormat build`
    * Hvis IntelliJ begynner å hikke, kan en kjøre `./gradlew clean ktlintFormat build`

Endre IntelliJ autoformateringskonfigurasjon for dette prosjektet:
* `./gradlew ktlintApplyToIdea`

Legg til pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`

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
* JDK 11
* Gradle

Krav:
* JDK 11

### sosialhjelp-common-api
Felles api-modeller for integrasjoner mot eksterne tjenester:
- Fiks API

### sosialhjelp-common-selftest
Selftest-bibliotek. SelftestService gir en rapport over avhengigheter som implementerer `DependencyCheck`.

Teknologi:
* Coroutines
* Jackson
* Micrometer

### sosialhjelp-common-kommuneinfo-client
Klient for å kunne hente KommunenInfo fra Fiks.

Teknologi:
* Spring
* Jackson

### sosialhjelp-common-client-utils
Hjelpemetoder og felles funksjonalitet for klient-moduler i repoet.

Teknologi:
* Spring
* Jackson

### sosialhjelp-common-idporten-client
Klient for å hente virksomhetstoken fra IdPorten.

Teknologi:
* Spring
* Jackson
* nimbus-jose-jwt

### sosialhjelp-common-kotlin-utils
Felles hjelpemetoder for logging og retry.

Teknologi:
* Coroutines

### sosialhjelp-common-maskinporten-utils
Klient for å hente access-token fra maskinporten til ønsket scope.  
Krever registrering av applikasjon i [navikt/nav-maskinporten](https://github.com/navikt/nav-maskinporten) for å lage nødvendige secrets i vault.  
Etter den er registert i nav-maskinporten må applikasjonen registreres med tilgang til managed secrets knyttet til
maskinporten i [navikt/vault-iac](https://github.com/navikt/vault-iac).
Brukes p.t kun i `sosialhjelp-adminpanel-api`.

Teknologi:
* Coroutines
* Spring-boot
* Jackson
* navikt/token-support
* Jsonwebtoken

### sosialhjelp-common-metrics
Metrics hentet fra common-java-modules. Brukes i sosialhjelp-soknad-api.

Teknologi:
* Java
* Spring
* Jackson
* Micrometer
* AspectJ

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

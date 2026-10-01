# Changelog

## 2.1.0

### Added

- `fold` takes an optional `clock: Clock` parameter (default `Clock.System`) so the 30-day window for søknad vedlegg krav can be tested.

### Fixed

- Søknad `VedleggKreves` krav are no longer applied once a `DokumentasjonEtterspurt` hendelse has been received. Previously, a `DokumentasjonEtterspurt` with empty `dokumenter` brought back the søknad's krav within the 30-day window.
- `paakrevdeVedleggProvider` is no longer called when a `DokumentasjonEtterspurt` hendelse exists.
- The 30-day window for søknad krav is now counted in calendar days in Europe/Oslo, not exact milliseconds, matching legacy behaviour.

## 2.0.0

### Breaking changes

- Added `Soknad.vilkarUtenSak` and `Soknad.dokumentasjonkravUtenSak` for requirements whose `saksreferanse` is blank or does not match a known sak.
- Added `saksReferanse` to `Vilkar` and `Dokumentasjonkrav` so requirements outside a sak retain their source reference.
- Vilkår and dokumentasjonskrav with an unknown `saksreferanse` no longer create synthetic `Sak` instances.

### Fixed

- Vilkår and dokumentasjonskrav without a `saksreferanse` are no longer dropped during folding.

## 1.1.0

- Previous release.

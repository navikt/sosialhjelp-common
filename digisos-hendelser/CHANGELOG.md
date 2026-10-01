# Changelog

## 2.0.0

### Breaking changes

- Added `Soknad.vilkarUtenSak` and `Soknad.dokumentasjonkravUtenSak` for requirements whose `saksreferanse` is blank or does not match a known sak.
- Added `saksReferanse` to `Vilkar` and `Dokumentasjonkrav` so requirements outside a sak retain their source reference.
- Vilkår and dokumentasjonskrav with an unknown `saksreferanse` no longer create synthetic `Sak` instances.

### Fixed

- Vilkår and dokumentasjonskrav without a `saksreferanse` are no longer dropped during folding.

## 1.1.0

- Previous release.

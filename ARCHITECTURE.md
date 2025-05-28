# ARCHITECTURE.md

## Oversikt

Dette dokumentet beskriver den tekniske arkitekturen til Trygve-applikasjonen. Målet er å gi en forståelig oversikt for videreutvikling og vedlikehold.

## Arkitekturprinsipper

Applikasjonen er bygget etter **Clean Architecture**-prinsipper med en lagdelt struktur:

- **UI-lag**: Presentasjonslogikk bygget med Jetpack Compose.
- **Domenelag**: Inneholder forretningslogikk og use cases.
- **Datalag**: Henter og prosesserer data fra API-er eller lokale kilder.

Kjernen i løsningen er en **TensorFlow Lite-modell** for risikoprediksjon.

### MVVM (Model-View-ViewModel)

Vi bruker MVVM for å strukturere UI-koden:

- **Model**: Inneholder dataobjekter og logikk (repositories, use cases).
- **ViewModel**: Eksponerer tilstand via `StateFlow` og håndterer brukerinput.
- **View**: Jetpack Compose-funksjoner som observerer ViewModelen.

### Unidirectional Data Flow (UDF)

All data flyter en vei:

- ViewModel oppdaterer tilstanden.
- View viser data og sender brukerhandlinger tilbake.

Dette gir forutsigbar og lett testbar tilstandshåndtering.

## Teknologistabel

- **Språk**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Asynkronitet**: Kotlin Coroutines og Flows
- **Nettverk**: Ktor Client (for værdata)
- **JSON**: kotlinx.serialization
- **Navigasjon**: Navigation Compose
- **Kart**: Mapbox SDK
- **Maskinlæring**: TensorFlow Lite
- **Dependency Injection**: Hilt (delvis implementert)

## Anbefalt prosjektstruktur

For bedre vedlikehold og byggtid anbefales en slik oppdeling:

- `:app` – UI og ViewModel
- `:data` – Repositories og eksterne kilder
- Per i dag ligger det meste i `:app`, men refaktorering er ønskelig.

## Objektorienterte prinsipper

- **Lav kobling**: Grensesnitt og Dependency Injection brukes for å redusere avhengigheter.
- **Høy kohesjon**: Komponenter har tydelig ansvar. ViewModels bør ikke vokse for mye.

## Android-versjon

- **minSdkVersion**: 26 (Android 8.0 Oreo)
- **compileSdkVersion / targetSdkVersion**: 34 anbefales

Begrunnelsen for `minSdkVersion` er støtte for TensorFlow Lite og moderne Android-funksjoner.

## Kodestil og praksis

- Bruk Android Studio sin formattering (eller Ktlint)
- Null-sikkerhet skal utnyttes aktivt
- Test kjernefunksjonalitet

## Vedlikehold og videreutvikling

- Hold dette dokumentet oppdatert ved større endringer.
- Nye funksjoner (som reell brukerlokasjon, bedre datakilder) bør integreres med tanke på testbarhet og struktur.
-  Appen inneholder tillatelser for posisjonering (ACCESS_COARSE_LOCATION og ACCESS_FINE_LOCATION) i AndroidManifest.xml, men bruker ikke faktisk brukerlokasjon i nåværende implementasjon - Istedet returneres Oslo som standardlokasjon. Dette designvalget ble tatt for å forenkle testing og utvikling, ettersom dynamisk lokasjonshåndtering krever omfattende testing på fysiske enheter. I Android Studio-miljøet må lokasjonstillatelser håndteres eksplisitt, noe som kompliserer utviklingsprosessen. Ved framtidig videreutvikling eller distribusjon via Google Play Store bør reell lokasjonshåndtering implementeres for å gi brukere stedsspesifikk informasjon. 

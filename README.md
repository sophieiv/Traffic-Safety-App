# Trafikkrisiko-app
Appen ble laget i faget IN2000 - Software Engineering i samarbeid med meteorologisk institutt.
Den har som hensikt å predikere veirisiko i norske fylker basert på værdata og tidligere ulykkeshistorikk.

## Funksjoner
- Interaktivt kart over norske fylker (Risiko og vær begrenset til Oslo, Akershus, Buskerud, Østfold og Vestfold grunnet omfattende manuell håndtering av værdatahistorikk.)
- Værdata for valgt fylke
- Risikoprediksjon for trafikkulykker (Moderat, Økt, Høy)
- 10-dagers prognose for vær og risiko (inkl. nåværende dag)
- Følgende skjermer: Hjem, Kart, Hjelp og Innstillinger

## Hvordan appen kjøres
1. Last ned og pakk ut .zip-filen til en mappe på datamaskinen din
2. Åpne prosjektet i Android Studio via "Open an existing project"
3. Legg til Mapbox Access Token i values/mapbox-access-token.xml (send mail til jatomter@gmail.com)
4. Legg til Azure-nøkkel i data/ki/AzureOpenAIService.kt (send mail til jatomter@gmail.com)
4. Bygg prosjektet gjennom "Build > Make Project"
5. Koble til en fysisk Android-enhet via USB eller sett opp en emulator via AVD Manager
6. Kjør appen ved å klikke på den grønne "Run"-knappen
7. Godta alle tillatelser appen ber om ved første oppstart
8. Appen kan teknisk sett kjøre på enheter med API 26+, men vi anbefaler API 31 eller nyere for optimal ytelse

## Viktig om brukerlokasjon
Appen bruker ikke faktisk brukerlokasjon i nåværende versjon, men returnerer alltid Oslo som standardlokasjon. Implementering av faktisk lokasjon bør vurderes ved videre utvikling og før evt. lansering på Google Play Store, men er ikke hensiktsmessig for testing og utvikling av appen. Se _Architecture.md_ for mer informasjon rundt dette.

## Krav
- Android 8.0 (API 26) eller nyere
- Internett-tilgang for kart og værdata

## Biblioteker
### Standardbiblioteker/vist i kurset:
- Jetpack Compose: UI
- Material 3: Design
- Navigation Compose: Navigasjon mellom skjermer
- Kontlinx Serialization: Håndtering av JSON
- Ktor Client: HTTP-klient for å hente værdata fra eksterne tjenester, og ble valgt for god koroutine-støtte og integrasjon med Kotlin.

### Andre biblioteker
- Mapbox: Brukes for interaktive kart og visualisering av fylkesdata. Valgt for å bedre støtte for geografisk datavisualisering og tilpassede kartlag.
- TensorFlow Lite: Kjører en lokal ML-modell for å predikere trafikkrisiko basert på værdata og fylker. Dette gir raskere prediksjoner og virker uten internett. Modellen er trent i Python ved bruk av Tensorflow, numpy og pandas. Dette biblioteket krever API 26 eller høyere
- Accompanist SystemUiController: Gir muligheten til å gjøre statusbar gjennomsiktig, samt velge farge for OS-ikoner og fjerne navigasjonsbar
- MockK: Gjør det enklere å lage og konfigurere mock-objekter for testing,
- Kontlinx Coroutines Test: Tester koden som bruker koroutiner
- JUnit: Testbibliotek for å definere og kjøre tester.



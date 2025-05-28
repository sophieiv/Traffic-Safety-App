# Diagrammer og funksjonelle krav

# Innhold

#### Dette dokumentet inneholder syv ulike diagrammer:

Use case diagram: Pendler, Trafikant og Yrkessjåfør.

Klassediagram

Sekvensdiagram – Eksempel: Vis risikovarsel med fallback hvis API feiler

Aktivitetsdiagram – Eksempel: Se vær og risiko for valgt fylke

Tilstandsdiagram – Eksempel: Appflyt og hovedskjermer



### Krav til produktet
De viktigste funksjonelle kravene i appen er illustrert i våre syv diagrammer.

#### Vi anser følgende funksjonelle krav som de viktigste:

Risikogradering: Appen skal ha risikogradering av brukervalgt fylke.

Kart: Appen skal vise et kart basert på MapBox som visualiserer de fem fylkene Oslo, Akershus, Buskerud, Vestfold og Østfold. Hvert fylke skal ha fargekoding som reflekterer gjeldende risikonivå for trafikkulykker.

Værinfo: Systemet skal hente og vise værdata fra MET vær-API. Dette inkluderer nåværende temperatur, nedbør og minimumstemperatur.

Utvidet værinfo: Appen skal vise 10-dagers værvarsel med daglig temperatur, nedbør, samt risikonivå for hvert fylke.

Nødhjelp: Applikasjonen skal ha en dedikert skjerm for tilgang til nødetater 



## Use case diagram

### Use Case 1 : Pendler

***Som** en pendler som kjører daglig mellom Oslo og Vestfold*

***ønsker jeg** å kunne se risikogradering for trafikkulykker på et kart over fylkene jeg reiser gjennom både i farger og i tekst*

***slik at** jeg kan planlegge min reiserute basert på risikonivå*
![UseCaseDiagram_Pendler_2](https://github.uio.no/IN2000-V25/team-1/assets/11590/c5bdd5d7-dc67-4554-9f82-b37a67755bf6)

### Use Case 2: Trafikant

***Som** en trafikant som lett glemmer nødnumre*

***ønsker jeg** å ha rask tilgang til nødtjenester direkte i appen*

***slik at** jeg kan kontakte riktig nødetat raskt når det oppstår en kritisk situasjon i trafikken*
![UseCaseDiagram_Trafikant](https://github.uio.no/IN2000-V25/team-1/assets/11590/e0b7b2de-a203-4d46-92b1-a672585a1070)

### Use Case 3: Yrkessjåfør

***Som** en yrkessjåfør som kjører daglig leveranser mellom fylkene i Østlandsområdet*

***Ønsker jeg** å kunne se værvarsel og predikert risikonivå for de neste 10 dagene*

***Slik at** jeg kan planlegge mine leveranseruter på forhånd og unngå områder med høy risiko for trafikkulykker*
![UseCaseDiagram_Yrkessjåfør](https://github.uio.no/IN2000-V25/team-1/assets/11590/2cf973e2-d330-44a4-9b66-ae8782a093b2)

### Beskrivelse use case diagrammer
Use case diagrammene er basert på tre ulike use cases, som igjen er basert på de sentrale funksjonelle kravene.


## Klassediagram
<img width="639" alt="Klassediagram" src="https://github.uio.no/IN2000-V25/team-1/assets/11590/a6fdb207-e071-427f-9c1d-b868ae05c98a">

### Beskrivelse av klassediagram
Målet med klassediagrammet har vært å gi et tydelig overblikk over appens arkitektur. Det blir fokusert på følgende:
1. Hvordan vær-API integrasjonen er bygget opp
2. Sammenhengen mellom ulike komponenter i appen
3. Dataflyt fra API til brukergrensesnittet
4. Hvordan ansvar er fordelt mellom de ulike komponentene

## Sekvensdiagram - Eksempel: "Vis risikovarsel med fallback hvis API feiler”
<img width="641" alt="Sekvensdiagram_Fallback" src="https://github.uio.no/IN2000-V25/team-1/assets/11590/620258db-2ef5-4147-92ea-6bbe9c785132">

### Beskrivelse av sekvensdiagram
Sekvensdiagrammet viser fallback-håndtering når et API feiler ved visning av risikovarsel.
Flyten starter med at en bruker åpner appen, som deretter forsøker å hente værdata via et API. I det første scenariet er API-et tilgjengelig og returnerer værdata til appen. Appen bruker denne dataen til å beregne risiko via maskinlæringsmodellen, som returnerer "Moderat" risiko som vises til brukeren.
I det andre scenariet er API-et utilgjengelig. Appen håndterer dette ved å bruke lokale data for å beregne risiko via maskinlæringsmodellen, som resulterer i enten "Ukjent" risiko eller en estimert verdi. Denne fallback-dataen vises så til brukeren.
Diagrammet illustrerer hvordan systemet sikrer at brukeren alltid får informasjon om risiko, selv når den eksterne datakilden ikke er tilgjengelig.

## Aktivitetsdiagram (Flytdiagram) - Eksempel: "Se vær og risiko for valgt fylke”
### "Se vær og risiko for valgt fylke”
<img width="500" alt="Aktivitetsdiagram" src="https://github.uio.no/IN2000-V25/team-1/assets/11590/e975c7e7-511f-4311-8b53-304b60ec4ea7">

### Beskrivelse av aktivitetsflyten

Dette aktivitetsdiagrammet beskriver prosessen en bruker går gjennom for å se vær- og risikoinformasjon for et spesifikt fylke i "Tryggve"-appen.

1. **Start:** Prosessen starter.
2. **Bruker ser fylkesvalg (Oslo forhåndsvalgt):** Når brukeren kommer til relevant skjerm (typisk Dashboard/Hjem-skjermen), presenteres de for et fylkesvalg. Som standard er Oslo forhåndsvalgt.
3. **Ønsker å endre fylke? (Beslutning):** Brukeren har nå et valg:
    - **Ja (ønsker å endre):** Hvis brukeren vil se informasjon for et annet fylke enn det som er forhåndsvalgt eller sist valgt.
    - **Nei (fornøyd med nåværende valg):** Hvis brukeren er fornøyd med det forhåndsvalgte fylket (Oslo i starten) eller det fylket som allerede vises.
4. **Bruker velger et annet fylke (Aktivitet - hvis Ja fra pkt. 3):** Brukeren interagerer med fylkesvelgeren (f.eks. en dropdown-meny eller et kart) og velger et nytt fylke.
5. **Systemet viser vær og risiko for valgt fylke (Aktivitet - etter pkt. 4):** Appen henter og viser relevant værdata og det beregnede risikonivået (Moderat, Økt, Høy) spesifikt for det fylket brukeren nettopp valgte.
6. **Systemet viser vær og risiko for Oslo (Aktivitet - hvis Nei fra pkt. 3):** Hvis brukeren ikke gjorde et aktivt valg for å endre fylke, viser appen værdata og risikonivå for det forhåndsvalgte fylket, Oslo.
7. **Se informasjon for et annet fylke? (Beslutning - etter pkt. 5 eller 6):** Etter at informasjonen er vist (enten for Oslo eller et annet valgt fylke), har brukeren igjen et valg:
    - **Ja (ønsker å se et nytt fylke):** Brukeren ønsker å gjenta prosessen for å se data for et annet fylke. Flyten går da tilbake til aktiviteten "Bruker velger et annet fylke" (pkt. 4 via C i diagrammet ditt).
    - **Nei (ønsker ikke å se flere fylker nå):** Brukeren er ferdig med denne spesifikke aktiviteten for øyeblikket.
8. **Slutt:** Prosessen for denne spesifikke brukerinteraksjonen avsluttes.

Denne flyten fokuserer på kjernefunksjonaliteten knyttet til å velge et geografisk område og få presentert stedsspesifikk informasjon, noe som er sentralt i "Tryggve"-appen.


## Tilstandsdiagram - Eksempel: Appflyt og hovedskjermer
<img width="568" alt="Tilstandsdiagram" src="https://github.uio.no/IN2000-V25/team-1/assets/11590/ac5c96ec-4078-49f9-a8ff-f5601cfe6c3e">

### Beskrivelse av tilstandsdiagram
Dette tilstandsdiagrammet viser hvordan brukeren beveger seg mellom de sentrale skjermene i *Tryggve*-appen, samt hvilke handlinger som utløser tilstandsendringer. Hver tilstand representerer en skjerm i appen, mens overgangene illustrerer brukerinteraksjoner som navigasjon eller valg.

Appen starter i *Onboarding*, hvor brukeren får en introduksjon til hovedfunksjonene: Hjem, Kart, Nødhjelp og Profil. Etter introduksjonen, eller ved å hoppe over den, går brukeren videre til *Dashboard*, som fungerer som appens hovedvisning.

Fra *Dashboard* kan brukeren navigere til:

- **Kart** – viser geografisk trafikkrisiko og værdata.
- **Nødhjelp** – gir tilgang til kontakt med nødetater og varsling av ulykker.
- **Profil** – gir oversikt over brukerdata og innstillinger.

I *Nødhjelp*-visningen kan brukeren også gå videre til *Ulykke*, hvor det vises trinnvise instruksjoner for hvordan man skal handle ved en ulykke. Alle visninger har støtte for å lukke appen, samt overgang tilbake til *Dashboard*.

Diagrammet oppsummerer appens logiske tilstandsmaskin og gir en helhetlig oversikt over hvordan brukeren kan navigere mellom ulike deler av systemet.

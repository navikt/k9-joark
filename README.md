# k9-joark
![CI / CD](https://github.com/navikt/k9-joark/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/k9-joark/workflows/Alerts/badge.svg)

Inneholder integrasjon mot joark for å opprette jornalpost i forbindelse med søknader for ytelsen i Kapittel 9 i folketrygdeloven.

## Versjon 1
### Meldingsformat
- aktoer_id : AtkørID for personen dokumentene skal journalføres på
- norsk_ident: Norsk ident for personen dokumentene skal journalføres på
- soker_navn: Navn på personen dokumente skal journalføres på. Denner optional, og om satt er også mellomnavn optional.
- mottatt : tidspunkt for når dokumentene er mottatt på ISO8601 format
- dokumenter : En liste med lister av URL'er til dokumenter som skal journalføres som må peke på "k9-dokument"
- dokumenter : Må inneholde minst en liste, og hvert liste må inneholde minst en entry.
- dokumenter[0] : Vil bli "Hoveddokument" i Joark
- En liste med URL'er skal være > 1 om man ønsker å journalføre samme dokument på forskjellige format. For eksempel PDF & JSON

```json
{
	"aktoer_id": "123561458",
	"norsk_ident": "01234567890",
	"mottatt": "2018-12-18T20:43:32Z",
	"soker_navn": {
	  "fornavn": "Navn",
	  "mellomnavn": "Navn",
	  "etternavn": "Navnesen"
	},
	"dokumenter": [
		[
			"https://k9-dokument.nav.no/dokument/c049520b-eed9-42d0-8d48-b7c8e6e1467e",
			"https://k9-dokument.nav.no/dokument/c049520b-eed9-42d0-8d48-b7c8e6e1467f"
		],
		[
			"https://k9-dokument.nav.no/dokument/c049520b-eed9-42d0-8d48-b7c8e6e1467g"
		]
	]
}
```

### Metadata
#### Correlation ID vs Request ID
Correlation ID blir propagert videre, og har ikke nødvendigvis sitt opphav hos konsumenten
Request ID blir ikke propagert videre, og skal ha sitt opphav hos konsumenten

#### REST API
- Correlation ID må sendes som header 'X-Correlation-ID'
- Request ID kan sendes som heder 'X-Request-ID'
- Versjon på meldingen avledes fra pathen '/v1/{søknadsType}/journalforing' -> 1

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.

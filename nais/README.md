# nais

## Merk!
- I Azure heter denne applikasjonen `pleiepenger-joark` ettersom applikasjonen het det tidligere.

- Dette er èn av 5 apper som er manuelt opprettet i Azure før `aad-iac` ble opprettet.
Sertifikat (AZURE_* under) hentes derfor etter at de manuelt er lagt inn i Vault, ikke via mekanismene som er innført med og etter `aad-iac`

## Må settes i Vault
- `AZURE_PRIVATE_KEY_JWK`
- `AZURE_CERTIFICATE_HEX_THUMBPRINT`
- `SERVICE_ACCOUNT_CLIENT_ID`
- `SERVICE_ACCOUNT_CLIENT_SECRET`
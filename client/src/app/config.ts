/**
 * External API configuration.
 *
 * ORS_KEY is an OpenRouteService API key (free tier). ⚠️ This demo key has
 * been committed to a public repository — treat it as compromised: revoke it
 * at https://openrouteservice.org/dev/ and paste your own key here. Free keys
 * are rate-limited (≈40 req/min), which is why the app caches every computed
 * distance in the backend (`/api/distances`).
 */
export const ORS_KEY =
  'eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImQ0NjhjM2IzODY1MTQxODViZGYxOWM3NDQ3M2VlZjlkIiwiaCI6Im11cm11cjY0In0=';

/** Base URL of the French national address API (geocoding). */
export const CARTO_URL = 'https://api-adresse.data.gouv.fr';

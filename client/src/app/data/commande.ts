// ----------------------------------------------------
// TRADUCTION TYPESCRIPT DES OBJETS JAVA (REST-API)
// ----------------------------------------------------
export interface AdresseResponse {
  id: number;
  rue: string;
  ville: string;
  latitude: number;
  longitude: number;
}

export interface ClientResponse {
  id: number;
  nom: string;
  email: string;
  adresse: AdresseResponse;
}

export interface CommandeResponse {
  id: number;
  reference: string;
  montant: number;
  etatCommande: string;
  dateCommande: string;
  dateLivraisonSouhaitee?: string;
  client: ClientResponse;
}

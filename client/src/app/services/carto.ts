import { inject, Injectable } from '@angular/core';
import { Adresse } from '../data/adresse';
import { LatLngLiteral, LatLngTuple } from 'leaflet';
import { unparse } from "papaparse";
import { extractAdressesFromApiGouvResponseString } from '../utils/extractAdressesFromApiGouvResponseString';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { OptimizationResult, parseOptimizationResultP, RouteStepBase } from './OptimizationResult';
import { GeoJSONFeatureCollectionSchema, GeoJSONLineStringSchema } from 'zod-geojson';

import { ORS_KEY as orsKey, CARTO_URL as cartoURL } from '../config';

@Injectable({ providedIn: 'root' })
export class Carto {
  private readonly _httpClient = inject(HttpClient);

  public getAdressesFromCoordinates(L: readonly LatLngLiteral[]): Promise<readonly Adresse[]> {
    const formData = new FormData();
    const csvBlob = new Blob([unparse([...L], { delimiter: ';' })], { type: 'text/csv' });
    formData.append("lon", "lng"); formData.append("columns", "lat"); formData.append("data", csvBlob);
    const req$ = this._httpClient.post(`${cartoURL}/reverse/csv`, formData, { responseType: 'text' });
    return firstValueFrom(req$).then(extractAdressesFromApiGouvResponseString);
  }

 public optimize(params: { nbVehicules: number, maxTimePerVehicule: number, adresses: readonly Adresse[], parking: Adresse }): Promise<OptimizationResult> {

    const LVehicules = Array.from({ length: params.nbVehicules }, (_, i) => ({
      id: i + 1,
      profile: "driving-car",
      start: [params.parking.lng, params.parking.lat],
      end: [params.parking.lng, params.parking.lat]

    }));

    const Ljobs = params.adresses.map((a, i) => ({
      id: i + 1,
      location: [a.lng, a.lat],
      service: 300

    }));

    const req$ = this._httpClient.post('https://api.openrouteservice.org/optimization',
      { jobs: Ljobs, vehicles: LVehicules },
      { headers: { Authorization: orsKey } }
    );

    return firstValueFrom(req$).then(parseOptimizationResultP);
  }

  public getDirections(lngLatCoordinates: readonly RouteStepBase['location'][]): Promise<ReadonlyArray<LatLngTuple>> {
    const snappingRadiuses = lngLatCoordinates.map(() => -1);
    const req$ = this._httpClient.post('https://api.openrouteservice.org/v2/directions/driving-car/geojson', { coordinates: lngLatCoordinates, radiuses: snappingRadiuses }, { headers: { Authorization: orsKey } });
    return firstValueFrom(req$)
      .then(res => GeoJSONFeatureCollectionSchema.parseAsync(res))
      .then(fc => Promise.all(fc.features.map(f => GeoJSONLineStringSchema.parseAsync(f.geometry))))
      .then(L => L.flatMap(geojson => geojson.coordinates.map(c => [c[1], c[0]] as LatLngTuple)));
  }

  public async getDistanceMatrix(points: readonly Adresse[]): Promise<{ distances: number[][]; times: number[][]; fromCache: boolean }> {
    if (points.length === 0) return { distances: [], times: [], fromCache: false };

    //  pour Spring Boot
    const coordsRequest = points.map(p => ({ lat: p.lat, lng: p.lng }));

    try {
      // On demande au cache Spring Boot
      const reqCache$ = this._httpClient.post<{distances: number[][], times: number[][]}>(
        '/api/distances/get-matrix',
        coordsRequest
      );
      const cacheResponse = await firstValueFrom(reqCache$);

      console.log(" VICTOIRE : Matrice récupérée instantanément depuis le cache local !");
      return { distances: cacheResponse.distances, times: cacheResponse.times, fromCache: true };

    } catch (error) {
      //  Si Spring Boot répond 404 (incomplet), on appelle ORS
      console.log(" Cache incomplet : Appel à l'API OpenRouteService...");
      const reqOrs$ = this._httpClient.post<any>('https://api.openrouteservice.org/v2/matrix/driving-car', {
        locations: points.map(p => [p.lng, p.lat]), metrics: ['distance', 'duration'], units: 'km'
      }, { headers: { Authorization: orsKey } });

      const res = await firstValueFrom(reqOrs$);
      return { distances: res.distances, times: res.durations, fromCache: false };
    }
  }
}

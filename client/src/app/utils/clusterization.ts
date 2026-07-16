import { Adresse } from '../data/adresse';
import { Cluster } from '../data/Cluster';
import { RouteStepBase } from '../services/OptimizationResult';

export interface GlobalRoute {
  vehicleId: number;
  clusterIds: number[];
  totalDistance: number;
  totalTime: number;
  steps: readonly RouteStepBase[];
}

export function clusterizeByGrid(adresses: readonly Adresse[], pointsPerCluster: number = 35): readonly Cluster[] {
  if (adresses.length === 0) return [];

  const lats = adresses.map(a => a.lat);
  const lngs = adresses.map(a => a.lng);

  const minLat = Math.min(...lats);
  const maxLat = Math.max(...lats);
  const minLng = Math.min(...lngs);
  const maxLng = Math.max(...lngs);

  const nbClusters = Math.ceil(adresses.length / pointsPerCluster);
  const gridSize = Math.ceil(Math.sqrt(nbClusters));

  const latStep = maxLat === minLat ? 1 : (maxLat - minLat) / gridSize;
  const lngStep = maxLng === minLng ? 1 : (maxLng - minLng) / gridSize;

  const clusters: Cluster[] = [];
  let clusterId = 0;

  for (let i = 0; i < gridSize; i++) {
    for (let j = 0; j < gridSize; j++) {
      const clusterMinLat = minLat + i * latStep;
      const clusterMaxLat = minLat + (i + 1) * latStep;
      const clusterMinLng = minLng + j * lngStep;
      const clusterMaxLng = minLng + (j + 1) * lngStep;

      const adressesInCluster = adresses.filter(a => {
        const latIn = i === gridSize - 1 ? a.lat >= clusterMinLat && a.lat <= clusterMaxLat : a.lat >= clusterMinLat && a.lat < clusterMaxLat;
        const lngIn = j === gridSize - 1 ? a.lng >= clusterMinLng && a.lng <= clusterMaxLng : a.lng >= clusterMinLng && a.lng < clusterMaxLng;
        return latIn && lngIn;
      });

      if (adressesInCluster.length > 0) {
        clusters.push({
          id: clusterId++,
          adresses: adressesInCluster,
          bounds: { minLat: clusterMinLat, maxLat: clusterMaxLat, minLng: clusterMinLng, maxLng: clusterMaxLng }
        });
      }
    }
  }
  return clusters;
}
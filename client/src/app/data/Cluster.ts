import { Adresse } from './adresse';
import { ClusterBounds} from './ClusterBounds';
// Interface pour un cluster d'adresses
export interface Cluster {
  id: number;
  adresses: readonly Adresse[];
  bounds: ClusterBounds;
}
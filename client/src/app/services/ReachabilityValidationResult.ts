import { Adresse } from '../data/adresse';

export interface ReachabilityValidationResult {
  readonly reachable: readonly Adresse[];
  readonly unreachable: readonly Adresse[];
}

# Deflight

Deflight is a flights search aggregator that initially integrates with two suppliers (CrazyAir and ToughJet). A future iteration may add more suppliers.

### Assumptions

- Both suppliers can be requested for one-way flights
- ToughJet's response
  - `discount` percentage value between 0 and 100
  - `tax` value greater than or equal to 0

### Enhancements

- Addition of a cache (i.e. Redis) as a fallback when a supplier is not available with a short eviction policy
- Fine-grained error handling when parallelizing supplier's requests
  - Circuit-breakers
  - Retry mechanisms

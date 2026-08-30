// Single source of truth for values shared by schema, mutations, seed, and dashboard.
// Per working rules: no duplicated cross-file literals.

export const ENRICHMENT = {
  pending: "pending",
  done: "done",
  failed: "failed",
} as const;

export type EnrichmentStatus = (typeof ENRICHMENT)[keyof typeof ENRICHMENT];

export const FOLLOW_UP_DELAY_MS = 48 * 60 * 60 * 1000;

/** Prepended to spoken numbers that arrive without a country prefix (PLAN step 5). */
export const DEFAULT_COUNTRY_CODE = "+971";

export const PRODUCT_NAME = "We Just Met";

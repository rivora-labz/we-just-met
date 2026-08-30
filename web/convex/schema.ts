import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";
import { ENRICHMENT } from "./shared";

export default defineSchema({
  contacts: defineTable({
    name: v.string(),
    phone: v.string(),
    company: v.optional(v.string()),
    role: v.optional(v.string()),
    note: v.optional(v.string()),
    selfieId: v.optional(v.id("_storage")),
    linkedinUrl: v.optional(v.string()),
    enrichment: v.union(
      v.literal(ENRICHMENT.pending),
      v.literal(ENRICHMENT.done),
      v.literal(ENRICHMENT.failed),
    ),
    metAt: v.number(),
    followUpAt: v.number(),
  }).index("by_metAt", ["metAt"]),
});

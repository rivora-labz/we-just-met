import { mutation, query } from "./_generated/server";
import { v } from "convex/values";
import { internal } from "./_generated/api";
import { ENRICHMENT, FOLLOW_UP_DELAY_MS } from "./shared";

/** Save a captured contact. Called by the phone right after the WhatsApp send. */
export const save = mutation({
  args: {
    name: v.string(),
    phone: v.string(),
    company: v.optional(v.string()),
    role: v.optional(v.string()),
    note: v.optional(v.string()),
    selfieId: v.optional(v.id("_storage")),
  },
  handler: async (ctx, args) => {
    const metAt = Date.now();
    const contactId = await ctx.db.insert("contacts", {
      ...args,
      enrichment: ENRICHMENT.pending,
      metAt,
      followUpAt: metAt + FOLLOW_UP_DELAY_MS,
    });
    await ctx.scheduler.runAfter(0, internal.enrich.run, { contactId });
    return contactId;
  },
});

/** Short-lived URL the phone POSTs the selfie bytes to; returns a storageId. */
export const generateUploadUrl = mutation({
  args: {},
  handler: async (ctx) => await ctx.storage.generateUploadUrl(),
});

/** Live dashboard feed, newest first, selfie storage IDs resolved to URLs. */
export const list = query({
  args: {},
  handler: async (ctx) => {
    const rows = await ctx.db.query("contacts").withIndex("by_metAt").order("desc").collect();
    return Promise.all(
      rows.map(async (row) => ({
        ...row,
        selfieUrl: row.selfieId ? await ctx.storage.getUrl(row.selfieId) : null,
      })),
    );
  },
});

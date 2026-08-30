import { internalMutation } from "./_generated/server";
import { ENRICHMENT, FOLLOW_UP_DELAY_MS } from "./shared";

const HOUR_MS = 60 * 60 * 1000;

/** Demo rows so the dashboard is alive before the phone flow lands (build order step 2). */
const SEED_CONTACTS = [
  {
    name: "Sara Al Mansouri",
    phone: "+971501234001",
    company: "Convex",
    role: "Developer Advocate",
    note: "Loved the one-sided capture wedge, intro to their DX team",
    enrichment: ENRICHMENT.done,
    linkedinUrl: "https://www.linkedin.com/in/sara-al-mansouri",
    metAgoMs: 2 * HOUR_MS,
  },
  {
    name: "Omar Haddad",
    phone: "+971521234002",
    company: "Context.dev",
    role: "Founding Engineer",
    note: "Talked enrichment latency budgets, wants the demo link",
    enrichment: ENRICHMENT.done,
    linkedinUrl: "https://www.linkedin.com/in/omar-haddad",
    metAgoMs: 5 * HOUR_MS,
  },
  {
    name: "Priya Nair",
    phone: "+971551234003",
    company: "TheBlock",
    note: "Met at the partner booth, follow up about pilot venues",
    enrichment: ENRICHMENT.pending,
    metAgoMs: 26 * HOUR_MS,
  },
  {
    name: "Daniel Kim",
    phone: "+971561234004",
    company: "Collabute",
    role: "Hackathon Lead",
    note: "Asked for the pitch deck after judging",
    enrichment: ENRICHMENT.failed,
    metAgoMs: 47 * HOUR_MS,
  },
] as const;

export const seedContacts = internalMutation({
  args: {},
  handler: async (ctx) => {
    const existing = await ctx.db.query("contacts").first();
    if (existing) return { seeded: 0, skipped: true };
    const now = Date.now();
    for (const { metAgoMs, ...contact } of SEED_CONTACTS) {
      const metAt = now - metAgoMs;
      await ctx.db.insert("contacts", {
        ...contact,
        metAt,
        followUpAt: metAt + FOLLOW_UP_DELAY_MS,
      });
    }
    return { seeded: SEED_CONTACTS.length, skipped: false };
  },
});

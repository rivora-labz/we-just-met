import { v } from "convex/values";
import {
  internalAction,
  internalMutation,
  internalQuery,
  mutation,
} from "./_generated/server";
import { internal } from "./_generated/api";
import { ENRICHMENT } from "./shared";

/**
 * Context.dev enrichment (PLAN step 3). Key lives in the Convex deployment env.
 * Primary: People Enrichment API (paid plans; auto-lights-up on upgrade).
 * Fallback: Web Search API (free plan) targeting LinkedIn profiles.
 */
const CONTEXT_DEV_API_BASE = "https://api.context.dev/v1";
const PEOPLE_ENRICH_PATH = "/people/enrich";
const WEB_SEARCH_PATH = "/web/search";
const CONTEXT_DEV_API_KEY_ENV = "CONTEXT_DEV_API_KEY";

const MATCH_STATUS_CANDIDATE = "candidate";
const MIN_MATCH_SCORE = 30;
const LINKEDIN_PROFILE_PATH = "linkedin.com/in";
const SEARCH_RESULT_COUNT = 10;
/** LinkedIn result titles look like "Name - Role at Company". */
const TITLE_SEPARATOR = " - ";
const ROLE_ORG_SEPARATOR = " at ";

type EnrichedFields = {
  role?: string;
  company?: string;
  linkedinUrl?: string;
};

type EnrichmentMatch = {
  status?: string;
  score?: number;
  person?: {
    social_urls?: string[];
    current_role?: { title?: string; organization?: { name?: string } };
  };
};

type SearchResult = { url?: string; title?: string; description?: string };

/** Dashboard "re-enrich" button; also the retry path for failed rows. */
export const reEnrich = mutation({
  args: { contactId: v.id("contacts") },
  handler: async (ctx, { contactId }) => {
    await ctx.db.patch(contactId, { enrichment: ENRICHMENT.pending });
    await ctx.scheduler.runAfter(0, internal.enrich.run, { contactId });
  },
});

export const getContact = internalQuery({
  args: { contactId: v.id("contacts") },
  handler: (ctx, { contactId }) => ctx.db.get(contactId),
});

export const apply = internalMutation({
  args: {
    contactId: v.id("contacts"),
    enrichment: v.union(v.literal(ENRICHMENT.done), v.literal(ENRICHMENT.failed)),
    role: v.optional(v.string()),
    company: v.optional(v.string()),
    linkedinUrl: v.optional(v.string()),
  },
  handler: async (ctx, { contactId, ...fields }) => {
    const patch = Object.fromEntries(
      Object.entries(fields).filter(([, value]) => value !== undefined),
    );
    await ctx.db.patch(contactId, patch);
  },
});

export const run = internalAction({
  args: { contactId: v.id("contacts") },
  handler: async (ctx, { contactId }): Promise<void> => {
    const contact = await ctx.runQuery(internal.enrich.getContact, { contactId });
    const apiKey = process.env[CONTEXT_DEV_API_KEY_ENV];

    let fields: EnrichedFields | null = null;
    if (contact && apiKey && contact.company) {
      fields =
        (await enrichViaPeopleApi(apiKey, contact.name, contact.company)) ??
        (await enrichViaSearch(apiKey, contact.name, contact.company));
    }

    await ctx.runMutation(internal.enrich.apply, {
      contactId,
      enrichment: fields ? ENRICHMENT.done : ENRICHMENT.failed,
      ...fields,
    });
  },
});

async function callContextDev(apiKey: string, path: string, body: object): Promise<unknown> {
  const response = await fetch(CONTEXT_DEV_API_BASE + path, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  return response.ok ? response.json() : null;
}

async function enrichViaPeopleApi(
  apiKey: string,
  name: string,
  company: string,
): Promise<EnrichedFields | null> {
  const [first, ...rest] = name.trim().split(/\s+/);
  const last = rest.length > 0 ? rest.join(" ") : first;
  const data = (await callContextDev(apiKey, PEOPLE_ENRICH_PATH, {
    name: { first, last },
    company: { name: company },
  })) as { match?: EnrichmentMatch } | null;

  const match = data?.match;
  const person = match?.person;
  if (match?.status !== MATCH_STATUS_CANDIDATE || (match.score ?? 0) < MIN_MATCH_SCORE || !person) {
    return null;
  }
  return {
    role: person.current_role?.title,
    company: person.current_role?.organization?.name ?? company,
    linkedinUrl: person.social_urls?.find((url) => url.includes(LINKEDIN_PROFILE_PATH)),
  };
}

async function enrichViaSearch(
  apiKey: string,
  name: string,
  company: string,
): Promise<EnrichedFields | null> {
  const data = (await callContextDev(apiKey, WEB_SEARCH_PATH, {
    query: `"${name}" "${company}" site:${LINKEDIN_PROFILE_PATH}`,
    numResults: SEARCH_RESULT_COUNT,
  })) as { results?: SearchResult[] } | null;

  const profiles = (data?.results ?? []).filter((r) =>
    (r.url ?? "").includes(LINKEDIN_PROFILE_PATH),
  );
  const companyLower = company.toLowerCase();
  const best =
    profiles.find((r) =>
      `${r.title ?? ""} ${r.description ?? ""}`.toLowerCase().includes(companyLower),
    ) ?? profiles[0];
  if (!best?.url) return null;

  // "Sundar Pichai - CEO at Google" -> role "CEO"
  const headline = (best.title ?? "").split(TITLE_SEPARATOR).slice(1).join(TITLE_SEPARATOR);
  const role = headline.split(ROLE_ORG_SEPARATOR)[0].trim();
  return {
    role: role || undefined,
    company,
    linkedinUrl: best.url,
  };
}

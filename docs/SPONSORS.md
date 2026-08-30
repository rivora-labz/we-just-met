# Sponsor Integration Deep-Dive (judge-facing)

How "We Just Met" uses all three mandatory partner platforms: Devin (Cognition), Convex, and Context.dev. Every claim below is grounded in a file in this repo, with paths cited inline.

## 1. The product in one paragraph

We Just Met is one-sided contact exchange: the other person needs nothing. You hold your phone, they speak their name, number, company, and what you talked about; you take a selfie together. Thirty seconds later their contact is saved and enriched, and a composed WhatsApp message with the selfie is one tap from their phone. They installed nothing, scanned nothing, signed up for nothing (`README.md`, line 3). Every rival (HiHello, Blinq, Popl, Mobilo) needs both people in the system; this needs one (`README.md`, "The wedge").

## 2. Devin (Cognition): the app was built by Devin, against a committed spec

- **PLAN.md is the single source of truth for Devin sessions.** Its first line says exactly that: "Single source of truth for Devin sessions. Deviations require a note here first" (`PLAN.md`, line 3). Every build step carries an inline DONE verdict with date and evidence (steps 2, 3, 4 in `PLAN.md`, "Build order"), so the spec doubles as a build log a judge can audit.
- **Two-session pattern.** A planning/brainstorm session and a build session ran in parallel on the same repo: the planning side writes decisions into `PLAN.md` and `README.md` (hard cuts, cut lines, flow changes like the founder-timestamped "FLOW CHANGE, 2026-08-30 15:35" in `PLAN.md`), while the build session executes steps and writes verdicts back into the same file. The spec is the interface between the two sessions.
- **Back-to-front build order, riskiest seam first.** Step 1 was the WhatsApp send seam with hardcoded data, and the risky `jid` intent extra was validated on a real Fold 4 device in the first 30 minutes: "VERDICT (step 1, 2026-08-30, Fold 4 real device): jid PASSES. Opens directly inside the target chat, image + text staged" (`PLAN.md`, stack table, Send row). Fallbacks were locked empirically before anything upstream existed, so the demo was green at every hour boundary (`README.md`, "Architecture").
- **Design handoff via HTML artifacts.** The capture orb's approved visual spec is a standalone HTML file, `design/orb-preview.html`, treated as the 1:1 source of truth for the Compose implementation: "approved visual spec = design/orb-preview.html, open it in a browser, copy it into Compose 1:1... timings, colors, and easings in the HTML are the source of truth" (`PLAN.md`, "Design language"). A second artifact, `design/flow-diagram.html`, documents the flow. This is how a human founder reviews visuals and a Devin session implements them without ambiguity.
- **Honest deviations, recorded in code.** Where the plan changed, the deviation is noted at the seam itself, e.g. the extraction action's docstring: "PLAN deviation (noted in PLAN.md): EXTRACTION_API_KEY was never provisioned, so this is a deterministic server-side extractor instead of a cloud LLM. Same fixed JSON schema, same action seam" (`web/convex/extract.ts`, lines 5-11).

## 3. Convex: the follow-up engine and the entire backend

- **Schema.** One `contacts` table: name, phone, company, role, note, selfieId (`v.id("_storage")`), linkedinUrl, enrichment status union (pending/done/failed), metAt, followUpAt, indexed by metAt (`web/convex/schema.ts`). Status literals live once in `web/convex/shared.ts` and are shared by schema, mutations, seed, and dashboard.
- **File storage for selfies.** The phone asks for a short-lived upload URL (`contacts.generateUploadUrl`, `web/convex/contacts.ts` lines 30-33), POSTs the selfie bytes, and the dashboard query resolves storage IDs to serving URLs live (`contacts.list`, lines 36-46).
- **Live dashboard subscription.** The judge-facing dashboard (Vite + React + Convex client, `web/`) subscribes to `contacts:list`; when a contact is saved on stage, the row appears in real time with selfie, enrichment badge, and a "nudge in 47h" countdown chip (`PLAN.md`, screen 5). No polling, no refresh.
- **Server actions keep every key server-side.** Transcript-to-card extraction is a Convex action (`web/convex/extract.ts`), and Context.dev enrichment is an internal action (`web/convex/enrich.ts`) whose API key is read from the Convex deployment environment (`CONTEXT_DEV_API_KEY`, `enrich.ts` lines 19, 80). Nothing sensitive ships in the APK.
- **Scheduler-driven pipeline and the 48h follow-up.** The save mutation schedules enrichment immediately via `ctx.scheduler.runAfter(0, internal.enrich.run, ...)` and stamps `followUpAt = metAt + FOLLOW_UP_DELAY_MS` (48h, defined once in `web/convex/shared.ts` line 12) for the follow-up nudge (`web/convex/contacts.ts` lines 16-26). The same scheduler primitive powers the 48h follow-up function (PLAN step 6), and the dashboard renders the countdown from `followUpAt`.
- **Cloud prod, provisioned programmatically.** Not a local dev sandbox: project `wejustmet`, deployment `bright-meadowlark-411` (https://bright-meadowlark-411.convex.cloud), provisioned via the Convex Management API with a team token; all seams (save, upload, live subscription, storage URL) proven on cloud prod (`PLAN.md`, step 2 DONE verdict).
- **Native Android client.** The phone talks to the same deployment through `android-convexmobile` 0.8.0 (`gradle/libs.versions.toml` line 22): `client.subscribe` drives the Home screen's live contact list, `client.action` runs extraction, `client.mutation` saves and gets upload URLs (`app/src/main/java/app/wejustmet/data/ConvexRepo.kt`). Proven end to end on the Fold 4: "Sarah Chen row landed on phone Home + dashboard with selfie, enrichment auto-fired" (`PLAN.md`, step 4 DONE verdict).

## 4. Context.dev: the enrichment layer, with an honest engineering story

All of it lives in `web/convex/enrich.ts`, running inside a Convex action so the key never touches the client.

- **What it does.** Given the spoken name + company, it returns role, company, and a public LinkedIn URL, patched onto the contact row and lighting up the dashboard's enrichment badge live.
- **The honest part.** The People Enrichment API (`/people/enrich`) returned 403 on the hackathon key, that endpoint is paid-plan only (`PLAN.md`, step 3 DONE verdict). Rather than fake it or drop the partner, we shipped a two-tier action:
  1. **Tier 1, People Enrichment API** (`enrichViaPeopleApi`, `enrich.ts` lines 109-131): full structured person match, candidate-status and match-score gated, role and organization from `current_role`, LinkedIn URL from `social_urls`.
  2. **Tier 2, Web Search API fallback** (`enrichViaSearch`, lines 133-161, free plan): searches `"name" "company" site:linkedin.com/in`, filters results to real LinkedIn profile URLs, prefers a result whose title/description mentions the company, and parses the role from the result title, "Sundar Pichai - CEO at Google" -> role "CEO" (comment at line 153).
  The action tries tier 1 and falls through to tier 2 in one expression (`enrich.ts` lines 84-86). Upgrading the Context.dev plan lights up the richer path with zero code changes.
- **Proven live on prod.** A real-person lookup (Sundar Pichai / Google) returned role CEO plus the real LinkedIn URL, visible on the live dashboard (`PLAN.md`, step 3 DONE verdict).
- **Wired into the product, not a demo stub.** The save mutation auto-schedules enrichment on every capture (`contacts.ts` line 24), and the dashboard's "re-enrich" button (`reEnrich` mutation, `enrich.ts` lines 46-53) is the retry path: it resets the row to pending and reschedules the action, making failures recoverable in front of judges.

## 5. Which platform powers which demo moment

| Demo moment (60s script) | Powered by | Evidence |
|---|---|---|
| The app exists at all, spec-to-verdict build trail | Devin CLI sessions | `PLAN.md` DONE verdicts, `design/orb-preview.html` |
| Speak -> structured contact card | Convex action `extract:run` | `web/convex/extract.ts`, `ConvexRepo.kt` line 57 |
| Selfie lands in the cloud | Convex file storage | `contacts.generateUploadUrl`, `ConvexRepo.kt` lines 81-98 |
| WhatsApp message staged in their chat | Devin-validated jid seam (step 1) | `PLAN.md` Send row verdict |
| Row appears on the big-screen dashboard, live | Convex subscription | `contacts.list`, dashboard in `web/` |
| Role + LinkedIn URL fill in seconds later | Context.dev two-tier action | `web/convex/enrich.ts` |
| "Nudge in 47h" countdown chip | Convex scheduler + `followUpAt` | `contacts.ts` line 22, `shared.ts` line 12 |

## 6. Why this matters

Each platform is load-bearing, not decorative. Remove Devin and there is no app: every feature was a Devin session driven from a committed spec, and the build trail proves it. Remove Convex and the demo breaks at three separate points: the card never extracts, the selfie has nowhere to live, and the judge-facing dashboard goes dark, because Convex is simultaneously the database, the file store, the compute layer, the scheduler, and the realtime channel. Remove Context.dev and the product loses its second act: capture without enrichment is a contacts app, and the enrichment badge lighting up on the dashboard is the moment the row becomes a relationship. The 25% partner-depth criterion is not bolted on here; the partner stack is the architecture (`README.md`, partner table).

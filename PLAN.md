# We Just Met, build spec

Single source of truth for Devin sessions. Deviations require a note here first.

## Stack (final)

| Layer | Choice | Why |
|---|---|---|
| App | Android, Jetpack Compose, Material 3, single Activity, single Gradle module | Fastest agent-buildable path; multi-module setup risk cut |
| Core | `core/` pure Kotlin package (zero Android imports): models, templating, state machines | KMP-ready story preserved without Gradle overhead |
| STT | `SpeechRecognizer` (platform, on-device). LOCKED per hard cut 2. Dumb ears + smart brain + human net: rough transcript is fine, cloud extraction normalizes, editable Review card catches the rest | Free, zero keys, live partials for the orb, no upload latency |
| Extraction | Convex **action** -> cloud LLM -> fixed JSON schema | Key server-side; Convex load-bearing |
| Backend | Convex: `contacts` table, file storage (selfies), enrichment action (Context.dev), scheduled follow-up function | Mandatory partner, powers dashboard + follow-up |
| Enrichment | Context.dev web API from Convex action: name + company -> role, company info, LinkedIn public URL | Mandatory partner |
| Send | `ACTION_SEND` + `EXTRA_STREAM` + `jid` extra -> WhatsApp with image. **VERDICT (step 1, 2026-08-30, Fold 4 real device): jid PASSES.** Opens directly inside the target chat, image + text staged, one tap on WhatsApp's send arrow. Fallback A (ContactsContract insert) NOT needed. Fallback B (share sheet, no jid) also works but is lengthy manual (pick app, pick recipient); keep only as emergency path. Sender resolves at runtime: `com.whatsapp` first, `com.whatsapp.w4b` fallback (both on test device) | No infra, real image in thread |
| Dashboard | Vite + React + Convex client, one page | Live demo surface for judges |
| Camera | CameraX front-lens embedded preview with countdown auto-shutter (selfie fires itself after capture ends); fallback = `TakePicture` + manual shutter if CameraX drags | Countdown flow needs in-app camera |

## Naming (final)

| Surface | Name |
|---|---|
| Android launcher label | **Just Met** (short, fits under the icon without ellipsis) |
| Web dashboard, README, pitch, everything else | **We Just Met** |
| Android `applicationId` | `app.wejustmet` |

Both names defined once: launcher label in `strings.xml` (`app_name`), product name in `core` config for templates/dashboard. Never inline.

## Design language (final: Rivora Labz light theme, ported from Snook-A-Look `SnookColors.light()`)

Light theme only. No dark mode, no settings screen. All values below are the single source of truth, defined once in `core/Theme` tokens, never inline.

| Token | Value | Use |
|---|---|---|
| bgPrimary | #FAFAF7 | cream app background |
| surfaceCard | #FFFFFF | cards |
| surfaceElevated / surfaceInput | #F7F7F4 / #F4F4F0 | sheets, text fields |
| brandGreen / brandPrimary | #0B3D2E / #12573F | forest green brand family |
| brandPrimaryHover / brandGreenHover | #166B4E / #145A44 | gradient tops, pressed |
| accentGold | #D4AF37 (as text: #8A6A00 for 4.85:1 on cream) | enrichment badge, countdown chip, sparingly |
| textPrimary / textSecondary / textMuted | #0A0A0A / 75% black / 60% black | WCAG-checked on cream |
| borderControl | #8F8F8F (1dp) | disabled CTA outline, input borders |
| statusSuccess / statusError | #27AE60 / #C0392B | |

**Buttons (ported from Snook `PrimaryGreenButton` + `pressScale`):**
- Primary CTA: 52dp tall, 12dp rounded rect, vertical gradient brandPrimaryHover -> brandPrimary -> brandGreen, white text 16sp, fills width.
- Press interaction: `pressScale` modifier via `graphicsLayer` (GPU, no layout invalidation), pressed scale 0.952, spring release (DampingRatioMediumBouncy, StiffnessMediumLow), 220ms fire delay before onClick, double-fire guard.
- Loading state: 24dp spinner in place of label. Disabled: flat surfaceCard fill + 1dp borderControl outline (never looks dead on cream).
- Secondary button: white fill, 1dp borderControl, brandPrimary text, same shape + pressScale.

**Orb (FINAL, approved visual spec = `design/orb-preview.html`, open it in a browser, copy it into Compose 1:1):**
- 150dp circle, radial gradient (light at 32%/28% offset): brandPrimaryHover -> brandPrimary (45%) -> brandGreen. Soft drop shadow (brand green 35%), inner bottom shade.
- Idle listening: breathe scale 1.0 -> 1.06, 2.6s ease-in-out infinite. Two expanding halo rings (2dp, brandPrimary 35% alpha, scale 1 -> 1.75 fading to 0, 2.6s, second delayed 1.3s).
- Actively hearing speech: 178dp gold ring (3dp, accentGold) fades/scales in over 250ms, glow pulse 1.1s; orb breathe speeds up to 1.1s; "hearing you" caption in goldText; live partial transcript below (last ~18 words, textSecondary bold on dim italic).
- Tap orb (or silence timeout) -> countdown overlay: cream 96% scrim, "Ready for selfie" in brandPrimary, digits 3-2-1-GO in brandGreen at 800ms beats, sub-line "extraction already running in the background".
- Countdown timings, colors, and easings in the HTML are the source of truth; implement with Compose animate*AsState/rememberInfiniteTransition, tokens from core/Theme only.

Cards: white, 24dp radius, soft shadow (no hard borders). Generous whitespace, 4/8dp spacing rhythm.

## Screens (5 app + 1 web)

**FLOW CHANGE (founder, 2026-08-30 15:35): Capture orb IS the home screen (first thing every user sees). Recent-contacts list moved behind a hamburger side menu ("People you met"), owner setup also in the menu ("Your details"). Screens 3+4 merged: Review card carries the auto-populated editable message and a single "Send on WhatsApp" CTA; the separate Send screen is deleted. Screen 0 onboarding shipped (not cut): implemented with SharedPreferences instead of DataStore (sync first read, no start-screen flicker).**

### 0. First-run setup (one-time, skippable)
- Shown once after install: single card, "This is you."
- Fields: my name, my WhatsApp number, my LinkedIn URL, optional photo.
- Optional "Enrich me" button: Context.dev web API fills my role/company from name (same enrichment action, pointed at self).
- Stored locally (DataStore). Feeds the outgoing message template (my name, my LinkedIn URL). Editable later by long-press on app title.
- If behind schedule: fields fall back to constants in one config object, screen skipped.

### 1. Home
- App name top. Recent contacts list from Convex (avatar = selfie thumb, name, company, "met 2h ago", enrichment badge).
- Dominant bottom CTA: full-width accent button "We just met" -> Capture.
- Empty state: single line "Meet someone. Hand them your phone."

### 2. Capture
- Fullscreen orb center, pulsing while listening.
- Live partial transcript below orb, dimmed.
- Prompt line above orb: "Hi! Say your name, number, company and what we talked about."
- Auto-stop on silence OR tap orb to stop -> orb collapses into "Ready for selfie, 3, 2, 1, go" countdown overlay.
- Extraction fires in parallel the moment capture stops (countdown hides the LLM latency).
- At zero: CameraX front camera auto-captures the selfie -> navigate Review with card + selfie both pre-filled.
- Mic + camera permissions requested here, not at app start.

### 3. Review (card + selfie arrive pre-filled)
- Contact card: name, phone, company, role, "what we talked about" note. Each field tappable-editable (plain TextField inline).
- Selfie thumb at top of card, already captured by the countdown. Tap thumb -> retake (countdown again). If extraction is still in flight, fields show shimmer and fill in when it lands.
- Bottom CTA "Compose message" disabled until name + phone present.

### 4. Send
- WhatsApp-style message preview bubble: selfie image + templated text (their name, my name, my LinkedIn URL, the talked-about note, "great meeting you").
- Text editable in place.
- CTA "Send on WhatsApp" -> fires intent with image + jid from captured phone number.
- On return: save contact to Convex (mutation + selfie upload), fire enrichment action async, save to device contacts (ContactsContract), then full-screen success moment (screen 4b).

### 4b. Success moment (replaces toast)
- Full-screen Lottie burst (bundled JSON asset, `lottie-compose`), accent-colored on the dark surface.
- Headline: one of a rotating quote list defined once in `core` config (never inline literals), e.g. "Now they can't forget you.", "See you on the other side.", "The follow-up is already working."
- Sub-line: "Saved. Nudge in 48h."
- Auto-dismisses to Home after ~2.5s, or tap anywhere to skip.

### 5. Web dashboard (judge-facing)
- One page: table/cards of contacts, live via Convex subscription.
- Columns: selfie, name, company, role (fills in live when enrichment lands), met-at, follow-up countdown chip ("nudge in 47h").
- Header stat row: people met today, enriched count.

## Data model (Convex)

```ts
contacts: {
  name: v.string(),
  phone: v.string(),
  company: v.optional(v.string()),
  role: v.optional(v.string()),
  note: v.optional(v.string()),        // what we talked about
  selfieId: v.optional(v.id("_storage")),
  linkedinUrl: v.optional(v.string()), // from enrichment
  enrichment: v.union(v.literal("pending"), v.literal("done"), v.literal("failed")),
  metAt: v.number(),
  followUpAt: v.number(),              // metAt + 48h
}
```

Extraction JSON schema (LLM output, fixed): `{ name, phone, company, role, note }`, all strings, missing -> empty string.

## Build order (back-to-front, demo green at every step)

1. Send seam: hardcoded contact + bundled image -> WhatsApp intent. Test `jid` on the real phone in the first 30 min; lock in fallback empirically. APK on phone.
2. Convex project: schema, save mutation, selfie upload, dashboard page reading table (seed rows via script, phone not needed). **DONE 2026-08-30 (web/): all seams proven on Convex CLOUD prod (save, upload, live subscription, storage URL). Project `wejustmet`, deployment `bright-meadowlark-411` (https://bright-meadowlark-411.convex.cloud), provisioned via Management API with the team token in .env (CONVEX_TEAM_TOKEN); prod CONVEX_DEPLOY_KEY + CONVEX_URL also in .env. Deploy = `cd web && CONVEX_DEPLOY_KEY=... npx convex deploy`.**
3. Context.dev enrichment action, tested from a dashboard "re-enrich" button with hardcoded name/company, decoupled from phone flow. (Partner-depth demoable even if later steps slip.) **DONE 2026-08-30: `web/convex/enrich.ts`. VERDICT: People Enrichment API (`/people/enrich`) returns 403 on the hackathon key (paid plans only); shipped a two-tier action: people API first, Web Search API (`/web/search`, free plan) fallback targeting linkedin.com/in results, role parsed from result title. Upgrade the Context.dev plan and the richer path lights up with zero code changes. Proven on prod: Sundar Pichai/Google -> role CEO + real LinkedIn URL, live on dashboard. Save mutation auto-schedules enrichment; re-enrich button = retry path. Key in Convex deployment env, not client.**
4. Screens 0-4 with hardcoded data, navigation, theme, saving to Convex for real. **DONE 2026-08-30: Home (live Convex subscription via android-convexmobile 0.8.0), Capture (orb per orb-preview spec + 3-2-1-GO countdown, hardcoded demo draft pending step 5), Review (editable fields, CTA gated on name+phone), Send (template bubble, jid WhatsApp send, selfie upload + save mutation), Success 4b (rotating quotes, 2.5s auto-dismiss). Screen 0 skipped per cut line 3: owner profile = AppConfig constants (OWNER_LINKEDIN_URL still blank, set when founder provides). Proven on Fold 4: full flow walked, Sarah Chen row landed on phone Home + dashboard with selfie, enrichment auto-fired.**
5. STT -> Convex extraction action -> Review populated for real. Extraction normalizes digits server-side (default country code from config, not a literal). **DONE 2026-08-30: SpeechCapture (platform SpeechRecognizer, partials -> live transcript tail, RMS -> gold hearing ring, silence auto-stop w/ no-match restarts, tap-to-stop). Extraction = `web/convex/extract.ts` action. DEVIATION: EXTRACTION_API_KEY was never provisioned, so extraction is a deterministic server-side parser (spoken-digit words + digit runs -> normalized +971 E.164, name/company/role/note patterns), same fixed JSON schema + action seam, LLM swap touches one file. Proven via CLI: demo-script transcript -> full correct card incl. "plus nine seven one..." -> +971551758694. Countdown hides extraction latency; 6s timeout falls back to transcript-as-note; Review card stays the human net.**
6. Countdown selfie: CameraX front-lens auto-capture wired to end of capture + scheduled follow-up function + wiring.
7. ContactsContract save (unless already required by jid fallback), polish, rehearse the 60s demo 5x.

Stretch (only if all 7 green before 15:30, per hard cut 1): Context.dev browser action finds their LinkedIn profile and sends the connect request from the dashboard.

## Cut lines (if behind at 15:30, drop in this order)

1. ContactsContract device save (unless jid fallback depends on it)
2. Scheduled follow-up function (fake countdown chip on dashboard)
3. First-run setup screen (owner profile falls back to config object)
4. Role/note display polish
Never cut: send seam, selfie, dashboard live update, inline field editing (phone field minimum, it is the STT safety net).

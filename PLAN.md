# We Just Met, build spec

Single source of truth for Devin sessions. Deviations require a note here first.

## Stack (final)

| Layer | Choice | Why |
|---|---|---|
| App | Android, Jetpack Compose, Material 3, single Activity | Fastest agent-buildable path; KMP-ready `:core` module keeps the story |
| Core | `:core` pure Kotlin module: models, templating, state machines | No Android deps, portable |
| STT | `SpeechRecognizer` (platform) | Free, on every phone, zero setup |
| Extraction | Convex **action** -> cloud LLM -> fixed JSON schema | Key server-side; Convex load-bearing |
| Backend | Convex: `contacts` table, file storage (selfies), enrichment action (Context.dev), scheduled follow-up function | Mandatory partner, powers dashboard + follow-up |
| Enrichment | Context.dev web API from Convex action: name + company -> role, company info, LinkedIn public URL | Mandatory partner |
| Send | `ACTION_SEND` + `EXTRA_STREAM` + `jid` extra -> WhatsApp with image | No infra, real image in thread |
| Dashboard | Vite + React + Convex client, one page | Live demo surface for judges |
| Camera | CameraX, front lens default | Selfie step |

## Design language

Dark theme only. Near-black surface (#0E0F12), one accent (electric teal #2DD4BF), white text, large rounded cards (24dp), generous whitespace. Orb = pulsing accent circle, scale + alpha animation while listening. No light mode, no settings screen.

## Screens (4 app + 1 web)

### 1. Home
- App name top. Recent contacts list from Convex (avatar = selfie thumb, name, company, "met 2h ago", enrichment badge).
- Dominant bottom CTA: full-width accent button "We just met" -> Capture.
- Empty state: single line "Meet someone. Hand them your phone."

### 2. Capture
- Fullscreen orb center, pulsing while listening.
- Live partial transcript below orb, dimmed.
- Prompt line above orb: "Hi! Say your name, number, company and what we talked about."
- Auto-stop on silence OR tap orb to stop -> extraction spinner in-place -> navigate Review.
- Mic permission requested here, not at app start.

### 3. Review + Selfie
- Contact card: name, phone, company, role, "what we talked about" note. Each field tappable-editable (plain TextField inline).
- Selfie slot at top of card: empty state = dashed circle "Add selfie" -> CameraX fullscreen capture, front camera, single shutter, confirm/retake -> thumb fills slot.
- Bottom CTA "Compose message" disabled until name + phone present.

### 4. Send
- WhatsApp-style message preview bubble: selfie image + templated text (their name, my name, my LinkedIn URL, the talked-about note, "great meeting you").
- Text editable in place.
- CTA "Send on WhatsApp" -> fires intent with image + jid from captured phone number.
- On return: save contact to Convex (mutation + selfie upload), fire enrichment action async, save to device contacts (ContactsContract), navigate Home. Toast "Saved. Follow-up scheduled."

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

1. Send seam: hardcoded contact + bundled image -> WhatsApp intent. APK on phone.
2. Screens 1-4 with hardcoded data, navigation, theme.
3. Convex project: schema, save mutation, selfie upload, dashboard page reading table.
4. STT -> Convex extraction action -> Review populated for real.
5. CameraX selfie into slot.
6. Context.dev enrichment action + dashboard live-fill + scheduled follow-up function.
7. ContactsContract save, polish, rehearse.

## Cut lines (if behind at 15:30, drop in this order)

1. ContactsContract device save
2. Scheduled follow-up function (fake countdown chip on dashboard)
3. Inline field editing (display-only card)
Never cut: send seam, selfie, dashboard live update.

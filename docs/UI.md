# We Just Met, UI and Design Document

Product name: **We Just Met**. Android launcher label: **Just Met** (`strings.xml` `app_name`).
Sources of truth: `PLAN.md` (Design language + Screens), `design/orb-preview.html` (approved orb spec),
`design/flow-diagram.html` (flow + palette). Shipped code lives under `app/src/main/java/app/wejustmet/`.
Where shipped code differs from the spec, the shipped truth is documented and the delta is flagged as
**spec vs shipped**.

## 1. Design principles

- **Light theme only.** No dark mode, no theme settings. Rivora Labz palette ported from Snook-A-Look
  `SnookColors.light()`: forest green on cream, gold as a scarce accent.
- **One primary CTA per screen.** A single full-width green gradient button carries each screen's action.
- **Press feedback everywhere.** Every tappable primary control scales to 0.952 with a spring release.
  Nothing feels dead on the cream background, including disabled states (outlined, never greyed-to-invisible).
- **Demo-first.** The payoff renders on our screen: the orb, the countdown, the success moment, and the
  live judge-facing dashboard are all designed to be watched, not just used.
- **Tokens defined once.** All values live in `ui/Theme.kt` (`Tokens`) and `core/AppConfig.kt`; never inline.

## 2. Token reference (shipped in `ui/Theme.kt`, verified against PLAN.md)

| Token | Value | Usage |
|---|---|---|
| BgPrimary | #FAFAF7 | cream app background, countdown scrim base |
| SurfaceCard | #FFFFFF | cards, disabled CTA fill |
| SurfaceElevated | #F7F7F4 | sheets |
| SurfaceInput | #F4F4F0 | unfocused text field container |
| BrandGreen | #0B3D2E | orb gradient outer, gradient bottom, headings, icons |
| BrandPrimary | #12573F | brand mid tone, halos, focused field border, avatars |
| BrandPrimaryHover | #166B4E | gradient tops (button + orb highlight) |
| BrandGreenHover | #145A44 | pressed variant (defined, currently unreferenced in app code) |
| AccentGold | #D4AF37 | gold hearing ring, success checkmark |
| AccentGoldText | #8A6A00 | gold as text (4.85:1 on cream): "hearing you", enriched badge |
| TextPrimary | #0A0A0A | headings, field values |
| TextSecondary | 75% black (0xBF000000) | body copy, card sublines |
| TextMuted | 60% black (0x99000000) | hints, transcripts, timestamps, disabled CTA label |
| BorderControl | #8F8F8F | 1dp disabled CTA outline, unfocused input borders |
| StatusSuccess | #27AE60 | success status |
| StatusError | #C0392B | Material error slot |

No discrepancies: every PLAN.md token shipped with the exact hex. Note: PLAN lists
`surfaceElevated / surfaceInput` on one row; shipped code splits them into two tokens with the same values.

## 3. Typography and spacing

No custom Material `Typography` object shipped; the app sets sizes per text, forming this de facto scale:

| Role | Size / weight | Where |
|---|---|---|
| Countdown digits | 110sp ExtraBold | 3-2-1-GO overlay |
| Success headline | 24sp Bold | rotating quote |
| Screen titles | 22sp Bold (drawer/People title: ExtraBold) | Capture prompt, Review, drawer header |
| CTA label | 16sp SemiBold | PrimaryGreenButton |
| Body | 15sp | prompt line, transcript, drawer items, sublines |
| Card subline | 13sp | contact role/company |
| Caption / badges | 12-13sp | hints, "met 2h ago", enriched badge |

Spacing rhythm is 4/8dp multiples: 8, 12, 16, 24, 32, 36dp paddings throughout. Screen gutters 24dp
(Capture uses 36dp horizontal to match the HTML preview). Cards: white, **24dp corner radius**, soft shadow
(`shadowElevation = 2.dp`), no hard borders. Buttons: **12dp corner radius**.

## 4. Components

### Primary button (`ui/PrimaryGreenButton.kt`, constants in `ButtonSpec`)
- 52dp tall, fills width, 12dp rounded rect.
- Fill: vertical gradient BrandPrimaryHover -> BrandPrimary -> BrandGreen. Label white 16sp SemiBold.
- Press: `graphicsLayer` scale to **0.952** (GPU only, no layout invalidation), spring release
  (DampingRatioMediumBouncy, StiffnessMediumLow).
- Fire: **220ms delay** before `onClick` plus a `firing` double-fire guard.
- Loading: 24dp white `CircularProgressIndicator` (2dp stroke) replaces the label; gradient stays.
- Disabled: flat SurfaceCard fill + **1dp BorderControl (#8F8F8F) outline**, label in TextMuted. Never looks
  dead on cream.

### Secondary button
Spec (PLAN + orb-preview.html): white fill, 1dp BorderControl, BrandPrimary text, same shape + pressScale.
**Spec vs shipped:** no dedicated Secondary composable exists yet; no screen currently needs one
(secondary actions ship as text/icon affordances: retake hint, drawer items, back arrow).

### Chips and badges
- Enriched badge (app, `PeopleScreen`): AccentGoldText 12sp SemiBold text "enriched" on the contact card.
- Dashboard chips (web `theme.css` classes): `chip-pending` ("enriching..."), `chip-done` ("enriched"),
  `chip-failed`, plus the gold follow-up countdown chip ("nudge in 47h", "nudge due").

### Cards
White SurfaceCard, RoundedCornerShape(24dp), shadowElevation 2dp, 16-24dp inner padding. Contact card row:
48dp circular avatar (BrandPrimary with white initial), name 16sp SemiBold, subline 13sp TextSecondary,
"met Xm/h/d ago" 12sp TextMuted, gold badge trailing.

### Text fields
Material3 `OutlinedTextField` everywhere (Review, Onboarding): unfocused = SurfaceInput container +
BorderControl border; focused = SurfaceCard container + BrandPrimary border and label. Single-line for
fields, multiline for the message body.

## 5. The Orb (crown jewel, `CaptureScreen.kt` `OrbSpec`, locked to `design/orb-preview.html`)

Stage: 260dp box, orb centered.

**Body:** 150dp circle. Radial gradient centered at **32% x / 28% y** offset:
BrandPrimaryHover (0) -> BrandPrimary (**45%**) -> BrandGreen (100%), radius = orb width.
**Spec vs shipped:** the HTML's drop shadow (brand green 35%) and inner bottom shade
(`inset 0 -8px 22px` black 25%) did not ship; the Compose orb is flat gradient only.

**Idle listening:** breathe scale 1.0 -> **1.06**, full cycle **2.6s** (tween of 1300ms each way,
RepeatMode.Reverse). Two expanding halo rings: 150dp start, **2dp stroke BrandPrimary**, alpha starts at
0.7 (spec says visible ring at 35% alpha color; shipped draws full BrandPrimary stroke and animates layer
alpha 0.7 -> 0), scaling **1.0 -> 1.75** over 2.6s while fading to 0, second halo offset by **1.3s**
(`StartOffset(1300)`). Easing: shipped LinearOutSlowInEasing vs HTML `ease-out`; visually equivalent.

**Actively hearing speech:** driven by a 250ms tick; "hearing" is true while the last partial arrived
within 900ms.
- **178dp gold ring, 3dp AccentGold stroke**, plus a soft glow drawn as an 8dp stroke at 30% alpha,
  5dp outside the ring.
- Orb breathe speeds up to a **1.1s** cycle.
- Caption "HEARING YOU" in AccentGoldText 13sp SemiBold replaces the bottom hint.
- **Spec vs shipped:** the HTML's 250ms fade/scale-in entrance and 1.1s gold glow pulse
  (`goldpulse`: shadow 18px<->34px, scale 1<->1.045) did not ship; the ring appears/disappears
  instantly on the hearing flag (state swap via `key(hearing)`, which also restarts the breathe phase).

**Live transcript:** below the orb, last **18 words** (`AppConfig.TRANSCRIPT_TAIL_WORDS`), 15sp italic.
**Spec vs shipped:** spec styles the tail bold TextSecondary on a dim italic base; shipped renders the whole
tail uniformly in TextMuted italic (no bold emphasis span).

**Countdown overlay** (tap orb or silence timeout; extraction fires in parallel the moment capture stops):
- Full-screen scrim: **BgPrimary at 96% alpha** (cream, content ghosts through).
- "Ready for selfie" 20sp Bold BrandPrimary; digits **3 -> 2 -> 1 -> GO** at **800ms beats**
  (`COUNTDOWN_BEAT_MS`), 110sp ExtraBold **BrandGreen**; sub-line "extraction already running in the
  background" 14sp TextMuted.
- **Spec vs shipped:** HTML fades the overlay in over 300ms; shipped overlay appears instantly.

## 6. Screen-by-screen

**FLOW (founder change, shipped):** the Capture orb IS the home screen. Recent contacts moved behind a
hamburger drawer, Review and Send merged into one screen. Navigation is a `sealed interface Screen` in
`MainActivity.kt`; back always returns to Capture.

### Capture-as-Home (`CaptureScreen.kt`)
- Hamburger icon top-left (BrandGreen) opens a `ModalNavigationDrawer` (cream sheet): product name header,
  items **"People you met"** (-> PeopleScreen) and **"Your details"** (-> OnboardingScreen).
- Prompt: "Hi! I'm listening." + "Say your name, number, company and what we talked about."
- Orb front and center; transcript tail below; bottom hint "tap the orb to stop" swaps to gold
  "HEARING YOU" while speech lands. Mic permission requested here, not at app start.
- Tap orb (or STT finish) -> extraction kicks off async -> countdown overlay -> Review with the draft
  (6s extraction timeout falls back to a note-only draft).

### People you met (`PeopleScreen.kt`, behind the drawer)
- Back arrow + "People you met" title, live Convex-fed contact cards (avatar initial, name, role/company,
  "met 2h ago", gold enriched badge). Empty state: "Meet someone. Hand them your phone."
- **Spec vs shipped:** avatar is the name initial on BrandPrimary, not the selfie thumbnail (selfie thumbs
  land on the web dashboard; app thumbnail pending).

### Review, merged with Send (`ReviewScreen.kt`)
- Circular 96dp selfie at top, tap to retake (returns to Capture). **Spec vs shipped:** selfie is currently
  the bundled demo asset (`SendConfig.TEST_IMAGE_ASSET`); CameraX auto-capture is build-order step 6.
  No shimmer while extraction is in flight; extraction is awaited (with timeout) before navigating here.
- Editable fields: name, phone, company, role, note. The WhatsApp message bubble is an editable multiline
  field that live-retemplates from field edits until the user touches the message itself
  (`MessageTemplate.compose`).
- Single CTA **"Send on WhatsApp"**, gated on name + phone (`draft.readyToCompose`), loading spinner while
  sending. Fires the jid intent, saves to Convex (selfie upload + mutation, enrichment auto-scheduled),
  then Success.

### Success moment (`SuccessScreen.kt`)
- Full-screen cream; 120dp BrandPrimary circle spring-scales in (0.4 -> 1.0, MediumBouncy) with a gold
  checkmark; headline from the rotating `SUCCESS_QUOTES` list (advances each send); sub-line
  "Saved. Nudge in 48h."; auto-dismiss after **2.5s** or tap anywhere.
- **Spec vs shipped:** PLAN called for a Lottie burst "on the dark surface"; shipped a spring checkmark
  burst on cream (consistent with light-only theme, no Lottie dependency).

### Your details / Onboarding (`OnboardingScreen.kt`, screen 0)
- One white 24dp-radius card: "This is you." Fields: name, WhatsApp number, LinkedIn URL, Instagram URL,
  about. Save CTA. Shown once on first run (SharedPreferences via `OwnerStore`, sync first read, no
  start-screen flicker), reachable later from the drawer.
- **Spec vs shipped:** DataStore replaced by SharedPreferences (noted in PLAN); optional photo and
  "Enrich me" button not shipped; Instagram + about fields added beyond spec.

### Web dashboard (`web/src/App.tsx` + `theme.css`)
- Same cream + green palette. Header: title + subtitle, stat row (met today, enriched count).
- Live contact cards via Convex subscription: selfie image (initial fallback), name, role/company filling
  in live when enrichment lands, "met 2h ago", enrichment chip, gold follow-up countdown chip
  ("nudge in 47h" / "nudge due"), re-enrich action. Empty state mirrors the app copy.

## 7. Motion language

- **Spring physics over linear.** Button press release and the success burst use
  spring(MediumBouncy, MediumLow); tweens only for infinite loops (breathe, halos) and beat timers.
- **Exit faster than enter.** Overlays and state exits are instant or near-instant; entrances get the
  animation budget (halo expansion, spring scale-in).
- **GPU-only transforms.** All motion goes through `graphicsLayer` scale/alpha; no layout invalidation,
  no recomposition-driven animation.
- **Every animation has meaning:** breathe = the app is listening; gold = it hears YOU right now;
  faster breathe = urgency of live capture; expanding halos = open ears; countdown beats = selfie imminent;
  spring burst = the contact is locked in.

## 8. Accessibility

- **Contrast baked into tokens:** gold as text is #8A6A00 (**4.85:1** on cream, vs raw #D4AF37 which
  fails); TextMuted 60% black is **5.49:1** on cream; TextSecondary 75% black and TextPrimary comfortably
  exceed WCAG AA. Disabled CTA keeps a 1dp #8F8F8F outline so state is visible without color reliance.
- **Touch targets 44dp+:** 52dp CTA, 48dp Material icon buttons, 48dp list avatars, 150dp orb.
- **STT safety net:** every extracted field on Review is a plain editable text field (phone at minimum is
  a never-cut item), so recognition errors never block the flow.
- Content descriptions on icon buttons and the selfie image via string resources.

## Spec vs shipped summary

| Area | Spec | Shipped |
|---|---|---|
| Orb shadow | drop shadow + inner bottom shade | flat gradient, no shadows |
| Gold ring entrance | 250ms fade/scale-in + 1.1s glow pulse | instant show/hide, static double stroke |
| Transcript emphasis | tail bold TextSecondary on dim italic | whole tail TextMuted italic |
| Countdown overlay | 300ms fade-in | instant |
| Secondary button | dedicated component | not yet needed, not built |
| Review selfie | live CameraX countdown capture | bundled demo asset (step 6 pending) |
| Review shimmer | shimmer while extraction in flight | extraction awaited pre-navigation instead |
| Success | Lottie burst, dark surface | spring checkmark burst on cream |
| People avatars | selfie thumbnails | name-initial circles |
| Onboarding | DataStore, photo, "Enrich me" | SharedPreferences, +Instagram/about, no photo/enrich |
| BrandGreenHover | pressed variant | token defined, unused |

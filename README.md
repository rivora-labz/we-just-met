# We Just Met

Contact capture app where the other person needs nothing. You hold your phone, they speak their details, you take a selfie together. Thirty seconds later their contact is saved, enriched, and a composed WhatsApp message with the selfie is on their phone. They installed nothing, scanned nothing, signed up for nothing.

Built at the Collabute x TheBlock hackathon (Dubai, 2026-08-30) on the mandatory partner stack.

## The wedge

Every rival (HiHello, Blinq, Popl, Mobilo) needs both people in the system. This needs one. The selfie plus the "thing we said we would do" is what lets you restart the relationship three weeks later. Capture wins you the right to win follow-up, and follow-up is where the category dies.

## Partner stack (all three load-bearing, 25% of judging)

| Partner | Role in product |
|---|---|
| **Devin** (Cognition) | Builds the app. Every feature is a Devin CLI session driven from this repo. |
| **Convex** | The follow-up engine. Contacts DB, selfie storage, enrichment pipeline state, scheduled functions that fire the 48-hour follow-up nudge, plus a live web dashboard of people you met. |
| **Context.dev** | The enrichment layer. Given the spoken name + company, web API finds their public profile, role, company info. Browser actions handle the LinkedIn pre-filled search / connect flow through their gateway. |

## Architecture

- **KMP shared core** (`commonMain`): data models, extraction client, message templating, screen state. Android surface ships first.
- **Capture flow (on device)**: platform SpeechRecognizer -> cloud structured extraction into fixed JSON schema -> selfie via CameraX -> `ACTION_SEND` + `EXTRA_STREAM` + `jid` fires the WhatsApp intent with the image.
- **Convex backend**: `contacts` table, file storage for selfies, action that calls Context.dev to enrich, scheduled function for follow-up reminder, simple web dashboard (list of met people, enrichment status, follow-up timers).
- **Build order is back-to-front**: WhatsApp send seam first against hardcoded data, then upstream populates it. Working demo at every hour boundary.

## Hard cuts (decided, do not relitigate)

1. No LinkedIn Invitations API auto-connect. Own LinkedIn URL rides in the outgoing message; "Find on LinkedIn" deep-links a pre-filled search. Context.dev browser action is the stretch path only.
2. No on-device LLM. Platform STT + cloud extraction.
3. No hosted selfie page. `ACTION_SEND` puts the real image in the thread.

## Demo rule

The payoff renders on OUR screen (composed message one tap from send, dashboard updating live), never on a judge's phone.

## Secrets

All keys via environment / local.properties, never committed. See `env.example`.

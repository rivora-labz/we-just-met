import { v } from "convex/values";
import { action } from "./_generated/server";
import { DEFAULT_COUNTRY_CODE } from "./shared";

/**
 * Transcript -> structured contact (PLAN step 5).
 * PLAN deviation (noted in PLAN.md): EXTRACTION_API_KEY was never provisioned, so this is a
 * deterministic server-side extractor instead of a cloud LLM. Same fixed JSON schema, same
 * action seam; swapping the internals for an LLM later touches only this file. The editable
 * Review card remains the human net.
 */
export const run = action({
  args: { transcript: v.string() },
  handler: async (_ctx, { transcript }) => extractContact(transcript),
});

type Extracted = { name: string; phone: string; company: string; role: string; note: string };

const NUMBER_WORDS: Record<string, string> = {
  zero: "0", oh: "0", o: "0", nought: "0",
  one: "1", two: "2", three: "3", four: "4", five: "5",
  six: "6", seven: "7", eight: "8", nine: "9",
};
const REPEAT_WORDS: Record<string, number> = { double: 2, triple: 3 };
const PLUS_WORD = "plus";
const MIN_PHONE_DIGITS = 7;
/** Words that end a captured name/company span. */
const SPAN_STOPWORDS = new Set([
  "and", "my", "i", "we", "you", "the", "a", "at", "with", "from", "plus",
  "phone", "number", "is", "it's", "its", "so", "here", "talked", "talk",
]);

export function extractContact(transcript: string): Extracted {
  const text = transcript.trim();
  const phone = normalizePhone(findPhoneDigits(text));
  const name = matchSpan(text, [
    /\b(?:my name is|i am|i'm|this is)\s+([a-z][a-z'.-]*(?:\s+[a-z][a-z'.-]*)?)/i,
  ], 2);
  const roleCompany = text.match(
    /\bi\s+(?:run|lead|head|do|manage)\s+([a-z][a-z\s'&.-]{1,30}?)\s+at\s+([a-z0-9][\w\s'&.-]{1,40})/i,
  );
  const role = roleCompany ? trimSpan(roleCompany[1], 3) : "";
  const company = roleCompany
    ? trimSpan(roleCompany[2], 4)
    : matchSpan(text, [
        /\b(?:i work (?:at|for)|working at|work at|i'm with|company is)\s+([a-z0-9][\w\s'&.-]{1,40})/i,
      ], 4);
  const note = matchSpan(text, [
    /\b(?:talked about|talking about|talk about|discussed|spoke about)\s+(.{3,90}?)(?:[.!?]|$)/i,
  ], 12, false);

  return { name: titleCase(name), phone, company: titleCase(company), role, note };
}

/** Digits spoken as words ("nine seven one"), digit runs ("971 55..."), double/triple, plus. */
function findPhoneDigits(text: string): string {
  const tokens = text.toLowerCase().split(/[\s,-]+/);
  let best = "";
  let current = "";
  let pendingRepeat = 1;
  const flush = () => {
    if (current.replace("+", "").length > best.replace("+", "").length) best = current;
    current = "";
    pendingRepeat = 1;
  };
  for (const raw of tokens) {
    const token = raw.replace(/[^\w+]/g, "");
    if (token === PLUS_WORD || token === "+") {
      if (current === "") current = "+";
      continue;
    }
    if (REPEAT_WORDS[token] !== undefined) {
      pendingRepeat = REPEAT_WORDS[token];
      continue;
    }
    const asWord = NUMBER_WORDS[token];
    const digits = asWord ?? (/^\+?\d+$/.test(token) ? token.replace("+", "") : null);
    if (digits !== null) {
      if (token.startsWith("+") && current === "") current = "+";
      current += digits.repeat(pendingRepeat);
      pendingRepeat = 1;
    } else if (current !== "") {
      flush();
    }
  }
  flush();
  return best.replace("+", "").length >= MIN_PHONE_DIGITS ? best : "";
}

function normalizePhone(raw: string): string {
  if (!raw) return "";
  const defaultDigits = DEFAULT_COUNTRY_CODE.slice(1);
  if (raw.startsWith("+")) return raw;
  let digits = raw;
  if (digits.startsWith("00")) return `+${digits.slice(2)}`;
  if (digits.startsWith(defaultDigits) && digits.length >= 11) return `+${digits}`;
  if (digits.startsWith("0")) digits = digits.slice(1);
  return `${DEFAULT_COUNTRY_CODE}${digits}`;
}

function matchSpan(text: string, patterns: RegExp[], maxWords: number, stopAtStopword = true): string {
  for (const pattern of patterns) {
    const match = text.match(pattern);
    if (match?.[1]) return trimSpan(match[1], maxWords, stopAtStopword);
  }
  return "";
}

function trimSpan(span: string, maxWords: number, stopAtStopword = true): string {
  const words: string[] = [];
  for (const word of span.trim().split(/\s+/)) {
    const clean = word.replace(/[.,!?]+$/, "");
    if (stopAtStopword && SPAN_STOPWORDS.has(clean.toLowerCase())) break;
    words.push(clean);
    if (/[.,!?]$/.test(word) || words.length >= maxWords) break;
  }
  return words.join(" ");
}

function titleCase(span: string): string {
  return span
    .split(/\s+/)
    .map((w) => (w ? w[0].toUpperCase() + w.slice(1) : w))
    .join(" ")
    .trim();
}

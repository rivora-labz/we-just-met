import { useEffect, useState } from "react";
import { useQuery } from "convex/react";
import { api } from "../convex/_generated/api";
import { ENRICHMENT, PRODUCT_NAME } from "../convex/shared";

const MINUTE_MS = 60 * 1000;
const HOUR_MS = 60 * MINUTE_MS;
const DAY_MS = 24 * HOUR_MS;

const ENRICHMENT_CHIP: Record<string, { className: string; label: string }> = {
  [ENRICHMENT.pending]: { className: "chip chip-pending", label: "enriching…" },
  [ENRICHMENT.done]: { className: "chip chip-done", label: "enriched" },
  [ENRICHMENT.failed]: { className: "chip chip-failed", label: "enrich failed" },
};

function metAgo(metAt: number, now: number): string {
  const delta = Math.max(0, now - metAt);
  if (delta < HOUR_MS) return `met ${Math.max(1, Math.round(delta / MINUTE_MS))}m ago`;
  if (delta < DAY_MS) return `met ${Math.round(delta / HOUR_MS)}h ago`;
  return `met ${Math.round(delta / DAY_MS)}d ago`;
}

function followUpChip(followUpAt: number, now: number): string {
  const delta = followUpAt - now;
  if (delta <= 0) return "nudge due";
  if (delta < HOUR_MS) return `nudge in ${Math.max(1, Math.round(delta / MINUTE_MS))}m`;
  return `nudge in ${Math.round(delta / HOUR_MS)}h`;
}

function startOfToday(now: number): number {
  const d = new Date(now);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

export default function App() {
  const contacts = useQuery(api.contacts.list);
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), MINUTE_MS);
    return () => clearInterval(timer);
  }, []);

  const metToday = contacts?.filter((c) => c.metAt >= startOfToday(now)).length ?? 0;
  const enriched = contacts?.filter((c) => c.enrichment === ENRICHMENT.done).length ?? 0;

  return (
    <div className="page">
      <header className="header">
        <div>
          <h1 className="title">{PRODUCT_NAME}</h1>
          <p className="subtitle">People you met, enriched and queued for follow-up</p>
        </div>
        <div className="stats">
          <div className="stat">
            <div className="stat-value">{metToday}</div>
            <div className="stat-label">met today</div>
          </div>
          <div className="stat">
            <div className="stat-value">{enriched}</div>
            <div className="stat-label">enriched</div>
          </div>
        </div>
      </header>

      {contacts && contacts.length === 0 && (
        <p className="empty">Meet someone. Hand them your phone.</p>
      )}

      <div className="grid">
        {contacts?.map((c) => (
          <article className="card" key={c._id}>
            {c.selfieUrl ? (
              <img className="avatar" src={c.selfieUrl} alt={c.name} />
            ) : (
              <div className="avatar-fallback">{c.name.charAt(0)}</div>
            )}
            <div className="card-body">
              <h2 className="card-name">{c.name}</h2>
              <p className="card-meta">
                {[c.role, c.company].filter(Boolean).join(", ") || c.phone}
              </p>
              {c.note && <p className="card-note">{c.note}</p>}
              <div className="card-footer">
                <span className="chip chip-met">{metAgo(c.metAt, now)}</span>
                <span className="chip chip-followup">{followUpChip(c.followUpAt, now)}</span>
                <span className={ENRICHMENT_CHIP[c.enrichment].className}>
                  {ENRICHMENT_CHIP[c.enrichment].label}
                </span>
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

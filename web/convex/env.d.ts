/** Convex actions expose deployment environment variables via process.env at runtime. */
declare const process: { env: Record<string, string | undefined> };

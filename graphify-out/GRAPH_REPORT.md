# Graph Report - /Users/narayandhingra/Documents/Claude/Projects/We-Just-Met/app  (2026-08-30)

## Corpus Check
- 6 files · ~3,281 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 16 nodes · 10 edges · 6 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]

## God Nodes (most connected - your core abstractions)
1. `WhatsAppSender` - 3 edges
2. `MainActivity` - 2 edges
3. `SendConfig` - 2 edges
4. `Tokens` - 1 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.4
Nodes (1): MainActivity

### Community 1 - "Community 1"
Cohesion: 0.5
Nodes (1): WhatsAppSender

### Community 2 - "Community 2"
Cohesion: 0.67
Nodes (1): SendConfig

### Community 3 - "Community 3"
Cohesion: 1.0
Nodes (1): Tokens

### Community 4 - "Community 4"
Cohesion: 1.0
Nodes (0): 

### Community 5 - "Community 5"
Cohesion: 1.0
Nodes (0): 

## Knowledge Gaps
- **1 isolated node(s):** `Tokens`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 3`** (2 nodes): `Tokens`, `Theme.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 4`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 5`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `Tokens` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
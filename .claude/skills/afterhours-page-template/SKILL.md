---
name: afterhours-page-template
description: Use when adding a new project subpage to the afterhours GitHub Pages site under docs/, or when modifying an existing project subpage. Ensures new pages match the shared style.css design system, follow the established nav/hero/section structure, and reuse existing components instead of introducing new CSS.
---

# afterhours Page Template

**DRAFT** — extracted from the three existing project subpages (`docs/01_dark_detector`, `docs/02_koog`, `docs/03_kotlin_mcp`). Not yet pressure-tested with subagents.

## Overview

The afterhours site is a hand-written static site under `docs/` served by GitHub Pages. Every project subpage shares one stylesheet (`docs/style.css`) and follows the same skeleton. New pages must reuse the existing component vocabulary, not introduce new CSS.

**Core principle:** the design system lives in `docs/style.css`. If a new page needs a component that isn't already in there, stop and confirm with the user before adding CSS — a matching component often exists under a different name.

## When to Use

- Adding a new numbered project subpage (`docs/NN_slug/index.html`)
- Editing an existing project subpage
- Wiring a new project into `docs/index.html` (the landing page grid)

## When NOT to Use

- Editing `docs/style.css` itself (component-level design work — use `frontend-design` instead)
- Editing `docs/index.html` structure beyond adding a `.project-card` (that's a landing-page change)
- Anything outside `docs/` (source code lives at the repo root)

## Required Page Skeleton

Every project subpage MUST have these elements in this order. Deviations without explicit reason are bugs.

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{Project Name} — afterhours</title>
  <link rel="stylesheet" href="../style.css">
</head>
<body>

  <nav>
    <div class="brand"><a href="../">afterhours</a> / <span>{NN}</span> {slug}</div>
    <ul>
      <li><a href="#overview">Overview</a></li>
      <!-- one <li> per section, in page order -->
      <li><a href="https://github.com/wtopolski/afterhours/tree/main/{NN}_{slug}">source</a></li>
    </ul>
  </nav>

  <header class="hero">
    <div class="tag">afterhours / project {NN}</div>
    <h1>{Project Name}</h1>
    <p>{One-sentence description. Same sentence used in landing-page card.}</p>
  </header>

  <!-- sections here — see Section Order -->

  <footer>
    <span>afterhours</span>
    <a href="https://github.com/wtopolski/afterhours">github.com/wtopolski/afterhours</a>
  </footer>

</body>
</html>
```

**Fixed invariants:**
- `lang="en"` — all pages are English
- Title format: `{Project Name} — afterhours` (em-dash, not hyphen)
- Stylesheet is always `../style.css` (relative from `docs/NN_slug/`)
- Nav brand uses ` / ` separators; the `NN` goes inside `<span>`
- Hero tag format: `afterhours / project NN` (lowercase, no leading zero shown as text — write `01`, `02`, `03`)
- Every section that appears in nav gets `id="…"`; every `id` in nav must exist
- Footer is byte-identical across pages

## Section Order

Two established patterns. Pick based on project type.

**Software project** (matches commit `c854a85` — projects 02, 03):
```
Overview → Architecture → Features → Setup
```
(Features section is sometimes named `Tools`, `Samples`, etc. — content-appropriate.)

**Hardware project** (project 01):
```
Overview → How it works → Schematic → BOM → PCB → Build
```

The first section is always `Overview`, containing `<h2>What this is</h2>`, a short paragraph, and a `.specs` grid of 3–5 headline facts.

## Component Vocabulary

Use these classes from `style.css`. Do not invent new ones for content that fits an existing pattern.

| Component | Class | Use for |
|---|---|---|
| Headline facts (3–5) | `.specs` + `.spec` (`.label`, `.value`, `.unit`) | Overview vitals: voltage, language, framework, count |
| Component list | `.bom-table` | Bill of materials, dependency tables (columns: Ref / Component / Value / Package) |
| Calculation | `.formula-block` (with `.result`) | Ohm's-law style derivations |
| Binary state | `.operation-grid` + `.op-card` (`.indicator.on`/`.off`, `.state`) | Two-state behavior (dark/bright, on/off) |
| Photo gallery | `.gallery` | Real photos — cropped `object-fit: cover` |
| Render gallery | `.gallery.renders` | 3D/2D renders — contained on elevated bg |
| Single diagram | `.schematic-img` | Schematics on light background |
| Bulleted architecture | `.arch-list` (use `<code>` inside `<li>` for identifiers) | Module/layer descriptions |
| Feature/sample tiles | `.sample-grid` + `.sample-card` (`.concepts` for tag chips) | Feature list with tags |
| Code block | `<pre>` with `.keyword` / `.comment` / `.string` spans | Setup snippets, examples |

**HTML entities:** the pages use `&mdash;`, `&Omega;`, `&beta;`, `&times;`, `&minus;`, `&rarr;` — prefer entities over Unicode for readability of source.

## Image Hosting

- Photos and renders: hosted on `raw.githubusercontent.com/wtopolski/afterhours/main/{NN}_{slug}/…` — NOT copied into `docs/`
- Small icons/logos (like `logo.png`): committed under `docs/NN_slug/` so they load without network dependency on GitHub raw
- Every `<img>` MUST have `alt` text

**Known gap:** none of the existing pages set `width`/`height` or `loading="lazy"` on images. If adding a page with many images, consider adding both to reduce layout shift — flag to the user before doing it retroactively to old pages.

## Landing Page Wiring

When adding a new project, also add a `<div class="project-card">` block to `docs/index.html` inside `.project-grid`. Structure:

```html
<div class="project-card">
  <a href="{NN}_{slug}/">
    <img class="thumb" src="{full URL or local path}" alt="{Project Name}">
    <div class="info">
      <div class="number">{NN}</div>
      <h3>{Project Name}</h3>
      <div class="desc">{Same one-sentence description as page hero}</div>
      <div class="meta">
        <span>{tag1}</span>
        <span>{tag2}</span>
        <span>{tag3}</span>
      </div>
    </div>
  </a>
</div>
```

Description text should match the hero `<p>` verbatim (or a lightly trimmed version) — the landing card and the page hero are the same promise.

## Quick Checklist

Before shipping a new page:

- [ ] File is at `docs/NN_slug/index.html` where `NN` is zero-padded
- [ ] `<title>` ends with `— afterhours` (em-dash)
- [ ] Nav links match section `id`s exactly
- [ ] `source` link points to `github.com/wtopolski/afterhours/tree/main/NN_slug`
- [ ] Hero tag reads `afterhours / project NN`
- [ ] First section is `Overview` with `<h2>What this is</h2>` and a `.specs` grid
- [ ] Section order matches software or hardware pattern (see above)
- [ ] All images have `alt` text
- [ ] No new CSS classes introduced (or if introduced, added to `style.css` with user's confirmation)
- [ ] Landing page `docs/index.html` has a matching `.project-card`
- [ ] Footer is byte-identical to other pages

## Common Mistakes

| Mistake | Fix |
|---|---|
| Copying HTML with wrong stylesheet path | Path is always `../style.css` from `docs/NN_slug/` |
| Using a hyphen instead of em-dash in title | Use `—` (or `&mdash;` in text) |
| Introducing Tailwind/inline styles | Everything goes through `style.css` classes |
| Adding a section but forgetting the nav `<li>` | Nav must list every visible section in order |
| Landing card description ≠ hero description | Keep them in sync — they are the same promise |
| Hosting large photos inside `docs/` | Photos live at repo root under `NN_slug/`, served via `raw.githubusercontent.com` |

## Draft Notes (remove after testing)

This skill has not been through the writing-skills RED-GREEN-REFACTOR cycle. Before promoting from draft:

1. Baseline: give a subagent "add a fourth project page to the afterhours repo" without this skill loaded; observe what conventions they miss (likely: em-dash in title, section order, image hosting location, syncing landing card copy).
2. Load skill and re-run — verify compliance.
3. Add a rationalization table for whichever guidance the agent tries to negotiate around.

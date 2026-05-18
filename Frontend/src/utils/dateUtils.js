/**
 * fmtDate — safe date formatter used across all pages.
 *
 * Handles:
 *  - null / undefined  → returns fallback (default "N/A")
 *  - "YYYY-MM-DD"      → "DD/MM/YYYY"
 *  - ISO datetime      → strips time component, then "DD/MM/YYYY"
 *    e.g. "2024-03-15T10:30:00" → "15/03/2024"
 *
 * @param {string|null|undefined} value
 * @param {string} fallback  shown when value is absent (default "N/A")
 * @returns {string}
 */
export function fmtDate(value, fallback = "N/A") {
  if (!value) return fallback;

  // Strip time component if present (LocalDateTime ISO strings)
  const datePart = String(value).split("T")[0];

  const segments = datePart.split("-");
  if (segments.length !== 3) return fallback;

  const [year, month, day] = segments;
  if (!year || !month || !day) return fallback;

  return `${day}/${month}/${year}`;
}

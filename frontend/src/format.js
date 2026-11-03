// The backend sends UTC timestamps without a "Z" suffix, so add it before parsing.
// Otherwise the browser treats them as local time and shows the wrong hour.
export function formatDate(value) {
  if (!value) return "";
  const iso = /Z|[+-]\d\d:?\d\d$/.test(value) ? value : value + "Z";
  return new Date(iso).toLocaleString();
}

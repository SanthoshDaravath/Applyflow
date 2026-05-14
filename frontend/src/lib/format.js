export const currency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(value || 0);

export const dateTime = (value) => new Intl.DateTimeFormat('en-US', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));
export const dateOnly = (value) => new Intl.DateTimeFormat('en-US', { dateStyle: 'medium' }).format(new Date(value));

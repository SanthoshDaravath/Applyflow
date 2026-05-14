import React from 'react';

export default function PageHeader({ kicker, title, description, actions }) {
  return (
    <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div>
        {kicker && <div className="text-xs uppercase tracking-[0.28em] text-cyan-200/60">{kicker}</div>}
        <h1 className="mt-2 text-3xl font-semibold text-white sm:text-4xl">{title}</h1>
        {description && <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-400">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
    </div>
  );
}

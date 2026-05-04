// primitives.jsx — shared visual atoms for Autografr
// Loaded as Babel script. Exposes globals via Object.assign(window, ...).

const { useState, useEffect, useRef, useMemo } = React;

// ─── Wordmark ───────────────────────────────────────────────
function Wordmark({ size = 28, color = 'var(--ink)', flourishColor = 'var(--signature)', tight = false }) {
  return (
    <span className="wordmark" style={{ fontSize: size, color, lineHeight: 1, letterSpacing: tight ? '-0.05em' : '-0.04em', display: 'inline-flex', alignItems: 'baseline' }}>
      <span>Autograf</span>
      <span className="flourish" style={{ color: flourishColor, fontSize: size * 1.25, marginLeft: -size * 0.06 }}>r</span>
    </span>
  );
}

// ─── Verification Seal (circular stamp w/ QR fragment) ───────
function Seal({ size = 88, color = 'var(--foil)', text = 'AUTOGRAFR · CERTIFIED · ', edition = '042/365', date = 'V·26', spin = false }) {
  const r = size / 2;
  const radius = r - 7;
  const id = useMemo(() => 'seal-' + Math.random().toString(36).slice(2, 8), []);
  const chars = (text + text).split('');
  const step = 360 / chars.length;
  return (
    <div style={{ position: 'relative', width: size, height: size, color, animation: spin ? 'spin 28s linear infinite' : 'none' }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ display: 'block', overflow: 'visible' }}>
        <defs>
          <path id={id} d={`M ${r},${r} m -${radius},0 a ${radius},${radius} 0 1,1 ${radius * 2},0 a ${radius},${radius} 0 1,1 -${radius * 2},0`} fill="none" />
        </defs>
        <circle cx={r} cy={r} r={r - 1} fill="none" stroke="currentColor" strokeWidth="1" opacity="0.6" />
        <circle cx={r} cy={r} r={r - 5} fill="none" stroke="currentColor" strokeWidth="0.6" opacity="0.4" />
        <text fontFamily="Inter" fontSize={Math.max(7, size * 0.085)} fontWeight="600" letterSpacing="2.4" fill="currentColor">
          <textPath href={`#${id}`} startOffset="0">{text + text}</textPath>
        </text>
        {/* center mark */}
        <g transform={`translate(${r}, ${r})`}>
          <circle r={size * 0.18} fill="none" stroke="currentColor" strokeWidth="1" />
          <text textAnchor="middle" fontFamily="Fraunces" fontWeight="700" fontSize={size * 0.18} dy={size * 0.05} fill="currentColor" style={{ fontVariationSettings: "'opsz' 144" }}>A</text>
          <text textAnchor="middle" fontFamily="JetBrains Mono" fontSize={size * 0.085} dy={size * 0.18} fill="currentColor" opacity="0.85">{date}</text>
        </g>
      </svg>
    </div>
  );
}

// ─── Stylized QR (designed, not real) ────────────────────────
function QRMark({ size = 120, color = 'var(--ink)', bg = 'var(--paper)', seed = 7, withCorners = true }) {
  // deterministic pseudo-random pattern from seed
  const grid = 21;
  const cells = useMemo(() => {
    const out = [];
    let s = seed * 9301 + 49297;
    for (let y = 0; y < grid; y++) {
      for (let x = 0; x < grid; x++) {
        s = (s * 9301 + 49297) % 233280;
        const v = (s / 233280) > 0.45;
        out.push({ x, y, v });
      }
    }
    return out;
  }, [seed]);
  const cell = size / grid;
  const isFinder = (x, y) =>
    (x < 7 && y < 7) || (x >= grid - 7 && y < 7) || (x < 7 && y >= grid - 7);
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ display: 'block' }}>
      <rect width={size} height={size} fill={bg} />
      {cells.map((c, i) => {
        if (isFinder(c.x, c.y)) return null;
        if (!c.v) return null;
        return <rect key={i} x={c.x * cell + 0.5} y={c.y * cell + 0.5} width={cell - 1} height={cell - 1} fill={color} rx={cell * 0.15} />;
      })}
      {withCorners && [
        [0, 0], [grid - 7, 0], [0, grid - 7],
      ].map(([x, y], i) => (
        <g key={i}>
          <rect x={x * cell} y={y * cell} width={cell * 7} height={cell * 7} fill="none" stroke={color} strokeWidth={cell} rx={cell * 1.4} />
          <rect x={x * cell + cell * 2} y={y * cell + cell * 2} width={cell * 3} height={cell * 3} fill={color} rx={cell * 0.6} />
        </g>
      ))}
    </svg>
  );
}

// ─── Signature SVG (auto-animated) ───────────────────────────
function SignatureMark({ width = 240, height = 90, color = 'var(--signature)', strokeWidth = 3.5, animate = true, variant = 'curl' }) {
  const paths = {
    curl: "M8 60 C 32 20, 58 20, 70 50 S 95 86, 118 50 C 132 28, 158 28, 168 50 C 178 70, 200 64, 215 42 L 232 32 M 70 70 L 196 70",
    loop: "M10 58 C 30 22, 60 22, 70 56 C 78 82, 110 80, 120 50 C 130 22, 158 22, 168 50 C 174 70, 200 70, 220 50 L 232 36",
    fast: "M6 56 L 40 28 L 60 64 L 88 30 L 110 64 L 140 28 L 168 64 L 200 28 L 230 60",
  };
  const d = paths[variant] || paths.curl;
  return (
    <svg width={width} height={height} viewBox="0 0 240 90" style={{ display: 'block', overflow: 'visible' }}>
      <path
        d={d}
        fill="none"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
        className={animate ? 'signature-stroke' : ''}
      />
    </svg>
  );
}

// ─── Tag / Badge ─────────────────────────────────────────────
function Tag({ children, tone = 'ink', size = 'sm' }) {
  const tones = {
    ink:    { bg: 'rgba(14,13,11,0.08)', fg: 'var(--ink)', bd: 'rgba(14,13,11,0.12)' },
    paper:  { bg: 'rgba(242,237,226,0.08)', fg: 'var(--paper)', bd: 'rgba(242,237,226,0.16)' },
    foil:   { bg: 'rgba(201,162,76,0.16)', fg: '#8a6d2c', bd: 'rgba(201,162,76,0.4)' },
    foilDark: { bg: 'rgba(201,162,76,0.18)', fg: 'var(--foil-2)', bd: 'rgba(201,162,76,0.45)' },
    red:    { bg: 'rgba(179,58,42,0.12)', fg: 'var(--signature)', bd: 'rgba(179,58,42,0.32)' },
    green:  { bg: 'rgba(26,75,58,0.12)', fg: 'var(--stamp)', bd: 'rgba(26,75,58,0.32)' },
  };
  const t = tones[tone] || tones.ink;
  const padding = size === 'lg' ? '6px 12px' : '3px 8px';
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding, borderRadius: 999,
      background: t.bg, color: t.fg, border: `1px solid ${t.bd}`,
      fontFamily: 'Inter, sans-serif', fontWeight: 600, fontSize: size === 'lg' ? 12 : 10.5,
      letterSpacing: '0.06em', textTransform: 'uppercase', whiteSpace: 'nowrap',
    }}>
      {children}
    </span>
  );
}

// ─── Avatar (placeholder portraits) ──────────────────────────
function Avatar({ name = 'A', size = 40, hue = 200, ring = false, src = null }) {
  const initials = name.split(' ').slice(0, 2).map(p => p[0]).join('').toUpperCase();
  const gradient = `linear-gradient(135deg, oklch(0.62 0.12 ${hue}), oklch(0.42 0.11 ${(hue + 40) % 360}))`;
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: src ? `${gradient}` : gradient,
      backgroundImage: src ? `url(${src}), ${gradient}` : gradient,
      backgroundSize: 'cover', backgroundPosition: 'center',
      color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: 'Fraunces, serif', fontWeight: 600, fontSize: size * 0.42,
      boxShadow: ring ? '0 0 0 2px var(--paper), 0 0 0 3.5px var(--foil)' : 'none',
      flexShrink: 0,
      position: 'relative',
      overflow: 'hidden',
    }}>
      <span style={{ position: 'relative', zIndex: 1, mixBlendMode: 'screen', opacity: 0.95 }}>{initials}</span>
    </div>
  );
}

// ─── Photo placeholder ───────────────────────────────────────
function PhotoPlaceholder({ name = 'A', hue = 200, height = 220, children, vignette = true, scene = 'studio' }) {
  // Generate a stylized portrait-like backdrop
  const scenes = {
    studio: `radial-gradient(at 30% 20%, oklch(0.78 0.08 ${hue}) 0%, oklch(0.4 0.1 ${hue}) 60%, oklch(0.18 0.08 ${(hue + 20) % 360}) 100%)`,
    stage:  `linear-gradient(180deg, oklch(0.18 0.12 ${hue}) 0%, oklch(0.3 0.18 ${(hue + 30) % 360}) 50%, oklch(0.14 0.1 ${hue}) 100%)`,
    sunset: `linear-gradient(165deg, oklch(0.78 0.16 ${hue}) 0%, oklch(0.62 0.18 ${(hue + 30) % 360}) 50%, oklch(0.32 0.14 ${(hue + 60) % 360}) 100%)`,
    cool:   `linear-gradient(170deg, oklch(0.72 0.1 ${hue}) 0%, oklch(0.42 0.12 ${(hue + 200) % 360}) 100%)`,
  };
  return (
    <div style={{
      position: 'relative', width: '100%', height,
      background: scenes[scene] || scenes.studio,
      overflow: 'hidden', display: 'flex', alignItems: 'flex-end', justifyContent: 'center',
    }}>
      {/* abstract head silhouette */}
      <svg width="100%" height="100%" viewBox="0 0 200 240" preserveAspectRatio="xMidYMid slice" style={{ position: 'absolute', inset: 0 }}>
        <defs>
          <radialGradient id={`pp-light-${hue}`} cx="40%" cy="20%" r="60%">
            <stop offset="0%" stopColor="rgba(255,255,255,0.5)" />
            <stop offset="100%" stopColor="rgba(255,255,255,0)" />
          </radialGradient>
        </defs>
        {/* shoulders */}
        <path d="M -10,260 C 20,180 60,160 100,160 C 140,160 180,180 210,260 Z" fill="rgba(0,0,0,0.35)" />
        {/* head */}
        <ellipse cx="100" cy="115" rx="36" ry="44" fill="rgba(0,0,0,0.42)" />
        <rect x="0" y="0" width="200" height="240" fill={`url(#pp-light-${hue})`} />
      </svg>
      {vignette && <div style={{ position: 'absolute', inset: 0, background: 'radial-gradient(ellipse at 50% 40%, transparent 50%, rgba(0,0,0,0.45) 100%)' }} />}
      {/* film grain */}
      <div className="grain" />
      {/* initial monogram, soft */}
      <div style={{
        position: 'absolute', top: 12, left: 14,
        color: 'rgba(255,255,255,0.45)', fontFamily: 'Fraunces, serif', fontWeight: 700,
        fontSize: 12, letterSpacing: '0.18em', textTransform: 'uppercase',
      }}>{name}</div>
      {children}
    </div>
  );
}

// ─── Status bar (Android, themed) ────────────────────────────
function StatusBar({ dark = false, time = '9:41', color }) {
  const c = color || (dark ? 'var(--paper)' : 'var(--ink)');
  return (
    <div style={{
      height: 36, display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 18px', position: 'relative',
      fontFamily: 'Inter, sans-serif', flexShrink: 0,
    }}>
      <span style={{ fontSize: 13, fontWeight: 600, color: c, letterSpacing: '-0.01em' }}>{time}</span>
      <div style={{ position: 'absolute', left: '50%', top: 8, transform: 'translateX(-50%)', width: 22, height: 22, borderRadius: 999, background: '#0a0a0a' }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, color: c }}>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M8 12.5L1 6.5a9.6 9.6 0 0114 0L8 12.5z" fill="currentColor"/></svg>
        <svg width="14" height="14" viewBox="0 0 16 16"><path d="M14 14V2L2 14h12z" fill="currentColor"/></svg>
        <svg width="20" height="12" viewBox="0 0 24 12"><rect x="0.5" y="0.5" width="20" height="11" rx="2.5" fill="none" stroke="currentColor"/><rect x="2" y="2" width="16" height="8" rx="1.4" fill="currentColor"/><rect x="21" y="4" width="2" height="4" rx="1" fill="currentColor" opacity="0.6"/></svg>
      </div>
    </div>
  );
}

// ─── Bottom Tab Bar ─────────────────────────────────────────
function TabBar({ active = 'home', onChange = () => {}, role = 'fan', dark = false }) {
  const fanTabs = [
    { id: 'home',       label: 'Drops',     icon: ICONS.home },
    { id: 'capture',    label: 'Capture',   icon: ICONS.scan, big: true },
    { id: 'collection', label: 'Collection',icon: ICONS.collection },
    { id: 'profile',    label: 'You',       icon: ICONS.user },
  ];
  const celebTabs = [
    { id: 'home',    label: 'Today',    icon: ICONS.home },
    { id: 'sign',    label: 'Sign',     icon: ICONS.pen, big: true },
    { id: 'queue',   label: 'Queue',    icon: ICONS.queue },
    { id: 'profile', label: 'Studio',   icon: ICONS.user },
  ];
  const tabs = role === 'celeb' ? celebTabs : fanTabs;
  const fg = dark ? 'var(--paper)' : 'var(--ink)';
  const muted = dark ? 'rgba(242,237,226,0.45)' : 'rgba(14,13,11,0.45)';
  const bg = dark ? 'var(--ink-2)' : 'var(--paper)';
  const border = dark ? 'var(--line-dark)' : 'var(--line)';
  return (
    <div style={{
      flexShrink: 0,
      background: bg,
      borderTop: `1px solid ${border}`,
      padding: '8px 14px 12px',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        {tabs.map(t => {
          const isActive = t.id === active;
          if (t.big) {
            return (
              <button key={t.id} onClick={() => onChange(t.id)} style={{
                width: 56, height: 56, borderRadius: 999,
                background: 'var(--ink)', color: 'var(--paper)',
                border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
                cursor: 'pointer', marginTop: -22, position: 'relative',
                boxShadow: '0 8px 24px -6px rgba(14,13,11,0.55), inset 0 1px 0 rgba(255,255,255,0.08)',
              }}>
                <div style={{ position: 'absolute', inset: -4, borderRadius: 999, border: '1px solid var(--foil)', opacity: 0.6 }} />
                <span style={{ width: 22, height: 22 }}>{t.icon}</span>
              </button>
            );
          }
          return (
            <button key={t.id} onClick={() => onChange(t.id)} style={{
              flex: 1, background: 'transparent', border: 'none', cursor: 'pointer',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              padding: '8px 0', color: isActive ? fg : muted,
            }}>
              <span style={{ width: 20, height: 20 }}>{t.icon}</span>
              <span style={{ fontSize: 10, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase' }}>{t.label}</span>
              <span style={{ width: 4, height: 4, borderRadius: 999, background: isActive ? 'var(--signature)' : 'transparent' }} />
            </button>
          );
        })}
      </div>
    </div>
  );
}

// ─── Icons ──────────────────────────────────────────────────
const ICONS = {
  home: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M3 11.5L12 4l9 7.5"/><path d="M5 10v10h14V10"/></svg>,
  scan: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M4 8V5a1 1 0 011-1h3"/><path d="M20 8V5a1 1 0 00-1-1h-3"/><path d="M4 16v3a1 1 0 001 1h3"/><path d="M20 16v3a1 1 0 01-1 1h-3"/><path d="M3 12h18"/></svg>,
  collection: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><rect x="4" y="4" width="7" height="7" rx="1.2"/><rect x="13" y="4" width="7" height="7" rx="1.2"/><rect x="4" y="13" width="7" height="7" rx="1.2"/><rect x="13" y="13" width="7" height="7" rx="1.2"/></svg>,
  user: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-7 8-7s8 3 8 7"/></svg>,
  pen: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M14 4l6 6L8 22H2v-6L14 4z"/><path d="M13 5l6 6"/></svg>,
  queue: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M3 6h18"/><path d="M3 12h12"/><path d="M3 18h18"/><circle cx="20" cy="12" r="2"/></svg>,
  back: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M15 6l-6 6 6 6"/></svg>,
  close: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M6 6l12 12M6 18L18 6"/></svg>,
  more: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><circle cx="5" cy="12" r="1.6"/><circle cx="12" cy="12" r="1.6"/><circle cx="19" cy="12" r="1.6"/></svg>,
  search: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" style={{ width: '100%', height: '100%' }}><circle cx="11" cy="11" r="6"/><path d="M16 16l4 4"/></svg>,
  bell: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M6 16V11a6 6 0 1112 0v5l1.5 2H4.5L6 16z"/><path d="M10 20a2 2 0 004 0"/></svg>,
  check: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><path d="M5 12l4 4 10-10"/></svg>,
  bolt: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><path d="M13 2L4 14h6l-1 8 9-12h-6l1-8z"/></svg>,
  share: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><circle cx="6" cy="12" r="2.4"/><circle cx="18" cy="6" r="2.4"/><circle cx="18" cy="18" r="2.4"/><path d="M8 11l8-4M8 13l8 4"/></svg>,
  sparkle: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><path d="M12 3l1.4 5.6L19 10l-5.6 1.4L12 17l-1.4-5.6L5 10l5.6-1.4L12 3z"/></svg>,
  verified: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><path d="M12 2l2.4 1.6 2.9-.5 1 2.7 2.7 1-.5 2.9L22 12l-1.5 2.3.5 2.9-2.7 1-1 2.7-2.9-.5L12 22l-2.4-1.6-2.9.5-1-2.7-2.7-1 .5-2.9L2 12l1.5-2.3-.5-2.9 2.7-1 1-2.7 2.9.5L12 2z"/><path d="M8.5 12.2l2.6 2.6 4.4-5" stroke="var(--paper)" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"/></svg>,
  qr: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" style={{ width: '100%', height: '100%' }}><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="3" height="3"/><rect x="18" y="14" width="3" height="3"/><rect x="14" y="18" width="3" height="3"/><rect x="18" y="18" width="3" height="3"/></svg>,
  lock: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" style={{ width: '100%', height: '100%' }}><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V8a4 4 0 018 0v3"/></svg>,
  flame: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><path d="M12 2c1 4 5 5 5 10a5 5 0 11-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z"/></svg>,
  plus: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" style={{ width: '100%', height: '100%' }}><path d="M12 5v14M5 12h14"/></svg>,
  filter: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" style={{ width: '100%', height: '100%' }}><path d="M3 5h18M6 12h12M10 19h4"/></svg>,
  heart: <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: '100%', height: '100%' }}><path d="M12 21s-7-4.5-9.5-9.5C.5 7 4 4 7 4c2 0 3.5 1 5 3 1.5-2 3-3 5-3 3 0 6.5 3 4.5 7.5C19 16.5 12 21 12 21z"/></svg>,
};

// ─── App-bar ─────────────────────────────────────────────────
function AppBar({ title, subtitle, dark = false, onBack, right, transparent = false, eyebrow }) {
  const fg = dark ? 'var(--paper)' : 'var(--ink)';
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '8px 16px 12px',
      background: transparent ? 'transparent' : 'inherit',
      flexShrink: 0,
    }}>
      {onBack && (
        <button onClick={onBack} style={{
          width: 38, height: 38, borderRadius: 999,
          background: 'rgba(14,13,11,0.06)', border: '1px solid var(--line)',
          color: fg, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0,
        }}>
          <span style={{ width: 18, height: 18 }}>{ICONS.back}</span>
        </button>
      )}
      <div style={{ flex: 1, minWidth: 0 }}>
        {eyebrow && <div className="eyebrow" style={{ color: dark ? 'rgba(242,237,226,0.6)' : 'var(--muted)', marginBottom: 2 }}>{eyebrow}</div>}
        {title && <div className="serif" style={{ fontSize: 22, fontWeight: 700, color: fg, letterSpacing: '-0.02em', lineHeight: 1.1 }}>{title}</div>}
        {subtitle && <div style={{ fontSize: 12, color: dark ? 'rgba(242,237,226,0.6)' : 'var(--muted)', marginTop: 2 }}>{subtitle}</div>}
      </div>
      {right}
    </div>
  );
}

Object.assign(window, {
  Wordmark, Seal, QRMark, SignatureMark, Tag, Avatar, PhotoPlaceholder, StatusBar, TabBar, ICONS, AppBar,
});

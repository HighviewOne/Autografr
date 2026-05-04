// screens-capture.jsx — The hero flow: QR Capture → Signing → Authenticated card

const { useState: useStateC, useEffect: useEffectC } = React;

// ─── Capture / Scan QR (fan side) ────────────────────────────
function CaptureScreen({ onClose, onComplete, celeb }) {
  const [phase, setPhase] = useStateC('scan'); // scan → signing → reveal
  const [progress, setProgress] = useStateC(0);

  useEffectC(() => {
    if (phase !== 'scan') return;
    const t = setTimeout(() => setPhase('signing'), 2400);
    return () => clearTimeout(t);
  }, [phase]);

  useEffectC(() => {
    if (phase !== 'signing') return;
    let n = 0;
    const i = setInterval(() => {
      n += 4;
      setProgress(Math.min(n, 100));
      if (n >= 100) {
        clearInterval(i);
        setTimeout(() => setPhase('reveal'), 600);
      }
    }, 80);
    return () => clearInterval(i);
  }, [phase]);

  const c = celeb || CELEBS[0];

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#000', color: 'var(--paper)', position: 'relative', overflow: 'hidden' }}>
      <StatusBar dark color="var(--paper)" />

      {/* Camera viewport — using PhotoPlaceholder as a stand-in for a live capture */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <PhotoPlaceholder name={c.name} hue={c.hue} height={920} scene="stage" vignette={false} />
        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(0,0,0,0.55) 0%, transparent 30%, transparent 70%, rgba(0,0,0,0.85) 100%)' }} />
      </div>

      {/* Top bar overlay */}
      <div style={{ position: 'relative', zIndex: 2, display: 'flex', justifyContent: 'space-between', padding: '8px 16px 0' }}>
        <button onClick={onClose} style={{ width: 38, height: 38, borderRadius: 999, background: 'rgba(255,255,255,0.12)', border: '1px solid rgba(255,255,255,0.18)', color: 'var(--paper)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(8px)' }}>
          <span style={{ width: 16, height: 16 }}>{ICONS.close}</span>
        </button>
        <Tag tone="paper" size="lg"><span style={{ width: 10, height: 10, color: 'var(--signature)' }}>●</span> Live capture</Tag>
        <button style={{ width: 38, height: 38, borderRadius: 999, background: 'rgba(255,255,255,0.12)', border: '1px solid rgba(255,255,255,0.18)', color: 'var(--paper)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ width: 16, height: 16 }}>{ICONS.bolt}</span>
        </button>
      </div>

      {/* Center scan reticle */}
      {phase === 'scan' && (
        <div style={{ position: 'relative', zIndex: 2, flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
          <div className="eyebrow" style={{ color: 'var(--foil-2)', marginBottom: 18 }}>Step 1 of 2 · Frame the celebrity</div>
          <div style={{ position: 'relative', width: 240, height: 240 }}>
            {/* corner reticles */}
            {[[0,0,'tl'],[1,0,'tr'],[0,1,'bl'],[1,1,'br']].map(([x,y,k]) => (
              <div key={k} style={{ position: 'absolute', width: 36, height: 36, top: y ? 'auto' : 0, bottom: y ? 0 : 'auto', left: x ? 'auto' : 0, right: x ? 0 : 'auto', borderTop: !y ? '2px solid var(--foil-2)' : 'none', borderBottom: y ? '2px solid var(--foil-2)' : 'none', borderLeft: !x ? '2px solid var(--foil-2)' : 'none', borderRight: x ? '2px solid var(--foil-2)' : 'none' }} />
            ))}
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ position: 'absolute', width: 110, height: 110, borderRadius: 999, background: 'rgba(201,162,76,0.12)', border: '1px solid rgba(201,162,76,0.4)' }} className="pulse-ring" />
              <Seal size={120} color="var(--foil-2)" />
            </div>
            <div style={{ position: 'absolute', left: 0, right: 0, height: 2, top: '50%', background: 'linear-gradient(90deg, transparent, var(--foil-2), transparent)', boxShadow: '0 0 12px var(--foil-2)' }} />
          </div>
          <div style={{ marginTop: 28, textAlign: 'center', maxWidth: 280 }}>
            <div className="serif" style={{ fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em' }}>Show {c.name.split(' ')[0]} your QR</div>
            <div style={{ fontSize: 13, color: 'rgba(242,237,226,0.65)', marginTop: 8, lineHeight: 1.5 }}>
              Their phone scans yours — that handshake is what authenticates the autograph.
            </div>
          </div>
        </div>
      )}

      {/* Signing phase */}
      {phase === 'signing' && (
        <div style={{ position: 'relative', zIndex: 2, flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
          <Tag tone="foilDark" size="lg">Step 2 of 2 · {c.name} is signing</Tag>
          <div style={{ marginTop: 26, width: 280, height: 200, background: 'rgba(255,255,255,0.06)', borderRadius: 14, border: '1px solid rgba(201,162,76,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
            <SignatureMark width={240} height={150} variant={c.sig} color="var(--foil-2)" strokeWidth={3.6} animate />
          </div>
          <div style={{ marginTop: 24, width: 240, height: 4, borderRadius: 999, background: 'rgba(255,255,255,0.12)', overflow: 'hidden' }}>
            <div style={{ width: `${progress}%`, height: '100%', background: 'linear-gradient(90deg, var(--foil), var(--foil-2))', transition: 'width 80ms linear' }} />
          </div>
          <div className="mono" style={{ marginTop: 12, fontSize: 11, color: 'rgba(242,237,226,0.55)', letterSpacing: '0.08em' }}>
            VERIFYING PRESENCE · {progress}%
          </div>
        </div>
      )}

      {/* Reveal phase — the authenticated card */}
      {phase === 'reveal' && (
        <div style={{ position: 'relative', zIndex: 2, flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '20px 24px', background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(14px)' }}>
          <div className="rise">
            <Tag tone="foilDark" size="lg"><span style={{ width: 10, height: 10 }}>{ICONS.sparkle}</span> Authenticated</Tag>
          </div>
          <div className="rise rise-d1" style={{ marginTop: 18, transform: 'rotate(-2deg)' }}>
            <CertCard celeb={c} edition="088/365" date="May 3, 2026" small />
          </div>
          <div className="rise rise-d2" style={{ marginTop: 22, textAlign: 'center', maxWidth: 280 }}>
            <div className="serif" style={{ fontSize: 22, fontWeight: 600, letterSpacing: '-0.02em' }}>That's the one.</div>
            <div style={{ fontSize: 13, color: 'rgba(242,237,226,0.7)', marginTop: 6 }}>Card #088 of 365 · permanently yours</div>
          </div>
          <div className="rise rise-d3" style={{ marginTop: 22, display: 'flex', gap: 10, width: '100%' }}>
            <button className="btn btn-ghost-light" style={{ flex: 1 }}><span style={{ width: 14, height: 14 }}>{ICONS.share}</span> Share</button>
            <button className="btn btn-foil" style={{ flex: 1.4 }} onClick={onComplete}>Add to collection</button>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Cert Card — the signed photo with seal & QR ─────────────
function CertCard({ celeb, edition = '042/365', date = 'May 2026', small = false, big = false, rarity = 'rare', drop }) {
  const c = celeb;
  const w = small ? 280 : big ? 340 : 320;
  const sigVariant = c.sig || 'curl';
  return (
    <div style={{ width: w, background: 'var(--paper)', borderRadius: 14, overflow: 'hidden', boxShadow: '0 30px 60px -20px rgba(0,0,0,0.5), 0 8px 16px -8px rgba(0,0,0,0.3)', position: 'relative' }}>
      {/* photo */}
      <div style={{ position: 'relative' }}>
        <PhotoPlaceholder name={c.name} hue={c.hue} height={w * 1.05} scene={drop?.scene || 'studio'}>
          {/* signature on photo */}
          <div style={{ position: 'absolute', bottom: 30, right: 22 }}>
            <SignatureMark width={150} height={56} variant={sigVariant} color="var(--foil-2)" strokeWidth={2.6} animate={false} />
          </div>
          {/* Seal corner */}
          <div style={{ position: 'absolute', top: 14, right: 14 }}>
            <Seal size={64} color="var(--foil-2)" date={date.slice(-2)} />
          </div>
        </PhotoPlaceholder>
      </div>
      {/* Card label strip — like a polaroid back */}
      <div style={{ padding: '14px 16px 16px', background: 'var(--paper)', position: 'relative' }}>
        <div className="grain grain-light" />
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', position: 'relative', zIndex: 1 }}>
          <div>
            <div className="eyebrow" style={{ color: 'var(--muted)', marginBottom: 4 }}>Studio Pass · Authenticated</div>
            <div className="serif" style={{ fontSize: 17, fontWeight: 600, letterSpacing: '-0.01em', lineHeight: 1.15 }}>{c.name}</div>
            <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 2 }}>{date} · {c.field}</div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6 }}>
            <span className="edition" style={{ color: 'var(--ink)' }}>№ {edition}</span>
            {rarity && <Tag tone={rarity === 'legend' ? 'red' : rarity === 'rare' ? 'foil' : 'ink'}>{rarity}</Tag>}
          </div>
        </div>
        {/* perforation line */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 14, paddingTop: 12, borderTop: '1.5px dashed var(--line-strong)', position: 'relative', zIndex: 1 }}>
          <div style={{ width: 12, height: 12, borderRadius: 999, background: '#B5AFA0', position: 'absolute', left: -22, top: 6 }} />
          <div style={{ width: 12, height: 12, borderRadius: 999, background: '#B5AFA0', position: 'absolute', right: -22, top: 6 }} />
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ width: 32, height: 32, background: 'var(--ink)', borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 4 }}>
              <QRMark size={24} color="var(--paper)" bg="var(--ink)" seed={c.id ? c.id.charCodeAt(1) : 7} withCorners={false} />
            </div>
            <div>
              <div className="mono" style={{ fontSize: 9.5, color: 'var(--muted)', letterSpacing: '0.05em' }}>VERIFY</div>
              <div className="mono" style={{ fontSize: 10, color: 'var(--ink)', fontWeight: 600 }}>aut.gr/{c.id || 'x'}-{edition.split('/')[0]}</div>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--stamp)' }}>
            <span style={{ width: 12, height: 12 }}>{ICONS.check}</span>
            <span style={{ fontSize: 10, fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>Verified</span>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { CaptureScreen, CertCard });

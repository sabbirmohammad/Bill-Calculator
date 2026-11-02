// Subtle Synthesized Web Audio Haptics
let audioCtx = null;
let isAudioMuted = typeof window !== 'undefined' ? localStorage.getItem('bd_bill_sound_muted') === 'true' : false;

export function getMuteState() {
  return isAudioMuted;
}

export function setMuteState(muted) {
  isAudioMuted = Boolean(muted);
  if (typeof window !== 'undefined') {
    localStorage.setItem('bd_bill_sound_muted', muted ? 'true' : 'false');
  }
}

export function playTickSound(frequency = 600, duration = 0.02) {
  if (isAudioMuted || typeof window === 'undefined') return;
  try {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (audioCtx.state === 'suspended') {
      audioCtx.resume();
    }

    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();

    osc.type = 'triangle';
    osc.frequency.setValueAtTime(frequency, audioCtx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(100, audioCtx.currentTime + duration);

    gain.gain.setValueAtTime(0.04, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + duration);

    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.start();
    osc.stop(audioCtx.currentTime + duration);
  } catch (e) {
    // Audio context not allowed or failed, fail silently
  }
}

export function playSuccessSound() {
  if (isAudioMuted || typeof window === 'undefined') return;
  try {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (audioCtx.state === 'suspended') {
      audioCtx.resume();
    }

    const now = audioCtx.currentTime;
    const osc1 = audioCtx.createOscillator();
    const osc2 = audioCtx.createOscillator();
    const gain = audioCtx.createGain();

    osc1.frequency.setValueAtTime(523.25, now); // C5
    osc2.frequency.setValueAtTime(659.25, now + 0.08); // E5

    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.3);

    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(audioCtx.destination);

    osc1.start(now);
    osc1.stop(now + 0.08);
    osc2.start(now + 0.08);
    osc2.stop(now + 0.3);
  } catch (e) {
    // Fail silently
  }
}

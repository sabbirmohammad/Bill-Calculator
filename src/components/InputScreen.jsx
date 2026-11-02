import React, { useState } from 'react';
import { 
  ArrowRight, 
  Settings2, 
  Plus, 
  Minus, 
  Tv, 
  DollarSign, 
  Info, 
  Zap, 
  Check, 
  Sparkles,
  Gauge,
  History,
  AlertCircle
} from 'lucide-react';
import { translations } from '../utils/translations';
import { toBengaliNumber, toEnglishNumber, calculateBill, formatTaka, TARIFF_RATES } from '../utils/calculator';
import { playTickSound, playSuccessSound } from '../utils/audio';

export default function InputScreen({
  lang,
  units,
  setUnits,
  onCalculate,
  includeAdditional,
  setIncludeAdditional,
  options,
  setOptions,
  onOpenAppliances,
  onOpenBudget
}) {
  const t = translations[lang];
  const [inputMode, setInputMode] = useState('direct'); // 'direct' | 'reading'
  const [prevReading, setPrevReading] = useState('12450');
  const [currReading, setCurrReading] = useState(() => {
    const u = parseFloat(units) || 0;
    return String(12450 + u);
  });
  const [readingError, setReadingError] = useState(null);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [isFocused, setIsFocused] = useState(false);
  const [isTyping, setIsTyping] = useState(false);

  const currentNum = parseFloat(units) || 0;
  const quickPreview = calculateBill(units, { ...options, includeAdditional });

  const handleInputChange = (e) => {
    const rawVal = e.target.value;
    const val = toEnglishNumber(rawVal);
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      setUnits(val);
      playTickSound(450 + Math.min(val * 2, 400), 0.015);
      setIsTyping(true);
      setTimeout(() => setIsTyping(false), 300);
    }
  };

  const adjustUnits = (delta) => {
    const current = parseFloat(units) || 0;
    const nextVal = Math.max(0, Math.round(current + delta));
    setUnits(String(nextVal));
    playTickSound(500 + delta * 5, 0.02);
  };

  const handleReadingChange = (type, rawVal) => {
    const val = toEnglishNumber(rawVal);
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      playTickSound(460, 0.015);
      const newPrev = type === 'prev' ? val : prevReading;
      const newCurr = type === 'curr' ? val : currReading;
      if (type === 'prev') setPrevReading(val);
      if (type === 'curr') setCurrReading(val);

      if (newPrev !== '' && newCurr !== '') {
        const p = parseFloat(newPrev) || 0;
        const c = parseFloat(newCurr) || 0;
        if (c >= p) {
          const diff = Math.round((c - p) * 100) / 100;
          setUnits(String(diff));
          setReadingError(null);
        } else {
          setReadingError(t.readingErrorNegative);
          setUnits('0');
        }
      }
    }
  };

  const adjustReading = (type, delta) => {
    playTickSound(480 + delta * 2, 0.015);
    const p = parseFloat(prevReading) || 0;
    const c = parseFloat(currReading) || 0;
    if (type === 'prev') {
      const nextP = Math.max(0, Math.round(p + delta));
      setPrevReading(String(nextP));
      if (c >= nextP) {
        setUnits(String(Math.round((c - nextP) * 100) / 100));
        setReadingError(null);
      } else {
        setReadingError(t.readingErrorNegative);
        setUnits('0');
      }
    } else {
      const nextC = Math.max(0, Math.round(c + delta));
      setCurrReading(String(nextC));
      if (nextC >= p) {
        setUnits(String(Math.round((nextC - p) * 100) / 100));
        setReadingError(null);
      } else {
        setReadingError(t.readingErrorNegative);
        setUnits('0');
      }
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      if (!readingError) {
        playSuccessSound();
        onCalculate();
      }
    }
  };

  const getTierIndicator = (u) => {
    if (u <= 0) return { label: lang === 'bn' ? '০ ইউনিট' : '0 Units', color: 'text-zinc-400', badge: 'অপেক্ষমাণ' };
    if (u <= 50) return { label: lang === 'bn' ? 'লাইফলাইন রেট • ৳৪.৬৩/ইউনিট' : 'Lifeline • ৳4.63/u', color: 'text-emerald-500 dark:text-emerald-400', badge: 'লাইফলাইন সুবিধা' };
    if (u <= 75) return { label: lang === 'bn' ? 'ধাপ ১ (০-৭৫ ইউনিট) • ৳৫.২৬/ইউনিট' : 'Step 1 (0–75) • ৳5.26/u', color: 'text-sky-500', badge: 'ধাপ ১' };
    if (u <= 200) return { label: lang === 'bn' ? 'ধাপ ২ (৭৬-২০০ ইউনিট) • ৳৭.২০/ইউনিট' : 'Step 2 (76–200) • ৳7.20/u', color: 'text-blue-500', badge: 'ধাপ ২' };
    if (u <= 300) return { label: lang === 'bn' ? 'ধাপ ৩ (২০১-৩০০ ইউনিট) • ৳৭.৫৯/ইউনিট' : 'Step 3 (201–300) • ৳7.59/u', color: 'text-indigo-500', badge: 'ধাপ ৩' };
    if (u <= 400) return { label: lang === 'bn' ? 'ধাপ ৪ (৩০১-৪০০ ইউনিট) • ৳৮.০২/ইউনিট' : 'Step 4 (301–400) • ৳8.02/u', color: 'text-amber-500', badge: 'ধাপ ৪' };
    if (u <= 600) return { label: lang === 'bn' ? 'ধাপ ৫ (৪০১-৬০০ ইউনিট) • ৳১২.৬৭/ইউনিট' : 'Step 5 (401–600) • ৳12.67/u', color: 'text-orange-500', badge: 'ধাপ ৫' };
    return { label: lang === 'bn' ? 'ধাপ ৬ (>৬০০ ইউনিট) • ৳১৪.৬১/ইউনিট' : 'Step 6 (>600) • ৳14.61/u', color: 'text-rose-500', badge: 'সর্বোচ্চ ধাপ' };
  };

  const currentTier = getTierIndicator(currentNum);

  const isSlabActive = (slab) => {
    if (slab.isLifeline) return currentNum > 0 && currentNum <= 50;
    if (currentNum <= 0) return false;
    if (currentNum <= 50) return false;
    if (slab.stepNumber === 1) return currentNum > 0;
    if (slab.stepNumber === 2) return currentNum > 75;
    if (slab.stepNumber === 3) return currentNum > 200;
    if (slab.stepNumber === 4) return currentNum > 300;
    if (slab.stepNumber === 5) return currentNum > 400;
    if (slab.stepNumber === 6) return currentNum > 600;
    return false;
  };

  const isCurrentMaxSlab = (slab) => {
    if (slab.isLifeline) return currentNum > 0 && currentNum <= 50;
    if (currentNum <= 0) return false;
    if (currentNum <= 50) return false;
    if (slab.stepNumber === 1) return currentNum <= 75;
    if (slab.stepNumber === 2) return currentNum > 75 && currentNum <= 200;
    if (slab.stepNumber === 3) return currentNum > 200 && currentNum <= 300;
    if (slab.stepNumber === 4) return currentNum > 300 && currentNum <= 400;
    if (slab.stepNumber === 5) return currentNum > 400 && currentNum <= 600;
    if (slab.stepNumber === 6) return currentNum > 600;
    return false;
  };

  return (
    <div className="w-full animate-fadeIn">
      
      {/* 2-Column Full-Width Grid: Left Input Cockpit, Right Live Tariff Matrix */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 items-stretch">
        
        {/* LEFT COLUMN: Large Prominent Input Dial (lg:col-span-6) */}
        <div className="lg:col-span-6 bg-white dark:bg-zinc-900/95 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-5 sm:p-6 shadow-sm flex flex-col justify-between space-y-4">
          
          <div>
            {/* Top Badge & Tier Line */}
            <div className="flex items-center justify-between mb-3">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-zinc-100 dark:bg-white/[0.06] text-zinc-700 dark:text-zinc-300 border border-zinc-200 dark:border-white/[0.08]">
                <Zap className={`w-3.5 h-3.5 text-amber-500 fill-amber-500 transition-transform ${isTyping ? 'scale-125' : ''}`} />
                <span>{lang === 'bn' ? 'আবাসিক মিটার রিডিং' : 'Electric Meter Reading'}</span>
              </div>

              <div className="text-xs font-mono font-semibold">
                <span className={currentTier.color}>
                  ● {currentTier.label}
                </span>
              </div>
            </div>

            {/* Input Mode Selector: Direct Units vs By Meter Reading */}
            <div className="grid grid-cols-2 p-1 rounded-xl bg-zinc-100 dark:bg-zinc-800/80 border border-zinc-200/80 dark:border-white/[0.08] mb-3.5 text-xs">
              <button
                type="button"
                onClick={() => {
                  playTickSound(450, 0.02);
                  setInputMode('direct');
                }}
                className={`py-2 px-3 rounded-lg font-bold transition-all flex items-center justify-center gap-1.5 ${
                  inputMode === 'direct'
                    ? 'bg-white dark:bg-zinc-900 text-zinc-950 dark:text-white shadow-xs'
                    : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
                }`}
              >
                <Zap className={`w-3.5 h-3.5 ${inputMode === 'direct' ? 'text-amber-500 fill-amber-500' : 'text-zinc-400'}`} />
                <span>{t.inputModeDirect}</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  playTickSound(500, 0.02);
                  setInputMode('reading');
                  const u = parseFloat(units) || 0;
                  const p = parseFloat(prevReading) || 12450;
                  setPrevReading(String(p));
                  setCurrReading(String(p + u));
                  setReadingError(null);
                }}
                className={`py-2 px-3 rounded-lg font-bold transition-all flex items-center justify-center gap-1.5 ${
                  inputMode === 'reading'
                    ? 'bg-white dark:bg-zinc-900 text-zinc-950 dark:text-white shadow-xs'
                    : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
                }`}
              >
                <Gauge className={`w-3.5 h-3.5 ${inputMode === 'reading' ? 'text-sky-500' : 'text-zinc-400'}`} />
                <span>{t.inputModeReading}</span>
              </button>
            </div>

            <h2 className="text-xl sm:text-2xl font-black text-zinc-950 dark:text-white tracking-tight">
              {inputMode === 'direct' ? t.directInputTitle : t.readingInputTitle}
            </h2>
            <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-1">
              {inputMode === 'direct' ? t.directInputSubtitle : t.readingInputSubtitle}
            </p>
          </div>

          {/* MODE 1: DIRECT UNITS ENTRY */}
          {inputMode === 'direct' && (
            <div className={`relative rounded-2xl bg-zinc-50 dark:bg-zinc-950 border-2 transition-all p-5 sm:p-6 text-center ${
              isFocused
                ? 'border-amber-500 electric-meter-active ring-4 ring-amber-500/10'
                : 'border-zinc-200 dark:border-white/[0.08]'
            }`}>
              
              {/* Corner pulse icon */}
              <div className="absolute top-3 right-3 text-zinc-400">
                <Sparkles className={`w-4 h-4 transition-all ${isTyping ? 'text-amber-500 rotate-12 scale-110' : 'opacity-40'}`} />
              </div>

              {/* Steppers & Giant Numeric Input */}
              <div className="flex items-center justify-center gap-3 sm:gap-4 my-2">
                <button
                  type="button"
                  onClick={() => adjustUnits(-1)}
                  className="h-12 w-12 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-sm flex items-center justify-center active:scale-90 transition-transform"
                  title="-1 Unit"
                >
                  <Minus className="w-5 h-5" />
                </button>

                {/* Huge Monospace Numeric Display */}
                <div className="relative flex-1 max-w-[220px]">
                  <input
                    id="unitInput"
                    type="text"
                    autoFocus
                    inputMode="decimal"
                    value={units}
                    onChange={handleInputChange}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                    onKeyDown={handleKeyDown}
                    placeholder="0"
                    className="w-full h-16 sm:h-20 text-center text-5xl sm:text-6xl font-black font-mono rounded-xl bg-transparent text-zinc-950 dark:text-white outline-none tabular-nums tracking-tight"
                  />
                  <span className="block text-xs font-mono font-semibold text-zinc-400 uppercase tracking-widest mt-1">
                    kWh / {t.unitText}
                  </span>
                </div>

                <button
                  type="button"
                  onClick={() => adjustUnits(1)}
                  className="h-12 w-12 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-sm flex items-center justify-center active:scale-90 transition-transform"
                  title="+1 Unit"
                >
                  <Plus className="w-5 h-5" />
                </button>
              </div>

              {/* Quick Presets Chips */}
              <div className="flex flex-wrap items-center justify-center gap-1.5 mt-3 pt-2.5 border-t border-zinc-200/60 dark:border-white/[0.06]">
                <span className="text-[10px] uppercase font-mono font-bold text-zinc-400 mr-1">
                  {t.quickPresets}:
                </span>
                {[
                  { label: '৫০ (L)', val: 50 },
                  { label: '৭৫', val: 75 },
                  { label: '১৫০', val: 150 },
                  { label: '২০০', val: 200 },
                  { label: '৩০০', val: 300 },
                  { label: '+১০', val: 'delta+10' },
                  { label: '+৫০', val: 'delta+50' }
                ].map((preset) => (
                  <button
                    key={preset.label}
                    type="button"
                    onClick={() => {
                      if (typeof preset.val === 'string' && preset.val.startsWith('delta')) {
                        const add = parseInt(preset.val.replace('delta', ''), 10);
                        adjustUnits(add);
                      } else {
                        setUnits(String(preset.val));
                        playTickSound(600, 0.02);
                      }
                    }}
                    className="px-2 py-1 rounded-md text-[11px] font-mono font-semibold bg-white dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-700 dark:text-zinc-300 border border-zinc-200 dark:border-white/[0.08] transition-all active:scale-95 shadow-2xs"
                  >
                    {lang === 'bn' ? toBengaliNumber(preset.label) : preset.label}
                  </button>
                ))}
              </div>

              {/* Live Real-time estimate directly inside the meter box */}
              <div className="mt-3 pt-2.5 border-t border-zinc-200/60 dark:border-white/[0.06] flex items-center justify-between text-xs text-zinc-600 dark:text-zinc-400">
                <span className="font-medium">
                  {lang === 'bn' ? 'আনুমানিক মোট বিল:' : 'Instant Estimate:'}
                </span>
                <span className="font-mono font-extrabold text-sm sm:text-base text-zinc-950 dark:text-white">
                  ~{t.currencySymbol}{formatTaka(quickPreview.totalBill, lang)} {t.takaUnit}
                </span>
              </div>

            </div>
          )}

          {/* MODE 2: METER READING DIFFERENCE (Current - Previous) */}
          {inputMode === 'reading' && (
            <div className="space-y-3">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                
                {/* PREVIOUS READING CARD */}
                <div className="p-3.5 sm:p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-white/[0.08] flex flex-col justify-between">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-bold text-zinc-700 dark:text-zinc-300 flex items-center gap-1.5">
                      <History className="w-3.5 h-3.5 text-zinc-400" />
                      <span>{t.previousReadingLabel}</span>
                    </span>
                    <span className="text-[10px] font-mono text-zinc-400">kWh</span>
                  </div>
                  <p className="text-[11px] text-zinc-400 mb-2 leading-tight">
                    {t.previousReadingSub}
                  </p>

                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => adjustReading('prev', -10)}
                      className="h-10 w-9 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-2xs flex items-center justify-center active:scale-90 transition-transform text-xs"
                      title="-10"
                    >
                      -10
                    </button>
                    <input
                      type="text"
                      inputMode="decimal"
                      value={prevReading}
                      onChange={(e) => handleReadingChange('prev', e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="12450"
                      className="flex-1 min-w-0 h-11 text-center text-xl sm:text-2xl font-black font-mono rounded-xl bg-white dark:bg-zinc-900 text-zinc-950 dark:text-white border border-zinc-200 dark:border-white/[0.08] outline-none tabular-nums"
                    />
                    <button
                      type="button"
                      onClick={() => adjustReading('prev', 10)}
                      className="h-10 w-9 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-2xs flex items-center justify-center active:scale-90 transition-transform text-xs"
                      title="+10"
                    >
                      +10
                    </button>
                  </div>
                </div>

                {/* CURRENT READING CARD */}
                <div className="p-3.5 sm:p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-950 border-2 border-sky-500/60 dark:border-sky-500/40 flex flex-col justify-between ring-2 ring-sky-500/10">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-bold text-sky-600 dark:text-sky-400 flex items-center gap-1.5">
                      <Gauge className="w-3.5 h-3.5" />
                      <span>{t.currentReadingLabel}</span>
                    </span>
                    <span className="text-[10px] font-mono text-sky-500 font-semibold">Active</span>
                  </div>
                  <p className="text-[11px] text-zinc-400 mb-2 leading-tight">
                    {t.currentReadingSub}
                  </p>

                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => adjustReading('curr', -10)}
                      className="h-10 w-9 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-2xs flex items-center justify-center active:scale-90 transition-transform text-xs"
                      title="-10"
                    >
                      -10
                    </button>
                    <input
                      type="text"
                      inputMode="decimal"
                      value={currReading}
                      onChange={(e) => handleReadingChange('curr', e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="12635"
                      className="flex-1 min-w-0 h-11 text-center text-xl sm:text-2xl font-black font-mono rounded-xl bg-white dark:bg-zinc-900 text-zinc-950 dark:text-white border border-zinc-200 dark:border-white/[0.08] outline-none tabular-nums"
                    />
                    <button
                      type="button"
                      onClick={() => adjustReading('curr', 10)}
                      className="h-10 w-9 rounded-xl bg-white dark:bg-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 font-bold border border-zinc-200 dark:border-white/[0.08] shadow-2xs flex items-center justify-center active:scale-90 transition-transform text-xs"
                      title="+10"
                    >
                      +10
                    </button>
                  </div>
                </div>

              </div>

              {/* CALCULATION / ERROR SUMMARY BANNER */}
              {readingError ? (
                <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/60 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-500" />
                  <span>{readingError}</span>
                </div>
              ) : (
                <div className="p-3.5 rounded-xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-white/[0.08] flex items-center justify-between text-xs">
                  <div>
                    <div className="text-[10px] font-mono text-zinc-400 uppercase">
                      {t.readingFormulaLabel}
                    </div>
                    <div className="font-mono text-xs font-semibold text-zinc-600 dark:text-zinc-300 mt-0.5">
                      {lang === 'bn' ? toBengaliNumber(currReading || 0) : currReading || 0} − {lang === 'bn' ? toBengaliNumber(prevReading || 0) : prevReading || 0}
                    </div>
                  </div>

                  <div className="text-right">
                    <div className="text-[10px] font-mono text-zinc-400 uppercase">
                      {t.consumedUnitsBadge}
                    </div>
                    <div className="text-base sm:text-lg font-black font-mono text-emerald-600 dark:text-emerald-400">
                      {lang === 'bn' ? toBengaliNumber(units) : units} {t.unitText}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Additional Charges / VAT Switch */}
          <div className="pt-2 border-t border-zinc-100 dark:border-white/[0.06]">
            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={includeAdditional}
                  onChange={(e) => {
                    playTickSound();
                    setIncludeAdditional(e.target.checked);
                  }}
                  className="w-4 h-4 rounded text-zinc-900 dark:text-amber-400 focus:ring-0 border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 cursor-pointer"
                />
                <span className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
                  {lang === 'bn' ? 'ডিমান্ড চার্জ, মিটার ভাড়া ও ৫% সরকারি ভ্যাট যুক্ত রাখুন' : 'Include Demand Charge, Meter Rent & 5% VAT'}
                </span>
              </label>

              <button
                type="button"
                onClick={() => {
                  playTickSound();
                  setShowAdvanced(!showAdvanced);
                }}
                className="p-1.5 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors"
                title="Configure Parameters"
              >
                <Settings2 className="w-4 h-4" />
              </button>
            </div>

            {showAdvanced && (
              <div className="mt-2.5 p-3 rounded-xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-white/[0.06] grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs animate-fadeIn">
                <div>
                  <label className="text-zinc-400 block mb-0.5 text-[11px]">ডিমান্ড/KW (৳)</label>
                  <input
                    type="number"
                    value={options.demandChargePerKw}
                    onChange={(e) => setOptions({ ...options, demandChargePerKw: e.target.value })}
                    className="w-full p-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono text-zinc-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="text-zinc-400 block mb-0.5 text-[11px]">লোড (KW)</label>
                  <input
                    type="number"
                    step="0.5"
                    value={options.sanctionedLoad}
                    onChange={(e) => setOptions({ ...options, sanctionedLoad: e.target.value })}
                    className="w-full p-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono text-zinc-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="text-zinc-400 block mb-0.5 text-[11px]">মিটার ভাড়া (৳)</label>
                  <input
                    type="number"
                    value={options.meterRent}
                    onChange={(e) => setOptions({ ...options, meterRent: e.target.value })}
                    className="w-full p-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono text-zinc-900 dark:text-white"
                  />
                </div>
                <div>
                  <label className="text-zinc-400 block mb-0.5 text-[11px]">ভ্যাট (%)</label>
                  <input
                    type="number"
                    value={options.vatPercent}
                    onChange={(e) => setOptions({ ...options, vatPercent: e.target.value })}
                    className="w-full p-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono text-zinc-900 dark:text-white"
                  />
                </div>
              </div>
            )}
          </div>

          {/* PRIMARY CTA: Large Clickable Calculate Button */}
          <div>
            <button
              type="button"
              onClick={() => {
                playSuccessSound();
                onCalculate();
              }}
              className="w-full h-13 sm:h-14 rounded-xl bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 font-bold text-base shadow-sm flex items-center justify-center gap-2.5 transition-all transform active:scale-[0.99]"
            >
              <span>{lang === 'bn' ? 'বিল ও গ্রাফ হিসাব দেখুন' : 'Calculate Bill & Graphs'}</span>
              <ArrowRight className="w-4 h-4" />
              <kbd className="hidden sm:inline-block px-1.5 py-0.5 rounded text-[10px] font-mono bg-zinc-800 dark:bg-zinc-200 text-zinc-300 dark:text-zinc-700 ml-1">
                ↵ Enter
              </kbd>
            </button>
          </div>

        </div>

        {/* RIGHT COLUMN: Live Tariff Matrix & Auxiliary Tools (lg:col-span-6) */}
        <div className="lg:col-span-6 flex flex-col justify-between space-y-4">
          
          {/* Live Estimate Card */}
          <div className="bg-white dark:bg-zinc-900/95 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-4 sm:p-5 shadow-xs flex items-center justify-between">
            <div>
              <span className="text-[10px] font-mono uppercase tracking-wider text-zinc-400 block">
                {lang === 'bn' ? 'তাৎক্ষণিক প্রাক্কলিত বিল' : 'Real-Time Estimated Bill'}
              </span>
              <div className="flex items-baseline gap-1.5 mt-0.5">
                <span className="text-3xl sm:text-4xl font-black font-mono text-zinc-950 dark:text-white">
                  {t.currencySymbol}{formatTaka(quickPreview.totalBill, lang)}
                </span>
                <span className="text-xs font-semibold text-zinc-400">
                  {t.takaUnit} ({lang === 'bn' ? `${toBengaliNumber(currentNum)} ইউনিট` : `${currentNum}u`})
                </span>
              </div>
            </div>

            <div className="text-right">
              <span className="text-[10px] font-mono text-zinc-400 block">{t.effectiveRateLabel}</span>
              <span className="text-base font-bold font-mono text-amber-500">
                {t.currencySymbol}{formatTaka(quickPreview.effectiveRate, lang)}/u
              </span>
            </div>
          </div>

          {/* BERC Tariff Slabs Live Visual Matrix */}
          <div className="bg-white dark:bg-zinc-900/95 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-4 sm:p-5 shadow-xs flex-1">
            <div className="flex items-center justify-between pb-3 mb-3 border-b border-zinc-100 dark:border-white/[0.06]">
              <span className="text-xs font-bold text-zinc-900 dark:text-white flex items-center gap-1.5">
                <Info className="w-3.5 h-3.5 text-zinc-400" />
                <span>{lang === 'bn' ? 'বিইআরসি ট্যারিফ রেট (আপনার ইউনিট অনুসারে হাইলাইট)' : 'BERC Tariff Slabs (Live Highlight)'}</span>
              </span>
              <span className="text-[10px] font-mono text-zinc-400">LT-A Domestic</span>
            </div>

            {/* Grid of 7 Slabs */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {TARIFF_RATES.map((slab) => {
                const active = isSlabActive(slab);
                const isPeak = isCurrentMaxSlab(slab);
                const title = lang === 'bn' ? slab.titleBn : slab.titleEn;
                const range = lang === 'bn' ? slab.rangeBn : slab.rangeEn;
                const rate = lang === 'bn' ? toBengaliNumber(slab.rate.toFixed(2)) : slab.rate.toFixed(2);

                return (
                  <div
                    key={slab.id}
                    className={`p-3 rounded-xl border transition-all flex flex-col justify-between ${
                      isPeak
                        ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 border-transparent shadow-md scale-[1.02]'
                        : active
                        ? 'bg-zinc-50 dark:bg-zinc-800 text-zinc-900 dark:text-white border-zinc-300 dark:border-white/[0.15]'
                        : 'bg-transparent text-zinc-500 border-zinc-100 dark:border-white/[0.04] opacity-55'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold truncate">{title}</span>
                      {active && (
                        <span className={`w-4 h-4 rounded-full flex items-center justify-center text-[10px] ${
                          isPeak ? 'bg-amber-400 text-zinc-950 font-bold' : 'text-emerald-500'
                        }`}>
                          <Check className="w-3 h-3" />
                        </span>
                      )}
                    </div>
                    <div className={`text-[11px] font-mono mt-1 ${isPeak ? 'text-zinc-300 dark:text-zinc-600' : 'text-zinc-400'}`}>
                      {range}
                    </div>
                    <div className="mt-2 pt-1.5 border-t border-zinc-200/40 dark:border-white/[0.08] flex items-baseline justify-between">
                      <span className="text-[10px] uppercase font-mono">{lang === 'bn' ? 'রেট' : 'Rate'}</span>
                      <span className="font-mono font-bold text-xs sm:text-sm">
                        {t.currencySymbol}{rate}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Quick Subtools Ribbon */}
          <div className="flex items-center justify-between gap-3 p-3 rounded-xl bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-white/[0.06] text-xs">
            <button
              onClick={onOpenAppliances}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg hover:bg-white dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-300 transition-all font-medium"
            >
              <Tv className="w-3.5 h-3.5 text-zinc-500" />
              <span>{lang === 'bn' ? 'যন্ত্রপাতি দিয়ে ইউনিট বের করুন' : 'Appliance Unit Estimator'}</span>
            </button>

            <button
              onClick={onOpenBudget}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg hover:bg-white dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-300 transition-all font-medium"
            >
              <DollarSign className="w-3.5 h-3.5 text-emerald-500" />
              <span>{lang === 'bn' ? 'বাজেট থেকে ইউনিট হিসাব' : 'Budget to Units'}</span>
            </button>
          </div>

        </div>

      </div>

    </div>
  );
}

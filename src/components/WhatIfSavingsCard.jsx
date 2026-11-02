import React, { useState } from 'react';
import { Sparkles, TrendingDown, ArrowRight, CheckCircle2, AlertCircle } from 'lucide-react';
import { calculateBill, formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { playTickSound, playSuccessSound } from '../utils/audio';

export default function WhatIfSavingsCard({
  lang,
  units,
  totalBill,
  options,
  onApplyUnits
}) {
  const t = translations[lang];
  const currentNum = Math.max(0, parseFloat(units) || 0);

  // Determine current slab and the previous lower boundary
  const getSlabDetails = (u) => {
    if (u <= 50) {
      return {
        slabName: 'Lifeline',
        lowerBoundary: 0,
        upperBoundary: 50,
        diffToLower: 0,
        isLifeline: true
      };
    }
    if (u <= 75) {
      return {
        slabName: lang === 'bn' ? 'ধাপ ১' : 'Step 1',
        lowerBoundary: 50,
        upperBoundary: 75,
        diffToLower: u - 50,
        targetSlabName: lang === 'bn' ? 'লাইফলাইন (৳৪.৬৩)' : 'Lifeline (৳4.63)',
        isSpecialLifelineTarget: true
      };
    }
    if (u <= 200) {
      return {
        slabName: lang === 'bn' ? 'ধাপ ২' : 'Step 2',
        lowerBoundary: 75,
        upperBoundary: 200,
        diffToLower: u - 75,
        targetSlabName: lang === 'bn' ? 'ধাপ ১ (০-৭৫)' : 'Step 1 (0-75)'
      };
    }
    if (u <= 300) {
      return {
        slabName: lang === 'bn' ? 'ধাপ ৩' : 'Step 3',
        lowerBoundary: 200,
        upperBoundary: 300,
        diffToLower: u - 200,
        targetSlabName: lang === 'bn' ? 'ধাপ ২ (৭৬-২০০)' : 'Step 2 (76-200)'
      };
    }
    if (u <= 400) {
      return {
        slabName: lang === 'bn' ? 'ধাপ ৪' : 'Step 4',
        lowerBoundary: 300,
        upperBoundary: 400,
        diffToLower: u - 300,
        targetSlabName: lang === 'bn' ? 'ধাপ ৩ (২০১-৩০০)' : 'Step 3 (201-300)'
      };
    }
    if (u <= 600) {
      return {
        slabName: lang === 'bn' ? 'ধাপ ৫' : 'Step 5',
        lowerBoundary: 400,
        upperBoundary: 600,
        diffToLower: u - 400,
        targetSlabName: lang === 'bn' ? 'ধাপ ৪ (৩০১-৪০০)' : 'Step 4 (301-400)',
        isExpensiveStep5: true
      };
    }
    return {
      slabName: lang === 'bn' ? 'ধাপ ৬' : 'Step 6',
      lowerBoundary: 600,
      upperBoundary: Infinity,
      diffToLower: u - 600,
      targetSlabName: lang === 'bn' ? 'ধাপ ৫ (৪০১-৬০০)' : 'Step 5 (401-600)',
      isPeakStep6: true
    };
  };

  const slabInfo = getSlabDetails(currentNum);
  const diffToLower = Math.round(slabInfo.diffToLower * 10) / 10;

  // Selected reduction amount for interactive simulation
  // Default to lower boundary difference if reasonable (<= 35 units), otherwise default to 10
  const initialReduction = diffToLower > 0 && diffToLower <= 40 ? diffToLower : 10;
  const [reductionAmount, setReductionAmount] = useState(initialReduction);

  // If user is currently in Lifeline (0 - 50 units)
  if (slabInfo.isLifeline) {
    const buffer = Math.max(0, 50 - currentNum);
    return (
      <div className="rounded-2xl bg-gradient-to-br from-emerald-500/[0.07] to-teal-500/[0.04] border border-emerald-500/20 p-4 sm:p-5 shadow-xs">
        <div className="flex items-center gap-2 mb-2">
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 dark:bg-emerald-950/80 text-emerald-800 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
            <Sparkles className="w-3 h-3 text-emerald-500" />
            <span>{t.whatIfCardBadge}</span>
          </span>
          <span className="text-[11px] font-mono font-semibold text-emerald-600 dark:text-emerald-400">
            {lang === 'bn' ? 'লাইফলাইন স্ল্যাব সেভার' : 'Lifeline Shield Active'}
          </span>
        </div>

        <h4 className="text-sm sm:text-base font-extrabold text-zinc-950 dark:text-white">
          {t.whatIfLifelineCongrats}
        </h4>
        <p className="text-xs text-zinc-600 dark:text-zinc-300 mt-1">
          {lang === 'bn' 
            ? `আর মাত্র ${toBengaliNumber(buffer)} ইউনিট পর্যন্ত আপনি এই বিশেষ ভর্তুকিযুক্ত রেট (৳৪.৬৩/ইউনিট) উপভোগ করবেন। ৫০ ইউনিট পার হলেই বিল সাধারণ আবাসিক ধাপের হারে চলে যাবে।`
            : `You have ${buffer} units remaining within this subsidized rate (৳4.63/u). Consuming above 50 units shifts your bill to regular residential tiers.`}
        </p>
      </div>
    );
  }

  // Calculate bill at the lower boundary
  const targetUnits = slabInfo.lowerBoundary;
  const targetBillObj = calculateBill(targetUnits, options);
  const boundarySavings = Math.max(0, totalBill - targetBillObj.totalBill);

  // Current simulated reduction calculation
  const simulatedUnits = Math.max(0, currentNum - reductionAmount);
  const simulatedBillObj = calculateBill(simulatedUnits, options);
  const simulatedSavings = Math.max(0, totalBill - simulatedBillObj.totalBill);

  // Preset reduction options
  const presets = [];
  if (diffToLower > 0 && diffToLower <= 60) {
    presets.push({
      label: lang === 'bn' ? `স্ল্যাব সীমা (-${toBengaliNumber(diffToLower)} u)` : `Slab Bound (-${diffToLower}u)`,
      val: diffToLower,
      isBoundary: true
    });
  }
  [5, 10, 20, 30].forEach((p) => {
    if (p < currentNum && !presets.some((item) => Math.abs(item.val - p) < 0.5)) {
      presets.push({
        label: lang === 'bn' ? `-${toBengaliNumber(p)} ইউনিট` : `-${p} Units`,
        val: p
      });
    }
  });

  return (
    <div className="rounded-2xl bg-gradient-to-br from-amber-500/[0.06] via-emerald-500/[0.04] to-sky-500/[0.05] border border-amber-500/25 dark:border-amber-400/20 p-4 sm:p-5 shadow-xs relative overflow-hidden animate-fadeIn">
      
      {/* Header Badge */}
      <div className="flex items-center justify-between gap-2 mb-2">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 dark:bg-amber-950/70 text-amber-900 dark:text-amber-200 border border-amber-300 dark:border-amber-800">
          <Sparkles className="w-3 h-3 text-amber-500 fill-amber-500" />
          <span>{t.whatIfCardBadge}</span>
        </div>

        <span className="text-[10px] font-mono text-zinc-400">
          {t.whatIfTitle}
        </span>
      </div>

      {/* Primary Motivational Headline */}
      <div className="my-1.5">
        <h4 className="text-sm sm:text-base font-black text-zinc-950 dark:text-white leading-snug">
          {lang === 'bn' ? (
            <>
              আপনি আর মাত্র{' '}
              <span className="text-amber-600 dark:text-amber-400 underline decoration-amber-400/50 underline-offset-2">
                {toBengaliNumber(diffToLower)} ইউনিট
              </span>{' '}
              কম খরচ করলেই বিলের স্ল্যাব নেমে আসত এবং আপনার{' '}
              <span className="text-emerald-600 dark:text-emerald-400 font-mono font-black">
                ৳{formatTaka(boundarySavings, lang)}
              </span>{' '}
              সাশ্রয় হতো!
            </>
          ) : (
            <>
              If you consumed just{' '}
              <span className="text-amber-500 underline decoration-amber-400/50 underline-offset-2">
                {diffToLower} fewer units
              </span>
              , you would drop down a slab tier and save{' '}
              <span className="text-emerald-500 font-mono font-bold">
                ৳{formatTaka(boundarySavings, lang)}
              </span>
              !
            </>
          )}
        </h4>

        <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-1">
          {lang === 'bn'
            ? `বর্তমান স্তর: ${slabInfo.slabName} • লক্ষ্য স্তর: ${slabInfo.targetSlabName} (${toBengaliNumber(slabInfo.lowerBoundary)} ইউনিট)`
            : `Current tier: ${slabInfo.slabName} • Target tier: ${slabInfo.targetSlabName} (${slabInfo.lowerBoundary} Units)`}
        </p>
      </div>

      {/* Interactive Quick Reduction Buttons */}
      <div className="mt-3 pt-3 border-t border-zinc-200/60 dark:border-white/[0.08]">
        <div className="text-[11px] font-semibold text-zinc-600 dark:text-zinc-300 mb-2 flex items-center justify-between">
          <span className="flex items-center gap-1">
            <TrendingDown className="w-3.5 h-3.5 text-emerald-500" />
            <span>{t.quickReduction}:</span>
          </span>
          <span className="text-[10px] font-mono text-zinc-400">
            {lang === 'bn' ? `বর্তমান: ${toBengaliNumber(currentNum)} ইউনিট` : `Current: ${currentNum}u`}
          </span>
        </div>

        <div className="flex flex-wrap gap-1.5">
          {presets.map((preset) => {
            const isSelected = Math.abs(reductionAmount - preset.val) < 0.1;
            return (
              <button
                key={preset.label}
                type="button"
                onClick={() => {
                  playTickSound(520, 0.015);
                  setReductionAmount(preset.val);
                }}
                className={`px-2.5 py-1.5 rounded-lg text-xs font-mono font-bold transition-all flex items-center gap-1 ${
                  isSelected
                    ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 shadow-xs scale-102 ring-2 ring-emerald-500/30'
                    : 'bg-white/80 dark:bg-zinc-800/80 hover:bg-white dark:hover:bg-zinc-800 text-zinc-700 dark:text-zinc-300 border border-zinc-200 dark:border-white/[0.08]'
                }`}
              >
                {isSelected && <CheckCircle2 className="w-3 h-3 text-emerald-400" />}
                <span>{preset.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Live Financial Outcome of Simulated Reduction */}
      <div className="mt-3 p-3 rounded-xl bg-white/90 dark:bg-zinc-950/80 border border-zinc-200 dark:border-white/[0.08] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-0.5">
          <div className="text-[10px] uppercase font-mono text-zinc-400">
            {t.simulatedUnitsLabel}:{' '}
            <span className="font-bold text-zinc-900 dark:text-white">
              {lang === 'bn' ? toBengaliNumber(simulatedUnits) : simulatedUnits} {t.unitText}
            </span>
          </div>
          <div className="text-xs text-zinc-600 dark:text-zinc-300">
            <span>{t.simulatedBillLabel}: </span>
            <span className="font-mono font-bold text-zinc-950 dark:text-white">
              {t.currencySymbol}{formatTaka(simulatedBillObj.totalBill, lang)} {t.takaUnit}
            </span>
          </div>
        </div>

        <div className="flex items-center justify-between sm:justify-end gap-3 pt-2 sm:pt-0 border-t sm:border-t-0 border-zinc-100 dark:border-white/[0.06]">
          <div className="text-left sm:text-right">
            <span className="text-[10px] uppercase font-mono font-semibold text-emerald-600 dark:text-emerald-400 block">
              {t.simulatedSavingsLabel}
            </span>
            <span className="text-base sm:text-lg font-black font-mono text-emerald-600 dark:text-emerald-400">
              +{t.currencySymbol}{formatTaka(simulatedSavings, lang)}
            </span>
          </div>

          <button
            type="button"
            onClick={() => {
              playSuccessSound();
              if (onApplyUnits) onApplyUnits(simulatedUnits);
            }}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-xs transition-all active:scale-95 shrink-0"
            title={t.applySimulatedUnits}
          >
            <span>{t.applySimulatedUnits}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

    </div>
  );
}

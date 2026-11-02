import React, { useEffect } from 'react';
import { ArrowLeft, Printer, RefreshCw, Zap, Sparkles, AlertTriangle, TrendingUp } from 'lucide-react';
import AnimatedCounter from './AnimatedCounter';
import BillCharts from './BillCharts';
import StepBreakdown from './StepBreakdown';
import WhatIfSavingsCard from './WhatIfSavingsCard';
import { formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { playTickSound } from '../utils/audio';
import confetti from 'canvas-confetti';

export default function ResultScreen({
  lang,
  units,
  setUnits,
  billResult,
  options,
  onBackToInput,
  onOpenReceipt,
  isDark
}) {
  const t = translations[lang];

  const {
    energyCharge,
    demandCharge,
    meterRent,
    vatAmount,
    totalBill,
    isLifeline,
    effectiveRate,
    stepsBreakdown
  } = billResult;

  const currentNum = parseFloat(units) || 0;

  useEffect(() => {
    if (isLifeline && units <= 50 && units > 0) {
      confetti({
        particleCount: 60,
        spread: 60,
        origin: { y: 0.5 }
      });
    }
  }, [isLifeline, units]);

  const getThresholdWarning = () => {
    if (currentNum > 360 && currentNum <= 400) {
      const diff = 401 - currentNum;
      return {
        show: true,
        textBn: `সতর্কতা: ৪০০ ইউনিটের কাছাকাছি! আর মাত্র ${toBengaliNumber(diff.toFixed(0))} ইউনিট পার হলেই রেট ১২.৬৭ টাকা হয়ে যাবে।`,
        textEn: `Notice: Close to 400 units! ${diff.toFixed(0)} more units triggers ৳12.67/u rate.`
      };
    }
    if (currentNum > 45 && currentNum <= 50) {
      const diff = 51 - currentNum;
      return {
        show: true,
        textBn: `মনোযোগ: আর মাত্র ${toBengaliNumber(diff.toFixed(0))} ইউনিট পার হলে লাইফলাইন রেট বাতিল হবে।`,
        textEn: `Heads up: Exceeding ${diff.toFixed(0)} more units cancels the Lifeline rate.`
      };
    }
    return { show: false };
  };

  const thresholdWarn = getThresholdWarning();

  return (
    <div className="w-full space-y-3.5 animate-fadeIn">
      
      {/* Top Header Controls Bar */}
      <div className="flex items-center justify-between gap-2 pb-2 border-b border-zinc-200/80 dark:border-white/[0.08]">
        <button
          onClick={() => {
            playTickSound();
            onBackToInput();
          }}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white dark:bg-zinc-900 hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-900 dark:text-zinc-100 font-bold text-xs border border-zinc-200 dark:border-white/[0.08] shadow-xs transition-all"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>{lang === 'bn' ? 'ইউনিট পরিবর্তন' : 'Change Units'}</span>
        </button>

        <div className="flex items-center gap-2">
          {/* Active units pill */}
          <div className="px-2.5 py-1 rounded-lg bg-zinc-100 dark:bg-white/[0.06] border border-zinc-200 dark:border-white/[0.08] text-xs font-mono font-bold text-zinc-800 dark:text-zinc-200">
            {lang === 'bn' ? `${toBengaliNumber(units)} ইউনিট` : `${units} Units`}
          </div>

          <button
            onClick={() => {
              playTickSound();
              onOpenReceipt();
            }}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 text-xs font-bold shadow-xs transition-all"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>{t.printReceipt}</span>
          </button>
        </div>
      </div>

      {/* FULL-WIDTH 2-COLUMN DASHBOARD GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-start">
        
        {/* LEFT COLUMN: Hero Bill + Metrics + Actions (lg:col-span-5) */}
        <div className="lg:col-span-5 space-y-3.5">
          
          {/* Main Hero Card */}
          <div className="bg-white dark:bg-zinc-900/90 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-4 sm:p-5 shadow-xs relative overflow-hidden">
            
            <div className="flex items-center justify-between gap-2 mb-2">
              <span className="text-[10px] font-mono font-bold uppercase tracking-wider text-zinc-400">
                {lang === 'bn' ? 'সর্বমোট প্রদেয় বিদ্যুৎ বিল' : 'Net Electricity Payable'}
              </span>

              {isLifeline && units <= 50 ? (
                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                  <Sparkles className="w-3 h-3 text-emerald-500" />
                  <span>{lang === 'bn' ? 'লাইফলাইন (৳৪.৬৩)' : 'Lifeline (৳4.63)'}</span>
                </span>
              ) : units > 600 ? (
                <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-50 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800">
                  <AlertTriangle className="w-3 h-3 text-rose-500" />
                  <span>{lang === 'bn' ? 'ধাপ ৬ (>৬০০ u)' : 'Step 6 (>600 u)'}</span>
                </span>
              ) : null}
            </div>

            {/* Total Animated Amount */}
            <div className="my-1 flex items-baseline gap-2 flex-wrap">
              <div className="text-4xl sm:text-5xl font-black font-mono tracking-tight text-zinc-950 dark:text-white">
                <AnimatedCounter
                  value={totalBill}
                  duration={1000}
                  lang={lang}
                  prefix={t.currencySymbol}
                />
              </div>
              <span className="text-base font-bold text-zinc-400">
                {t.takaUnit}
              </span>
            </div>

            {/* Warning if near threshold */}
            {thresholdWarn.show && (
              <div className="mt-2.5 p-2 rounded-lg bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/60 text-amber-900 dark:text-amber-200 text-xs flex items-center gap-2">
                <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400 shrink-0" />
                <span>{lang === 'bn' ? thresholdWarn.textBn : thresholdWarn.textEn}</span>
              </div>
            )}

            {/* 4 Financial Chips */}
            <div className="grid grid-cols-2 gap-2 mt-3.5 pt-3.5 border-t border-zinc-100 dark:border-white/[0.06] text-xs">
              
              <div className="p-2 rounded-lg bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
                <div className="text-zinc-400 text-[10px]">{t.energyChargeLabel}</div>
                <div className="mt-0.5 text-sm font-bold font-mono text-zinc-900 dark:text-white">
                  {t.currencySymbol}{formatTaka(energyCharge, lang)}
                </div>
              </div>

              <div className="p-2 rounded-lg bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
                <div className="text-zinc-400 text-[10px]">{t.fixedChargesLabel}</div>
                <div className="mt-0.5 text-sm font-bold font-mono text-zinc-900 dark:text-white">
                  {t.currencySymbol}{formatTaka(demandCharge + meterRent, lang)}
                </div>
              </div>

              <div className="p-2 rounded-lg bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
                <div className="text-zinc-400 text-[10px]">{t.vatAmountLabel}</div>
                <div className="mt-0.5 text-sm font-bold font-mono text-zinc-900 dark:text-white">
                  {t.currencySymbol}{formatTaka(vatAmount, lang)}
                </div>
              </div>

              <div className="p-2 rounded-lg bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
                <div className="text-zinc-400 text-[10px] flex items-center gap-1">
                  <TrendingUp className="w-2.5 h-2.5 text-amber-500" />
                  <span>{t.effectiveRateLabel}</span>
                </div>
                <div className="mt-0.5 text-sm font-bold font-mono text-zinc-900 dark:text-white">
                  {t.currencySymbol}{formatTaka(effectiveRate, lang)}
                  <span className="text-[10px] font-normal text-zinc-400 ml-0.5">/u</span>
                </div>
              </div>

            </div>

          </div>

          {/* What-If Savings Optimizer Card */}
          <WhatIfSavingsCard
            lang={lang}
            units={units}
            totalBill={totalBill}
            options={options}
            onApplyUnits={(newUnits) => {
              if (setUnits) setUnits(String(newUnits));
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
          />

          {/* Quick Actions Panel */}
          <div className="p-3 rounded-xl bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-white/[0.08] flex items-center justify-between gap-2">
            <span className="text-xs text-zinc-600 dark:text-zinc-400">
              {lang === 'bn' ? 'অন্য কোনো রিডিং দেখতে চান?' : 'Need to check another reading?'}
            </span>

            <button
              onClick={() => {
                playTickSound();
                onBackToInput();
              }}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 font-bold text-xs shadow-xs transition-all"
            >
              <RefreshCw className="w-3 h-3" />
              <span>{lang === 'bn' ? 'নতুন হিসাব' : 'Recalculate'}</span>
            </button>
          </div>

        </div>

        {/* RIGHT COLUMN: Charts + Step Breakdown Ledger (lg:col-span-7) */}
        <div className="lg:col-span-7 space-y-3.5">
          
          {/* Visual Charts */}
          <BillCharts
            lang={lang}
            billResult={billResult}
            isDark={isDark}
          />

          {/* Itemized Step Breakdown Table */}
          <StepBreakdown
            lang={lang}
            billResult={billResult}
          />

        </div>

      </div>

    </div>
  );
}

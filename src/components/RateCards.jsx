import React from 'react';
import { TARIFF_RATES, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { Zap, Check, ArrowUpRight } from 'lucide-react';

export default function RateCards({ lang, units, isLifeline }) {
  const t = translations[lang];
  const numUnits = parseFloat(units) || 0;

  // Determine active state for each slab
  const isSlabActive = (slab) => {
    if (slab.isLifeline) {
      return isLifeline && numUnits <= 50;
    }
    if (numUnits <= 0) return false;
    if (isLifeline && numUnits <= 50) return false; // Lifeline overrides step 1

    if (slab.stepNumber === 1) return numUnits > 0;
    if (slab.stepNumber === 2) return numUnits > 75;
    if (slab.stepNumber === 3) return numUnits > 200;
    if (slab.stepNumber === 4) return numUnits > 300;
    if (slab.stepNumber === 5) return numUnits > 400;
    if (slab.stepNumber === 6) return numUnits > 600;
    return false;
  };

  // Determine if this slab is the highest active tier reached
  const isCurrentMaxSlab = (slab) => {
    if (slab.isLifeline) return isLifeline && numUnits <= 50 && numUnits > 0;
    if (numUnits <= 0) return false;
    if (isLifeline && numUnits <= 50) return false;

    if (slab.stepNumber === 1) return numUnits <= 75;
    if (slab.stepNumber === 2) return numUnits > 75 && numUnits <= 200;
    if (slab.stepNumber === 3) return numUnits > 200 && numUnits <= 300;
    if (slab.stepNumber === 4) return numUnits > 300 && numUnits <= 400;
    if (slab.stepNumber === 5) return numUnits > 400 && numUnits <= 600;
    if (slab.stepNumber === 6) return numUnits > 600;
    return false;
  };

  return (
    <div className="w-full my-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4">
        <div>
          <h3 className="text-base sm:text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Zap className="w-5 h-5 text-amber-500 fill-amber-500" />
            {t.tariffRatesTitle}
          </h3>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            {t.tariffRatesSubtitle}
          </p>
        </div>
        <div className="mt-2 sm:mt-0 flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400">
          <span className="w-2.5 h-2.5 rounded-full bg-cyan-500 animate-pulse"></span>
          <span>{lang === 'bn' ? 'বর্তমান ব্যবহারভিত্তিক হাইলাইট' : 'Live Highlight Based on Consumption'}</span>
        </div>
      </div>

      {/* Grid of 7 Cards (Lifeline + 6 steps) */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-7 gap-3">
        {TARIFF_RATES.map((slab) => {
          const active = isSlabActive(slab);
          const currentPeak = isCurrentMaxSlab(slab);
          const rateFormatted = lang === 'bn' ? toBengaliNumber(slab.rate.toFixed(2)) : slab.rate.toFixed(2);
          const title = lang === 'bn' ? slab.titleBn : slab.titleEn;
          const range = lang === 'bn' ? slab.rangeBn : slab.rangeEn;

          return (
            <div
              key={slab.id}
              className={`relative rounded-2xl p-3.5 transition-all duration-300 flex flex-col justify-between border ${
                currentPeak
                  ? 'bg-gradient-to-b from-cyan-50 to-blue-50 dark:from-cyan-950/40 dark:to-blue-950/30 border-cyan-500 dark:border-cyan-400 shadow-lg shadow-cyan-500/10 scale-[1.02] ring-2 ring-cyan-500/20'
                  : active
                  ? 'bg-white dark:bg-slate-800/80 border-cyan-200 dark:border-slate-700 shadow-sm'
                  : 'bg-slate-50/70 dark:bg-slate-800/40 border-slate-200/70 dark:border-slate-800/80 opacity-75 hover:opacity-100'
              }`}
            >
              {/* Badge for peak tier or lifeline */}
              {currentPeak && (
                <div className="absolute -top-2.5 right-2 px-2 py-0.5 rounded-full text-[10px] font-extrabold uppercase tracking-wider bg-cyan-600 text-white shadow">
                  {lang === 'bn' ? 'সক্রিয় ধাপ' : 'Active'}
                </div>
              )}
              {slab.isLifeline && !currentPeak && (
                <div className="absolute -top-2.5 right-2 px-1.5 py-0.5 rounded-full text-[9px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/60 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-700">
                  {lang === 'bn' ? 'লাইফলাইন' : 'Lifeline'}
                </div>
              )}

              <div>
                <div className="flex items-center justify-between">
                  <span className={`text-xs font-bold ${currentPeak ? 'text-cyan-700 dark:text-cyan-300' : 'text-slate-600 dark:text-slate-400'}`}>
                    {title}
                  </span>
                  {active && (
                    <span className="flex items-center justify-center w-4 h-4 rounded-full bg-cyan-100 text-cyan-600 dark:bg-cyan-900/60 dark:text-cyan-300">
                      <Check className="w-2.5 h-2.5" />
                    </span>
                  )}
                </div>

                <div className="mt-1 text-[11px] font-medium text-slate-500 dark:text-slate-400">
                  {range}
                </div>
              </div>

              <div className="mt-3 pt-2.5 border-t border-slate-100 dark:border-slate-700/60">
                <div className="text-[10px] uppercase font-semibold text-slate-400 dark:text-slate-500">
                  {lang === 'bn' ? 'প্রতি ইউনিট' : 'Per Unit'}
                </div>
                <div className="flex items-baseline space-x-1">
                  <span className={`text-lg sm:text-xl font-extrabold tracking-tight ${
                    currentPeak
                      ? 'text-cyan-700 dark:text-cyan-300'
                      : 'text-slate-800 dark:text-white'
                  }`}>
                    {t.currencySymbol}{rateFormatted}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

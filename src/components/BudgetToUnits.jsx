import React, { useState } from 'react';
import { translations } from '../utils/translations';
import { formatTaka, toBengaliNumber, estimateUnitsFromBudget, calculateBill } from '../utils/calculator';
import { DollarSign, ArrowRight } from 'lucide-react';
import { playTickSound, playSuccessSound } from '../utils/audio';

export default function BudgetToUnits({ lang, onApplyUnits, options }) {
  const t = translations[lang];
  const [budget, setBudget] = useState('2000');

  const budgetNum = parseFloat(budget) || 0;
  const allowableUnits = estimateUnitsFromBudget(budgetNum, options);
  const dailyUnits = (allowableUnits / 30).toFixed(1);

  const presetBudgets = [1000, 1500, 2000, 3000, 5000, 8000];
  const verifiedBill = calculateBill(allowableUnits, options);

  return (
    <div className="space-y-6">
      
      {/* Title */}
      <div className="pb-4 border-b border-zinc-100 dark:border-white/[0.06]">
        <h3 className="text-base sm:text-lg font-extrabold text-zinc-950 dark:text-white flex items-center gap-2">
          <DollarSign className="w-5 h-5 text-emerald-500" />
          <span>{t.budgetTitle}</span>
        </h3>
        <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
          {t.budgetSubtitle}
        </p>
      </div>

      {/* Input */}
      <div>
        <label className="block text-xs font-bold text-zinc-700 dark:text-zinc-300 mb-2">
          {t.targetBudgetLabel}
        </label>
        
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-xl font-bold font-mono text-zinc-400">
            {t.currencySymbol}
          </span>
          <input
            type="number"
            step="100"
            value={budget}
            onChange={(e) => {
              setBudget(e.target.value);
              playTickSound(500, 0.015);
            }}
            className="w-full h-14 pl-10 pr-4 text-2xl font-bold font-mono rounded-2xl bg-zinc-50 dark:bg-zinc-950 text-zinc-900 dark:text-white border border-zinc-200 dark:border-white/[0.08] outline-none"
            placeholder="2000"
          />
        </div>

        {/* Chips */}
        <div className="flex flex-wrap gap-2 mt-3">
          {presetBudgets.map((b) => (
            <button
              key={b}
              onClick={() => {
                setBudget(String(b));
                playTickSound();
              }}
              className={`px-3 py-1.5 rounded-xl text-xs font-mono font-bold transition-all border ${
                budgetNum === b
                  ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 border-transparent shadow-sm'
                  : 'bg-zinc-50 dark:bg-zinc-950 text-zinc-700 dark:text-zinc-300 border-zinc-200 dark:border-white/[0.06]'
              }`}
            >
              {t.currencySymbol}{lang === 'bn' ? toBengaliNumber(b) : b}
            </button>
          ))}
        </div>
      </div>

      {/* Result Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        
        <div className="p-5 rounded-2xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
          <div className="text-xs font-mono uppercase text-zinc-400 font-semibold">
            {t.estimatedUnitsLabel}
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-4xl sm:text-5xl font-black font-mono text-zinc-950 dark:text-white">
              {lang === 'bn' ? toBengaliNumber(allowableUnits) : allowableUnits}
            </span>
            <span className="text-xs font-medium text-zinc-400">
              {t.unitText} / {lang === 'bn' ? 'মাস' : 'Month'}
            </span>
          </div>
          <p className="mt-2 text-xs text-zinc-500 font-mono">
            {lang === 'bn'
              ? `প্রকৃত সম্ভাব্য বিল: ৳${formatTaka(verifiedBill.totalBill, lang)} টাকা`
              : `Exact bill target: ৳${formatTaka(verifiedBill.totalBill, lang)}`}
          </p>
        </div>

        <div className="p-5 rounded-2xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06]">
          <div className="text-xs font-mono uppercase text-zinc-400 font-semibold">
            {t.estimatedDailyUnits}
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-4xl sm:text-5xl font-black font-mono text-zinc-950 dark:text-white">
              {lang === 'bn' ? toBengaliNumber(dailyUnits) : dailyUnits}
            </span>
            <span className="text-xs font-medium text-zinc-400">
              {t.unitText} / {lang === 'bn' ? 'প্রতিদিন' : 'Day'}
            </span>
          </div>
          <p className="mt-2 text-xs text-zinc-500">
            {lang === 'bn'
              ? 'দৈনিক ব্যবহার এই সীমার মধ্যে রাখলে বাজেটের অতিরিক্ত বিল আসবে না।'
              : 'Keep daily meter readings below this limit to ensure no bill shock.'}
          </p>
        </div>

      </div>

      <div className="flex justify-end pt-2">
        <button
          onClick={() => {
            playSuccessSound();
            onApplyUnits(allowableUnits);
          }}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 font-bold text-xs sm:text-sm shadow-sm transition-all"
        >
          <span>{lang === 'bn' ? 'এই ইউনিট ক্যালকুলেটরে দেখুন' : 'Apply & View in Calculator'}</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>

    </div>
  );
}

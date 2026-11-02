import React from 'react';
import { formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { Layers, CheckCircle2 } from 'lucide-react';

export default function StepBreakdown({ lang, billResult }) {
  const t = translations[lang];
  const { stepsBreakdown, energyCharge } = billResult;

  return (
    <div className="bg-white dark:bg-zinc-900/90 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-4 sm:p-5 shadow-xs transition-all">
      
      {/* Header */}
      <div className="flex items-center justify-between pb-3 border-b border-zinc-100 dark:border-white/[0.06]">
        <div>
          <h3 className="text-xs sm:text-sm font-bold text-zinc-900 dark:text-white flex items-center gap-1.5">
            <Layers className="w-4 h-4 text-zinc-400" />
            <span>{t.stepsBreakdownTitle}</span>
          </h3>
        </div>

        <div className="text-[11px] font-mono font-semibold px-2 py-0.5 rounded-lg bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-300 border border-zinc-200 dark:border-white/[0.06]">
          {lang === 'bn' ? 'শক্তি চার্জ:' : 'Energy:'}{' '}
          <span className="text-zinc-950 dark:text-white font-bold">
            {t.currencySymbol}{formatTaka(energyCharge, lang)}
          </span>
        </div>
      </div>

      {/* Compact Ledger Table */}
      <div className="overflow-x-auto mt-2.5">
        <table className="w-full text-xs text-left">
          <thead className="text-[10px] uppercase font-mono text-zinc-400 border-b border-zinc-100 dark:border-white/[0.06]">
            <tr>
              <th className="py-2 px-2.5">{lang === 'bn' ? 'ধাপ' : 'Step'}</th>
              <th className="py-2 px-2.5">{lang === 'bn' ? 'পরিসীমা' : 'Range'}</th>
              <th className="py-2 px-2.5 text-right font-mono">{lang === 'bn' ? 'রেট' : 'Rate'}</th>
              <th className="py-2 px-2.5 text-right font-mono">{lang === 'bn' ? 'ইউনিট' : 'Units'}</th>
              <th className="py-2 px-2.5 text-right font-mono">{lang === 'bn' ? 'খরচ (টাকা)' : 'Cost (BDT)'}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-100 dark:divide-white/[0.04]">
            {stepsBreakdown.map((step) => {
              const title = lang === 'bn' ? step.titleBn : step.titleEn;
              const range = lang === 'bn' ? step.rangeBn : step.rangeEn;
              const unitsFormatted = lang === 'bn' ? toBengaliNumber(step.units.toFixed(1)) : step.units.toFixed(1);
              const rateFormatted = lang === 'bn' ? toBengaliNumber(step.rate.toFixed(2)) : step.rate.toFixed(2);
              const costFormatted = formatTaka(step.cost, lang);
              const isActive = step.isActive;

              return (
                <tr
                  key={step.stepId}
                  className={`transition-colors ${
                    isActive
                      ? 'bg-zinc-50/80 dark:bg-zinc-950/50 font-medium'
                      : 'opacity-40'
                  }`}
                >
                  <td className="py-2 px-2.5">
                    <div className="flex items-center gap-1.5">
                      <span className={`w-5 h-5 rounded flex items-center justify-center font-mono text-[10px] font-bold ${
                        isActive
                          ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950'
                          : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-400'
                      }`}>
                        {step.stepId === 'lifeline' ? 'L' : step.stepNumber}
                      </span>
                      <span className="font-semibold text-zinc-900 dark:text-white truncate">
                        {title}
                      </span>
                    </div>
                  </td>

                  <td className="py-2 px-2.5 text-zinc-400 text-[11px]">
                    {range}
                  </td>

                  <td className="py-2 px-2.5 text-right font-mono text-zinc-600 dark:text-zinc-400">
                    {t.currencySymbol}{rateFormatted}
                  </td>

                  <td className="py-2 px-2.5 text-right font-mono">
                    <span className={isActive ? 'font-bold text-zinc-900 dark:text-white' : 'text-zinc-400'}>
                      {unitsFormatted}
                    </span>
                  </td>

                  <td className="py-2 px-2.5 text-right font-mono font-bold text-zinc-950 dark:text-white">
                    {t.currencySymbol}{costFormatted}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

    </div>
  );
}

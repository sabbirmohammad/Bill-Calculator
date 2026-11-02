import React from 'react';
import { X, Zap } from 'lucide-react';
import { TARIFF_RATES, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { playTickSound } from '../utils/audio';

export default function TariffModal({ isOpen, onClose, lang }) {
  if (!isOpen) return null;
  const t = translations[lang];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-md animate-fadeIn">
      <div className="relative w-full max-w-xl bg-white dark:bg-zinc-900 rounded-[2rem] shadow-2xl border border-zinc-200 dark:border-white/[0.1] p-6 sm:p-8 overflow-hidden">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-zinc-200 dark:border-white/[0.08]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-zinc-900 dark:bg-white text-white dark:text-zinc-950 flex items-center justify-center text-xs font-black">
              ⚡
            </div>
            <div>
              <h3 className="font-extrabold text-base sm:text-lg text-zinc-950 dark:text-white">
                {t.tariffRatesTitle}
              </h3>
              <p className="text-xs text-zinc-400">
                {t.tariffRatesSubtitle}
              </p>
            </div>
          </div>

          <button
            onClick={() => {
              playTickSound();
              onClose();
            }}
            className="p-1.5 rounded-xl text-zinc-400 hover:text-zinc-800 dark:hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Table */}
        <div className="mt-5 overflow-x-auto rounded-2xl border border-zinc-200 dark:border-white/[0.08]">
          <table className="w-full text-xs text-left">
            <thead className="bg-zinc-50 dark:bg-zinc-950 text-zinc-500 dark:text-zinc-400 font-semibold border-b border-zinc-200 dark:border-white/[0.08]">
              <tr>
                <th className="py-3 px-4">{lang === 'bn' ? 'ধাপ' : 'Step'}</th>
                <th className="py-3 px-4">{lang === 'bn' ? 'ইউনিট সীমা' : 'Range'}</th>
                <th className="py-3 px-4 text-right font-mono">{lang === 'bn' ? 'রেট (টাকা/ইউনিট)' : 'Rate (BDT/u)'}</th>
                <th className="py-3 px-4">{lang === 'bn' ? 'শ্রেণি' : 'Status'}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100 dark:divide-white/[0.04]">
              {TARIFF_RATES.map((slab) => {
                const title = lang === 'bn' ? slab.titleBn : slab.titleEn;
                const range = lang === 'bn' ? slab.rangeBn : slab.rangeEn;
                const rate = lang === 'bn' ? toBengaliNumber(slab.rate.toFixed(2)) : slab.rate.toFixed(2);
                
                return (
                  <tr key={slab.id} className="hover:bg-zinc-50/50 dark:hover:bg-zinc-950/40">
                    <td className="py-3 px-4 font-bold text-zinc-900 dark:text-white">
                      {title}
                    </td>
                    <td className="py-3 px-4 text-zinc-500 font-medium">
                      {range}
                    </td>
                    <td className="py-3 px-4 text-right font-black font-mono text-zinc-950 dark:text-white text-sm">
                      {t.currencySymbol}{rate}
                    </td>
                    <td className="py-3 px-4 text-zinc-400 text-[11px]">
                      {slab.badge}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Footer Note */}
        <div className="mt-4 p-3 rounded-xl bg-zinc-50 dark:bg-zinc-950 text-[11px] text-zinc-400 leading-relaxed">
          {lang === 'bn'
            ? '💡 নিয়মাবলি: মোট ব্যবহার ৫০ ইউনিট পর্যন্ত হলে স্বয়ংক্রিয়ভাবে লাইফলাইন রেট (৳৪.৬৩) কার্যকর হয়। ৫০ পার হলে সাধারণ ধাপভিত্তিক হিসাব (ধাপ ১: ০-৭৫ ইউনিট @ ৳৫.২৬) প্রযোজ্য হবে।'
            : '💡 Rule: If monthly units <= 50, Lifeline rate (৳4.63) applies. Above 50, standard stepped tiers apply.'}
        </div>

        <div className="mt-5 flex justify-end">
          <button
            onClick={() => {
              playTickSound();
              onClose();
            }}
            className="px-4 py-2 rounded-xl bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 text-xs font-bold"
          >
            {t.close}
          </button>
        </div>

      </div>
    </div>
  );
}

import React from 'react';
import { formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { Zap, Printer, ShieldCheck, AlertCircle, ArrowUpRight, TrendingUp, Sparkles } from 'lucide-react';
import confetti from 'canvas-confetti';

export default function BillSummary({
  lang,
  billResult,
  onOpenReceipt,
  onViewTips
}) {
  const t = translations[lang];

  const triggerConfetti = () => {
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.6 }
    });
  };

  const {
    units,
    isLifeline,
    energyCharge,
    demandCharge,
    meterRent,
    subtotalBeforeVat,
    vatPercent,
    vatAmount,
    totalBill,
    effectiveRate,
    highestActiveRate
  } = billResult;

  // Determine category badge and color theme
  let categoryBadge = {
    textBn: 'স্বাভাবিক আবাসিক ব্যবহার',
    textEn: 'Normal Residential',
    bg: 'bg-blue-500/10 dark:bg-blue-400/10',
    border: 'border-blue-500/30',
    text: 'text-blue-700 dark:text-blue-300'
  };

  if (isLifeline && units <= 50) {
    categoryBadge = {
      textBn: '🌟 লাইফলাইন গ্রাহক (সর্বনিম্ন সাশ্রয়ী হার)',
      textEn: '🌟 Lifeline Consumer (Subsidized Rate)',
      bg: 'bg-emerald-500/15 dark:bg-emerald-400/15',
      border: 'border-emerald-500/40',
      text: 'text-emerald-700 dark:text-emerald-300'
    };
  } else if (units > 600) {
    categoryBadge = {
      textBn: '⚠️ সর্বোচ্চ স্ল্যাব গ্রাহক (১৪.৬১ টাকা/ইউনিট)',
      textEn: '⚠️ Peak Slab Consumer (৳14.61/unit)',
      bg: 'bg-rose-500/15 dark:bg-rose-400/15',
      border: 'border-rose-500/40',
      text: 'text-rose-700 dark:text-rose-300'
    };
  } else if (units > 400) {
    categoryBadge = {
      textBn: '⚡ উচ্চ ধাপের ব্যবহারকারী (১২.৬৭ টাকা/ইউনিট)',
      textEn: '⚡ High Usage Tier (৳12.67/unit)',
      bg: 'bg-amber-500/15 dark:bg-amber-400/15',
      border: 'border-amber-500/40',
      text: 'text-amber-700 dark:text-amber-300'
    };
  }

  return (
    <div className="relative rounded-3xl p-6 sm:p-8 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white shadow-2xl border border-slate-700/80 overflow-hidden">
      
      {/* Decorative ambient lighting */}
      <div className="absolute top-0 right-0 -mt-12 -mr-12 w-80 h-80 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-0 left-0 -mb-12 -ml-12 w-64 h-64 bg-blue-600/20 rounded-full blur-3xl pointer-events-none"></div>

      <div className="relative z-10">
        
        {/* Top bar: Category badge & Print action */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-5 border-b border-slate-700/60">
          <div className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold border ${categoryBadge.bg} ${categoryBadge.border} ${categoryBadge.text}`}>
            <span>{lang === 'bn' ? categoryBadge.textBn : categoryBadge.textEn}</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={onOpenReceipt}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-cyan-300 text-xs font-semibold border border-slate-600 transition-all hover:scale-[1.02] shadow-sm"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>{t.printReceipt}</span>
            </button>
          </div>
        </div>

        {/* Hero Bill Amount */}
        <div className="my-6">
          <div className="text-xs sm:text-sm font-medium text-slate-300 tracking-wide uppercase">
            {t.totalBillLabel} ({lang === 'bn' ? `${toBengaliNumber(units)} ইউনিট` : `${units} Units`})
          </div>
          
          <div className="mt-2 flex items-baseline gap-2 flex-wrap">
            <span className="text-4xl sm:text-6xl font-black tracking-tight bg-gradient-to-r from-cyan-300 via-white to-blue-200 bg-clip-text text-transparent">
              {t.currencySymbol}{formatTaka(totalBill, lang)}
            </span>
            <span className="text-sm sm:text-base font-semibold text-slate-400">
              {t.takaUnit}
            </span>
          </div>

          {/* Lifeline or high-usage alert banners */}
          {isLifeline && units <= 50 && (
            <div className="mt-4 p-3 rounded-2xl bg-emerald-950/60 border border-emerald-500/40 text-emerald-300 text-xs flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>{t.lifelineAlert}</span>
              </div>
              <button
                onClick={triggerConfetti}
                className="shrink-0 px-2 py-1 rounded-lg bg-emerald-500/30 hover:bg-emerald-500/50 text-[11px] font-bold text-white transition-all ml-2"
              >
                🎉 Celebrate
              </button>
            </div>
          )}

          {units > 600 && (
            <div className="mt-4 p-3 rounded-2xl bg-rose-950/60 border border-rose-500/40 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
              <span>{t.step6Alert}</span>
            </div>
          )}
        </div>

        {/* Key Metrics Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-4 border-t border-slate-700/60 text-slate-200">
          
          {/* Energy Charge */}
          <div className="p-3 rounded-2xl bg-slate-800/60 border border-slate-700/60">
            <div className="text-[11px] text-slate-400 font-medium">
              {t.energyChargeLabel}
            </div>
            <div className="mt-1 text-base sm:text-lg font-bold text-white">
              {t.currencySymbol}{formatTaka(energyCharge, lang)}
            </div>
          </div>

          {/* Fixed Charges (Demand + Rent) */}
          <div className="p-3 rounded-2xl bg-slate-800/60 border border-slate-700/60">
            <div className="text-[11px] text-slate-400 font-medium">
              {t.fixedChargesLabel}
            </div>
            <div className="mt-1 text-base sm:text-lg font-bold text-white">
              {t.currencySymbol}{formatTaka(demandCharge + meterRent, lang)}
            </div>
          </div>

          {/* VAT */}
          <div className="p-3 rounded-2xl bg-slate-800/60 border border-slate-700/60">
            <div className="text-[11px] text-slate-400 font-medium">
              {t.vatAmountLabel}
            </div>
            <div className="mt-1 text-base sm:text-lg font-bold text-white">
              {t.currencySymbol}{formatTaka(vatAmount, lang)}
            </div>
          </div>

          {/* Effective Rate */}
          <div className="p-3 rounded-2xl bg-cyan-950/40 border border-cyan-800/50">
            <div className="text-[11px] text-cyan-300 font-medium flex items-center gap-1">
              <TrendingUp className="w-3 h-3" />
              {t.effectiveRateLabel}
            </div>
            <div className="mt-1 text-base sm:text-lg font-bold text-cyan-200">
              {t.currencySymbol}{formatTaka(effectiveRate, lang)}
              <span className="text-[10px] font-normal text-cyan-400 ml-1">/unit</span>
            </div>
          </div>

        </div>

        {/* Saving opportunity pill */}
        {units > 75 && (
          <div className="mt-4 pt-3 flex items-center justify-between text-xs text-slate-400">
            <span>
              {lang === 'bn' 
                ? `বর্তমান সর্বোচ্চ ধাপের ট্যারিফ: ৳${toBengaliNumber(highestActiveRate.toFixed(2))} টাকা/ইউনিট` 
                : `Current highest slab rate applied: ৳${highestActiveRate.toFixed(2)}/unit`}
            </span>
            <button
              onClick={onViewTips}
              className="text-cyan-400 hover:text-cyan-300 font-semibold flex items-center gap-1 underline underline-offset-2"
            >
              <span>{lang === 'bn' ? 'সাশ্রয়ী টিপস' : 'Saving Tips'}</span>
              <ArrowUpRight className="w-3 h-3" />
            </button>
          </div>
        )}

      </div>
    </div>
  );
}

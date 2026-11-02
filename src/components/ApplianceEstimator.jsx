import React, { useState } from 'react';
import { translations } from '../utils/translations';
import { formatTaka, toBengaliNumber, calculateBill } from '../utils/calculator';
import { Tv, Wind, Lightbulb, Refrigerator, Laptop, Flame, Waves, ArrowRight, Zap } from 'lucide-react';
import { playTickSound, playSuccessSound } from '../utils/audio';

const INITIAL_APPLIANCES = [
  { id: 'fan', nameBn: 'সিলিং ফ্যান', nameEn: 'Ceiling Fan', watt: 75, quantity: 3, hours: 12, icon: Wind },
  { id: 'light', nameBn: 'এলইডি বাল্ব', nameEn: 'LED Bulb', watt: 15, quantity: 5, hours: 6, icon: Lightbulb },
  { id: 'fridge', nameBn: 'রেফ্রিজারেটর / ফ্রিজ', nameEn: 'Refrigerator', watt: 150, quantity: 1, hours: 12, icon: Refrigerator },
  { id: 'ac', nameBn: 'ইনভার্টার এসি (১.৫ টন)', nameEn: 'Inverter AC (1.5 Ton)', watt: 1200, quantity: 1, hours: 6, icon: Wind },
  { id: 'tv', nameBn: 'টেলিভিশন (LED)', nameEn: 'Television', watt: 70, quantity: 1, hours: 5, icon: Tv },
  { id: 'pump', nameBn: 'পানির পাম্প (১ HP)', nameEn: 'Water Pump (1 HP)', watt: 750, quantity: 1, hours: 0.5, icon: Waves },
  { id: 'iron', nameBn: 'ইস্ত্রি / আয়রন', nameEn: 'Electric Iron', watt: 1000, quantity: 1, hours: 0.5, icon: Flame },
  { id: 'pc', nameBn: 'কম্পিউটার / ল্যাপটপ', nameEn: 'Computer / Laptop', watt: 100, quantity: 1, hours: 6, icon: Laptop }
];

export default function ApplianceEstimator({ lang, onApplyUnits }) {
  const t = translations[lang];
  const [appliances, setAppliances] = useState(INITIAL_APPLIANCES);

  const updateAppliance = (id, field, value) => {
    const val = Math.max(0, parseFloat(value) || 0);
    setAppliances(prev => prev.map(item => item.id === id ? { ...item, [field]: val } : item));
    playTickSound(450, 0.015);
  };

  const getApplianceMonthlyUnits = (item) => {
    return (item.watt * item.quantity * item.hours * 30) / 1000;
  };

  const totalMonthlyUnits = appliances.reduce((sum, item) => sum + getApplianceMonthlyUnits(item), 0);
  const totalEstimatedBill = calculateBill(totalMonthlyUnits).totalBill;

  return (
    <div className="space-y-6">
      
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-zinc-100 dark:border-white/[0.06] gap-3">
        <div>
          <h3 className="text-base sm:text-lg font-extrabold text-zinc-950 dark:text-white flex items-center gap-2">
            <Zap className="w-5 h-5 text-amber-500 fill-amber-500" />
            <span>{t.applianceTitle}</span>
          </h3>
          <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
            {t.applianceSubtitle}
          </p>
        </div>

        {/* Total Badge & Action */}
        <div className="flex items-center gap-3">
          <div className="px-3.5 py-2 rounded-xl bg-zinc-100 dark:bg-zinc-800 text-right">
            <div className="text-[10px] font-mono font-medium text-zinc-400 uppercase">
              {lang === 'bn' ? 'মোট আনুমানিক ইউনিট' : 'Total Monthly Units'}
            </div>
            <div className="text-base sm:text-lg font-black font-mono text-zinc-950 dark:text-white">
              {lang === 'bn' ? toBengaliNumber(totalMonthlyUnits.toFixed(1)) : totalMonthlyUnits.toFixed(1)} {t.unitText}
            </div>
          </div>

          <button
            onClick={() => {
              playSuccessSound();
              onApplyUnits(Math.round(totalMonthlyUnits));
            }}
            className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 text-xs sm:text-sm font-bold shadow-sm transition-all"
          >
            <span>{t.applyApplianceToCalculator}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {appliances.map((item) => {
          const Icon = item.icon;
          const monthlyUnits = getApplianceMonthlyUnits(item);
          const name = lang === 'bn' ? item.nameBn : item.nameEn;

          return (
            <div
              key={item.id}
              className="rounded-2xl p-4 bg-zinc-50/70 dark:bg-zinc-950/70 border border-zinc-200/80 dark:border-white/[0.06] flex flex-col justify-between"
            >
              <div className="flex items-center justify-between pb-2 border-b border-zinc-200/50 dark:border-white/[0.04]">
                <div className="flex items-center space-x-2.5">
                  <div className="w-8 h-8 rounded-xl bg-white dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 flex items-center justify-center shadow-sm">
                    <Icon className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="font-bold text-sm text-zinc-900 dark:text-white">
                      {name}
                    </span>
                    <span className="text-[11px] font-mono text-zinc-400 block">
                      {item.watt}W
                    </span>
                  </div>
                </div>

                <div className="text-right">
                  <span className="text-xs font-mono font-bold text-zinc-900 dark:text-white">
                    {lang === 'bn' ? toBengaliNumber(monthlyUnits.toFixed(1)) : monthlyUnits.toFixed(1)} {t.unitText}/মাস
                  </span>
                </div>
              </div>

              {/* Inputs */}
              <div className="grid grid-cols-2 gap-3 pt-3 text-xs">
                <div>
                  <label className="text-zinc-400 block mb-1">
                    {lang === 'bn' ? 'সংখ্যা (টি)' : 'Quantity'}:
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="50"
                    value={item.quantity}
                    onChange={(e) => updateAppliance(item.id, 'quantity', e.target.value)}
                    className="w-full px-2.5 py-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono font-bold text-zinc-900 dark:text-white"
                  />
                </div>

                <div>
                  <label className="text-zinc-400 block mb-1">
                    {lang === 'bn' ? 'দৈনিক ব্যবহার (ঘণ্টা)' : 'Daily Hours'}:
                  </label>
                  <input
                    type="number"
                    step="0.5"
                    min="0"
                    max="24"
                    value={item.hours}
                    onChange={(e) => updateAppliance(item.id, 'hours', e.target.value)}
                    className="w-full px-2.5 py-1.5 rounded-lg bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono font-bold text-zinc-900 dark:text-white"
                  />
                </div>
              </div>

            </div>
          );
        })}
      </div>

      {/* Summary Footer */}
      <div className="p-4 rounded-2xl bg-zinc-900 text-white flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="text-xs sm:text-sm">
          <span className="text-zinc-400">
            {lang === 'bn' ? 'মোট আনুমানিক মাসিক বিল:' : 'Total Estimated Bill:'}
          </span>{' '}
          <span className="font-mono font-black text-white text-base sm:text-lg ml-1">
            {t.currencySymbol}{formatTaka(totalEstimatedBill, lang)} {t.takaUnit}
          </span>
        </div>

        <button
          onClick={() => {
            playSuccessSound();
            onApplyUnits(Math.round(totalMonthlyUnits));
          }}
          className="w-full sm:w-auto px-4 py-2 rounded-xl bg-white text-zinc-950 font-bold text-xs sm:text-sm shadow-sm transition-all"
        >
          {t.applyApplianceToCalculator}
        </button>
      </div>

    </div>
  );
}

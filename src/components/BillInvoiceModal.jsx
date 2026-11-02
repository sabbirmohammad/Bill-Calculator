import React from 'react';
import { formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { X, Printer, FileText } from 'lucide-react';
import { playTickSound } from '../utils/audio';

export default function BillInvoiceModal({
  isOpen,
  onClose,
  lang,
  billResult,
  options
}) {
  if (!isOpen) return null;

  const t = translations[lang];
  const {
    units,
    isLifeline,
    energyCharge,
    demandCharge,
    meterRent,
    vatPercent,
    vatAmount,
    totalBill,
    stepsBreakdown
  } = billResult;

  const currentDate = new Date().toLocaleDateString(lang === 'bn' ? 'bn-BD' : 'en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  const invoiceNumber = 'BD-' + Math.floor(100000 + Math.random() * 900000);

  const handlePrint = () => {
    playTickSound();
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-zinc-950/80 backdrop-blur-md overflow-y-auto animate-fadeIn">
      
      <div className="relative w-full max-w-2xl bg-white dark:bg-zinc-900 rounded-[2rem] shadow-2xl border border-zinc-200 dark:border-white/[0.1] my-8 overflow-hidden">
        
        {/* Modal Controls (No Print) */}
        <div className="no-print flex items-center justify-between p-5 bg-zinc-50 dark:bg-zinc-950 border-b border-zinc-200 dark:border-white/[0.08]">
          <div className="flex items-center space-x-2">
            <FileText className="w-4 h-4 text-zinc-500" />
            <h3 className="font-bold text-zinc-900 dark:text-white text-sm">
              {t.receiptModalTitle}
            </h3>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={handlePrint}
              className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-100 text-white dark:text-zinc-950 text-xs font-bold shadow-sm transition-all"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>{t.print}</span>
            </button>
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
        </div>

        {/* Printable Bill Area */}
        <div id="printable-bill" className="p-6 sm:p-10 space-y-6 text-zinc-800 dark:text-zinc-200">
          
          {/* Header */}
          <div className="flex justify-between items-start border-b border-zinc-200 dark:border-white/[0.08] pb-6">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-2xl bg-zinc-900 dark:bg-white text-white dark:text-zinc-950 flex items-center justify-center font-black text-base shadow-sm">
                ⚡
              </div>
              <div>
                <h2 className="text-xl font-extrabold text-zinc-950 dark:text-white tracking-tight">
                  {lang === 'bn' ? 'বাংলাদেশ বিদ্যুৎ বিল বিবরণী' : 'Bangladesh Utility Statement'}
                </h2>
                <p className="text-xs font-mono text-zinc-400">
                  {lang === 'bn' ? 'ট্যারিফ ক্যাটাগরি: এলটি-এ (আবাসিক)' : 'Tariff: LT-A (Residential / Domestic)'}
                </p>
              </div>
            </div>

            <div className="text-right text-xs font-mono">
              <div className="font-bold text-zinc-800 dark:text-zinc-200">
                {lang === 'bn' ? 'রসিদ নং:' : 'Ref:'} <span className="text-zinc-500">{invoiceNumber}</span>
              </div>
              <div className="text-zinc-400 mt-0.5">
                {currentDate}
              </div>
            </div>
          </div>

          {/* Specs */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-2xl bg-zinc-50 dark:bg-zinc-950 border border-zinc-200/80 dark:border-white/[0.06] text-xs font-mono">
            <div>
              <span className="text-zinc-400 block font-sans">{lang === 'bn' ? 'মোট ব্যবহার:' : 'Consumption:'}</span>
              <span className="font-bold text-sm text-zinc-950 dark:text-white">
                {lang === 'bn' ? `${toBengaliNumber(units)} ইউনিট` : `${units} Units`}
              </span>
            </div>
            <div>
              <span className="text-zinc-400 block font-sans">{lang === 'bn' ? 'অনুমোদিত লোড:' : 'Sanctioned:'}</span>
              <span className="font-bold text-sm text-zinc-950 dark:text-white">
                {lang === 'bn' ? `${toBengaliNumber(options.sanctionedLoad)} KW` : `${options.sanctionedLoad} KW`}
              </span>
            </div>
            <div>
              <span className="text-zinc-400 block font-sans">{lang === 'bn' ? 'গ্রাহক শ্রেণি:' : 'Tier Category:'}</span>
              <span className="font-bold text-sm text-zinc-900 dark:text-white font-sans">
                {isLifeline && units <= 50 ? (lang === 'bn' ? 'লাইফলাইন' : 'Lifeline') : (lang === 'bn' ? 'সাধারণ আবাসিক' : 'General')}
              </span>
            </div>
            <div>
              <span className="text-zinc-400 block font-sans">{lang === 'bn' ? 'বিল চক্র:' : 'Billing Period:'}</span>
              <span className="font-bold text-sm text-zinc-950 dark:text-white font-sans">
                {lang === 'bn' ? '১ মাস (৩০ দিন)' : '1 Month'}
              </span>
            </div>
          </div>

          {/* Slab Breakdown Table */}
          <div>
            <h4 className="text-[11px] font-mono uppercase tracking-wider font-semibold text-zinc-400 mb-2">
              {lang === 'bn' ? 'স্ল্যাবভিত্তিক এনার্জি চার্জ বিস্তারিত:' : 'Itemized Step Breakdown:'}
            </h4>
            <div className="overflow-x-auto rounded-xl border border-zinc-200 dark:border-white/[0.08]">
              <table className="w-full text-xs text-left">
                <thead className="bg-zinc-50 dark:bg-zinc-950 text-zinc-600 dark:text-zinc-400 font-semibold border-b border-zinc-200 dark:border-white/[0.08]">
                  <tr>
                    <th className="py-2.5 px-3">{lang === 'bn' ? 'ধাপ' : 'Step'}</th>
                    <th className="py-2.5 px-3">{lang === 'bn' ? 'পরিসীমা' : 'Range'}</th>
                    <th className="py-2.5 px-3 text-right font-mono">{lang === 'bn' ? 'ইউনিট' : 'Units'}</th>
                    <th className="py-2.5 px-3 text-right font-mono">{lang === 'bn' ? 'রেট (টাকা)' : 'Rate'}</th>
                    <th className="py-2.5 px-3 text-right font-mono">{lang === 'bn' ? 'মোট টাকা' : 'Amount'}</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100 dark:divide-white/[0.04] font-mono">
                  {stepsBreakdown.filter(s => s.units > 0).map((step) => (
                    <tr key={step.stepId} className="hover:bg-zinc-50/50 dark:hover:bg-zinc-950/40">
                      <td className="py-2.5 px-3 font-sans font-bold text-zinc-900 dark:text-zinc-100">
                        {lang === 'bn' ? step.titleBn : step.titleEn}
                      </td>
                      <td className="py-2.5 px-3 text-zinc-400 font-sans">
                        {lang === 'bn' ? step.rangeBn : step.rangeEn}
                      </td>
                      <td className="py-2.5 px-3 text-right">
                        {lang === 'bn' ? toBengaliNumber(step.units.toFixed(1)) : step.units.toFixed(1)}
                      </td>
                      <td className="py-2.5 px-3 text-right">
                        {t.currencySymbol}{lang === 'bn' ? toBengaliNumber(step.rate.toFixed(2)) : step.rate.toFixed(2)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-bold text-zinc-950 dark:text-white">
                        {t.currencySymbol}{formatTaka(step.cost, lang)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Subtotals & Taxes */}
          <div className="space-y-2 pt-2 border-t border-zinc-200 dark:border-white/[0.08] text-xs font-mono">
            <div className="flex justify-between text-zinc-600 dark:text-zinc-400 font-sans">
              <span>{t.energyChargeLabel}:</span>
              <span className="font-bold text-zinc-950 dark:text-white font-mono">
                {t.currencySymbol}{formatTaka(energyCharge, lang)}
              </span>
            </div>

            {demandCharge > 0 && (
              <div className="flex justify-between text-zinc-600 dark:text-zinc-400 font-sans">
                <span>{t.demandChargeLabel} ({options.sanctionedLoad} KW × ৳{options.demandChargePerKw}):</span>
                <span className="font-bold text-zinc-950 dark:text-white font-mono">
                  {t.currencySymbol}{formatTaka(demandCharge, lang)}
                </span>
              </div>
            )}

            {meterRent > 0 && (
              <div className="flex justify-between text-zinc-600 dark:text-zinc-400 font-sans">
                <span>{t.meterRentLabel}:</span>
                <span className="font-bold text-zinc-950 dark:text-white font-mono">
                  {t.currencySymbol}{formatTaka(meterRent, lang)}
                </span>
              </div>
            )}

            <div className="flex justify-between text-zinc-600 dark:text-zinc-400 font-sans">
              <span>{lang === 'bn' ? `সরকারি ভ্যাট (${toBengaliNumber(vatPercent)}%):` : `Government VAT (${vatPercent}%):`}</span>
              <span className="font-bold text-zinc-950 dark:text-white font-mono">
                {t.currencySymbol}{formatTaka(vatAmount, lang)}
              </span>
            </div>

            {/* Total Grand Bill */}
            <div className="flex justify-between items-center pt-3 border-t-2 border-zinc-300 dark:border-zinc-700 text-base sm:text-lg font-black text-zinc-950 dark:text-white">
              <span className="font-sans">{lang === 'bn' ? 'সর্বমোট প্রদেয় বিল:' : 'Total Payable Net:'}</span>
              <span className="text-xl sm:text-2xl font-mono text-zinc-950 dark:text-white">
                {t.currencySymbol}{formatTaka(totalBill, lang)}
              </span>
            </div>
          </div>

          {/* Footer Note */}
          <div className="p-3.5 rounded-xl bg-zinc-50 dark:bg-zinc-950 text-[11px] text-zinc-400 text-center">
            {t.receiptNote}
          </div>

        </div>

      </div>
    </div>
  );
}

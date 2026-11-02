import React, { useState } from 'react';
import { Settings2, RotateCcw, Plus, Minus, HelpCircle, Check, Sparkles } from 'lucide-react';
import { translations } from '../utils/translations';
import { toBengaliNumber } from '../utils/calculator';

export default function CalculatorInput({
  lang,
  units,
  setUnits,
  includeAdditional,
  setIncludeAdditional,
  options,
  setOptions,
  onReset
}) {
  const t = translations[lang];
  const [showSettings, setShowSettings] = useState(false);

  const presets = [35, 50, 75, 120, 185, 250, 350, 500, 750];

  const handleInputChange = (e) => {
    const val = e.target.value;
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      setUnits(val);
    }
  };

  const adjustUnits = (delta) => {
    const current = parseFloat(units) || 0;
    const nextVal = Math.max(0, current + delta);
    setUnits(String(nextVal));
  };

  const handleOptionChange = (key, val) => {
    setOptions(prev => ({ ...prev, [key]: val }));
  };

  const currentNum = parseFloat(units) || 0;

  return (
    <div className="glass-panel rounded-3xl p-5 sm:p-7 relative overflow-hidden transition-all duration-300">
      
      {/* Background soft glow decoration */}
      <div className="absolute top-0 right-0 w-64 h-64 bg-cyan-400/10 rounded-full blur-3xl pointer-events-none -mr-20 -mt-20"></div>

      {/* Main Input Section */}
      <div className="relative">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-3">
          <label htmlFor="unitInput" className="text-sm sm:text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span>{t.unitInputLabel}</span>
            <span className="text-xs font-normal text-cyan-600 dark:text-cyan-400 bg-cyan-50 dark:bg-cyan-950/60 px-2 py-0.5 rounded-full border border-cyan-200 dark:border-cyan-800">
              kWh / {lang === 'bn' ? 'ইউনিট' : 'Units'}
            </span>
          </label>

          <button
            onClick={onReset}
            className="mt-1 sm:mt-0 self-start sm:self-auto flex items-center space-x-1 text-xs text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>{t.resetButton}</span>
          </button>
        </div>

        {/* Input Box with Steppers */}
        <div className="flex items-center gap-2 sm:gap-3">
          <button
            onClick={() => adjustUnits(-10)}
            className="h-14 px-3 sm:px-4 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold text-sm transition-all flex items-center justify-center border border-slate-200 dark:border-slate-700 active:scale-95"
            title="-10 Units"
          >
            -10
          </button>
          <button
            onClick={() => adjustUnits(-1)}
            className="h-14 w-12 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold transition-all flex items-center justify-center border border-slate-200 dark:border-slate-700 active:scale-95"
            title="-1 Unit"
          >
            <Minus className="w-5 h-5" />
          </button>

          {/* Large text input */}
          <div className="relative flex-1">
            <input
              id="unitInput"
              type="text"
              inputMode="decimal"
              value={units}
              onChange={handleInputChange}
              placeholder={t.unitInputPlaceholder}
              className="w-full h-14 sm:h-16 px-4 text-center text-2xl sm:text-3xl font-extrabold tracking-wide rounded-2xl bg-white dark:bg-slate-800/90 text-slate-900 dark:text-white border-2 border-slate-200 dark:border-slate-700 focus:border-cyan-500 dark:focus:border-cyan-400 focus:ring-4 focus:ring-cyan-500/20 outline-none transition-all shadow-inner"
            />
            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-xs sm:text-sm font-semibold text-slate-400 dark:text-slate-500 pointer-events-none">
              {t.unitText}
            </span>
          </div>

          <button
            onClick={() => adjustUnits(1)}
            className="h-14 w-12 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold transition-all flex items-center justify-center border border-slate-200 dark:border-slate-700 active:scale-95"
            title="+1 Unit"
          >
            <Plus className="w-5 h-5" />
          </button>
          <button
            onClick={() => adjustUnits(10)}
            className="h-14 px-3 sm:px-4 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold text-sm transition-all flex items-center justify-center border border-slate-200 dark:border-slate-700 active:scale-95"
            title="+10 Units"
          >
            +10
          </button>
        </div>

        {/* Range Slider for intuitive feedback */}
        <div className="mt-4 px-1">
          <input
            type="range"
            min="0"
            max="1000"
            step="1"
            value={currentNum > 1000 ? 1000 : currentNum}
            onChange={(e) => setUnits(e.target.value)}
            className="w-full h-2 bg-slate-200 dark:bg-slate-700 rounded-lg appearance-none cursor-pointer accent-cyan-500 focus:outline-none"
          />
          <div className="flex justify-between text-[11px] text-slate-400 dark:text-slate-500 font-medium mt-1">
            <span>০ {t.unitText}</span>
            <span>২০০ {t.unitText}</span>
            <span>৪০০ {t.unitText}</span>
            <span>৬০০ {t.unitText}</span>
            <span>১০০০+ {t.unitText}</span>
          </div>
        </div>

        {/* Quick Presets */}
        <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800/60">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              {t.quickUnits}:
            </span>
          </div>
          <div className="flex flex-wrap gap-1.5 sm:gap-2">
            {presets.map((preset) => {
              const isSelected = currentNum === preset;
              const displayVal = lang === 'bn' ? toBengaliNumber(preset) : preset;
              return (
                <button
                  key={preset}
                  onClick={() => setUnits(String(preset))}
                  className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                    isSelected
                      ? 'bg-cyan-600 text-white shadow-md shadow-cyan-600/30 ring-2 ring-cyan-500/30'
                      : 'bg-slate-100 dark:bg-slate-800/90 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 border border-slate-200/80 dark:border-slate-700/80'
                  }`}
                >
                  {displayVal} {t.unitText}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Additional Charges & VAT Section */}
      <div className="mt-5 pt-4 border-t border-slate-200/80 dark:border-slate-800/80">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setIncludeAdditional(!includeAdditional)}>
            <div className={`w-5 h-5 rounded-md flex items-center justify-center transition-colors border ${
              includeAdditional
                ? 'bg-cyan-600 border-cyan-600 text-white'
                : 'bg-white dark:bg-slate-800 border-slate-300 dark:border-slate-600'
            }`}>
              {includeAdditional && <Check className="w-3.5 h-3.5" />}
            </div>
            <div>
              <div className="text-xs sm:text-sm font-bold text-slate-800 dark:text-slate-200">
                {t.additionalChargesToggle}
              </div>
              <div className="text-[11px] text-slate-500 dark:text-slate-400">
                {t.additionalChargesSubtitle}
              </div>
            </div>
          </div>

          <button
            onClick={() => setShowSettings(!showSettings)}
            className={`p-2 rounded-xl text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200 transition-colors ${
              showSettings ? 'bg-slate-100 dark:bg-slate-800 text-cyan-600 dark:text-cyan-400' : ''
            }`}
            title="Configure parameters"
          >
            <Settings2 className="w-4 h-4" />
          </button>
        </div>

        {/* Collapsible Advanced Parameters */}
        {showSettings && (
          <div className="mt-4 p-4 rounded-2xl bg-slate-50/90 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 text-xs animate-fadeIn space-y-3">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              <div>
                <label className="block text-slate-600 dark:text-slate-400 mb-1 font-medium">
                  {t.demandChargeLabel}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    value={options.demandChargePerKw}
                    onChange={(e) => handleOptionChange('demandChargePerKw', e.target.value)}
                    className="w-full px-3 py-1.5 rounded-lg bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-white"
                  />
                  <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 text-[10px]">
                    {t.currencySymbol}/KW
                  </span>
                </div>
              </div>

              <div>
                <label className="block text-slate-600 dark:text-slate-400 mb-1 font-medium">
                  {t.sanctionedLoadLabel}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    step="0.5"
                    min="1"
                    value={options.sanctionedLoad}
                    onChange={(e) => handleOptionChange('sanctionedLoad', e.target.value)}
                    className="w-full px-3 py-1.5 rounded-lg bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-white"
                  />
                  <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 text-[10px]">
                    KW
                  </span>
                </div>
              </div>

              <div>
                <label className="block text-slate-600 dark:text-slate-400 mb-1 font-medium">
                  {t.meterRentLabel}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    value={options.meterRent}
                    onChange={(e) => handleOptionChange('meterRent', e.target.value)}
                    className="w-full px-3 py-1.5 rounded-lg bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-white"
                  />
                  <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 text-[10px]">
                    {t.currencySymbol}
                  </span>
                </div>
              </div>

              <div>
                <label className="block text-slate-600 dark:text-slate-400 mb-1 font-medium">
                  {t.vatLabel}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    value={options.vatPercent}
                    onChange={(e) => handleOptionChange('vatPercent', e.target.value)}
                    className="w-full px-3 py-1.5 rounded-lg bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-white"
                  />
                  <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 text-[10px]">
                    %
                  </span>
                </div>
              </div>
            </div>

            {/* Lifeline Mode Toggle */}
            <div className="pt-2 border-t border-slate-200 dark:border-slate-700 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
              <span className="text-slate-600 dark:text-slate-400">
                {lang === 'bn' ? 'লাইফলাইন নির্ধারণ পদ্ধতি:' : 'Lifeline Evaluation Mode:'}
              </span>
              <div className="flex gap-1">
                {[
                  { id: 'auto', labelBn: 'স্বয়ংক্রিয় (<=৫০ ইউনিট)', labelEn: 'Auto (<= 50 Units)' },
                  { id: 'lifeline', labelBn: 'লাইফলাইন বাধ্যতামুলক', labelEn: 'Always Lifeline (if <=50)' },
                  { id: 'standard', labelBn: 'সাধারণ ধাপ ১ (০-৭৫)', labelEn: 'Standard Step 1' },
                ].map((mode) => (
                  <button
                    key={mode.id}
                    onClick={() => handleOptionChange('forceLifelineMode', mode.id)}
                    className={`px-2 py-1 rounded text-[11px] font-medium transition-all ${
                      options.forceLifelineMode === mode.id
                        ? 'bg-cyan-600 text-white'
                        : 'bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 border border-slate-300 dark:border-slate-700'
                    }`}
                  >
                    {lang === 'bn' ? mode.labelBn : mode.labelEn}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>

    </div>
  );
}

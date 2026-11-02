import React, { useEffect } from 'react';
import { X } from 'lucide-react';
import { playTickSound } from '../utils/audio';
import ApplianceEstimator from './ApplianceEstimator';
import BudgetToUnits from './BudgetToUnits';
import EnergySavingTips from './EnergySavingTips';

export default function SubToolSheet({
  activeSubTab,
  onClose,
  lang,
  t,
  onApplyUnits,
  options
}) {
  if (!activeSubTab) return null;

  // Body scroll lock on mobile
  useEffect(() => {
    if (typeof window !== 'undefined' && window.innerWidth < 640) {
      document.body.style.overflow = 'hidden';
    }
    return () => {
      if (typeof window !== 'undefined') {
        document.body.style.overflow = '';
      }
    };
  }, []);

  // Escape key listener to close drawer
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        playTickSound();
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <>
      {/* Mobile Dark Backdrop Overlay */}
      <div
        onClick={() => {
          playTickSound();
          onClose();
        }}
        className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs sm:hidden animate-fadeIn"
        aria-hidden="true"
      />

      {/* Sheet Container: Fixed Bottom Sheet on Mobile (<sm), In-line Card on Desktop (sm+) */}
      <div className="fixed inset-x-0 bottom-0 z-50 sm:static sm:z-auto sm:mb-4 max-h-[88vh] sm:max-h-none flex flex-col rounded-t-[1.75rem] sm:rounded-2xl bg-white dark:bg-zinc-900 border-t sm:border border-zinc-200 dark:border-white/[0.08] shadow-2xl sm:shadow-lg animate-slideUp sm:animate-fadeIn overflow-hidden">
        
        {/* Mobile Pull/Drag Handle Bar */}
        <div className="sm:hidden w-12 h-1.5 rounded-full bg-zinc-300 dark:bg-zinc-700 mx-auto mt-2.5 mb-1 shrink-0" />

        {/* Header */}
        <div className="flex justify-between items-center px-4 sm:px-5 py-3 border-b border-zinc-100 dark:border-white/[0.06] shrink-0">
          <span className="text-xs font-mono font-bold uppercase tracking-wider text-zinc-500 dark:text-zinc-300">
            {activeSubTab === 'appliances' && t.applianceEstimatorTab}
            {activeSubTab === 'budget' && t.budgetCalculatorTab}
            {activeSubTab === 'tips' && t.smartSavingTipsTab}
          </span>
          <button
            onClick={() => {
              playTickSound();
              onClose();
            }}
            className="p-1.5 rounded-xl bg-zinc-100 dark:bg-zinc-800 text-zinc-500 hover:text-zinc-900 dark:hover:text-white transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Scrollable Content Body */}
        <div className="overflow-y-auto p-4 sm:p-5 flex-1 overscroll-contain">
          {activeSubTab === 'appliances' && (
            <ApplianceEstimator lang={lang} onApplyUnits={onApplyUnits} />
          )}
          {activeSubTab === 'budget' && (
            <BudgetToUnits lang={lang} onApplyUnits={onApplyUnits} options={options} />
          )}
          {activeSubTab === 'tips' && (
            <EnergySavingTips lang={lang} />
          )}
        </div>

      </div>
    </>
  );
}

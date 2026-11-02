import React, { useState } from 'react';
import { Zap, Moon, Sun, Volume2, VolumeX } from 'lucide-react';
import { translations } from '../utils/translations';
import { setMuteState, playTickSound } from '../utils/audio';

export default function Header({
  lang,
  setLang,
  darkMode,
  setDarkMode,
  soundMuted,
  setSoundMuted,
  currentScreen,
  onResetToInput,
  activeSubTab,
  setActiveSubTab
}) {
  const t = translations[lang];

  const toggleSound = () => {
    const next = !soundMuted;
    setSoundMuted(next);
    setMuteState(next);
    if (!next) playTickSound(800, 0.03);
  };

  return (
    <header className="sticky top-0 z-40 bg-zinc-50/90 dark:bg-[#09090b]/90 backdrop-blur-md border-b border-zinc-200/80 dark:border-white/[0.08] transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-13 sm:h-14">
          
          {/* Logo & Product Title */}
          <div 
            onClick={() => {
              playTickSound(500);
              onResetToInput();
            }}
            className="flex items-center space-x-2.5 cursor-pointer select-none group"
          >
            <div className="w-7 h-7 rounded-lg bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-950 flex items-center justify-center font-black text-xs shadow-sm transition-transform group-hover:scale-105">
              ⚡
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-sm sm:text-base font-extrabold text-zinc-900 dark:text-white tracking-tight">
                {lang === 'bn' ? 'বিদ্যুৎ বিল ক্যালকুলেটর' : 'Electricity Bill Engine'}
              </span>
              <span className="hidden sm:inline-flex px-1.5 py-0.2 rounded text-[9px] font-mono font-semibold bg-zinc-200/60 dark:bg-white/[0.08] text-zinc-600 dark:text-zinc-400">
                LT-A
              </span>
            </div>
          </div>

          {/* Quick Subtools: Appliances & Budget */}
          <div className="hidden sm:flex items-center space-x-1.5 text-xs">
            <button
              onClick={() => {
                playTickSound();
                setActiveSubTab(activeSubTab === 'appliances' ? null : 'appliances');
              }}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                activeSubTab === 'appliances'
                  ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 font-semibold'
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-200/50 dark:hover:bg-white/[0.05]'
              }`}
            >
              {t.applianceEstimatorTab}
            </button>

            <button
              onClick={() => {
                playTickSound();
                setActiveSubTab(activeSubTab === 'budget' ? null : 'budget');
              }}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                activeSubTab === 'budget'
                  ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 font-semibold'
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-200/50 dark:hover:bg-white/[0.05]'
              }`}
            >
              {t.budgetCalculatorTab}
            </button>

            <button
              onClick={() => {
                playTickSound();
                setActiveSubTab(activeSubTab === 'tips' ? null : 'tips');
              }}
              className={`px-2.5 py-1 rounded-lg transition-all ${
                activeSubTab === 'tips'
                  ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-950 font-semibold'
                  : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-200/50 dark:hover:bg-white/[0.05]'
              }`}
            >
              {t.smartSavingTipsTab}
            </button>
          </div>

          {/* Controls: Audio Haptics, Lang & Theme */}
          <div className="flex items-center space-x-1.5">
            
            {/* Audio Toggle */}
            <button
              onClick={toggleSound}
              className={`p-1.5 rounded-lg transition-all border ${
                !soundMuted 
                  ? 'bg-zinc-100 dark:bg-white/[0.06] border-zinc-200 dark:border-white/[0.08] text-zinc-700 dark:text-zinc-300 hover:border-amber-400' 
                  : 'bg-zinc-100/50 dark:bg-white/[0.03] border-dashed border-zinc-300 dark:border-zinc-700 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-200'
              }`}
              title={soundMuted ? t.soundToggleUnmute : t.soundToggleMute}
              aria-label={soundMuted ? t.soundToggleUnmute : t.soundToggleMute}
            >
              {soundMuted ? (
                <VolumeX className="w-3.5 h-3.5 text-zinc-400" />
              ) : (
                <Volume2 className="w-3.5 h-3.5 text-amber-500 fill-amber-500/20" />
              )}
            </button>

            {/* Language Switch */}
            <div className="flex items-center bg-zinc-200/60 dark:bg-white/[0.06] rounded-lg p-0.5 border border-zinc-200 dark:border-white/[0.08]">
              <button
                onClick={() => {
                  playTickSound();
                  setLang('bn');
                }}
                className={`px-2 py-0.5 text-[11px] font-semibold rounded transition-all ${
                  lang === 'bn'
                    ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-white shadow-xs'
                    : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
                }`}
              >
                বাং
              </button>
              <button
                onClick={() => {
                  playTickSound();
                  setLang('en');
                }}
                className={`px-2 py-0.5 text-[11px] font-semibold rounded transition-all ${
                  lang === 'en'
                    ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-white shadow-xs'
                    : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
                }`}
              >
                EN
              </button>
            </div>

            {/* Dark Mode Toggle */}
            <button
              onClick={() => {
                playTickSound();
                setDarkMode(!darkMode);
              }}
              className="p-1.5 rounded-lg bg-zinc-200/60 dark:bg-white/[0.06] text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-white/[0.1] transition-colors border border-zinc-200 dark:border-white/[0.08]"
              title={darkMode ? 'Light' : 'Dark'}
            >
              {darkMode ? <Sun className="w-3.5 h-3.5 text-amber-400" /> : <Moon className="w-3.5 h-3.5 text-zinc-600" />}
            </button>
          </div>

        </div>
      </div>
    </header>
  );
}

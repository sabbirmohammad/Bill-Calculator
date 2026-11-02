import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import InputScreen from './components/InputScreen';
import ResultScreen from './components/ResultScreen';
import SubToolSheet from './components/SubToolSheet';
import TariffModal from './components/TariffModal';
import BillInvoiceModal from './components/BillInvoiceModal';
import { calculateBill } from './utils/calculator';
import { translations } from './utils/translations';
import { playTickSound, setMuteState } from './utils/audio';

export default function App() {
  const [lang, setLang] = useState(() => localStorage.getItem('bd_bill_lang') || 'bn');
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('bd_bill_theme') === 'dark' || 
      (!('bd_bill_theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
  });
  const [soundMuted, setSoundMuted] = useState(() => {
    return localStorage.getItem('bd_bill_sound_muted') === 'true';
  });
  
  // UX State: 'input' or 'result'
  const [currentView, setCurrentView] = useState('input');
  const [units, setUnits] = useState('185');
  const [includeAdditional, setIncludeAdditional] = useState(true);

  // Active sub-tool drawer (null | 'appliances' | 'budget' | 'tips')
  const [activeSubTab, setActiveSubTab] = useState(null);
  const [showTariffModal, setShowTariffModal] = useState(false);
  const [showReceiptModal, setShowReceiptModal] = useState(false);

  const [options, setOptions] = useState({
    demandChargePerKw: 42,
    meterRent: 10,
    vatPercent: 5,
    sanctionedLoad: 1,
    forceLifelineMode: 'auto'
  });

  // Dark mode class sync
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('bd_bill_theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('bd_bill_theme', 'light');
    }
  }, [darkMode]);

  // Language persistence & HTML lang attribute
  useEffect(() => {
    document.documentElement.lang = lang;
    localStorage.setItem('bd_bill_lang', lang);
  }, [lang]);

  // Audio mute state sync
  useEffect(() => {
    setMuteState(soundMuted);
  }, [soundMuted]);

  // Real-time calculation calculation result
  const billResult = calculateBill(units, {
    ...options,
    includeAdditional
  });

  const handleCalculate = () => {
    setCurrentView('result');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleApplyUnits = (newUnits) => {
    setUnits(String(newUnits));
    setActiveSubTab(null);
    setCurrentView('result');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const t = translations[lang];

  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-[#09090b] text-zinc-900 dark:text-zinc-100 transition-colors flex flex-col font-sans selection:bg-amber-400 selection:text-black">
      
      {/* Precision Header */}
      <Header
        lang={lang}
        setLang={setLang}
        darkMode={darkMode}
        setDarkMode={setDarkMode}
        soundMuted={soundMuted}
        setSoundMuted={setSoundMuted}
        currentScreen={currentView}
        onResetToInput={() => setCurrentView('input')}
        activeSubTab={activeSubTab}
        setActiveSubTab={setActiveSubTab}
      />

      {/* Main Container - Full-Width Cockpit (Zero side wasted space) */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-3 sm:py-4">
        
        {/* Sub-tool Drawer / Mobile Bottom Sheet (Appliances, Budget, Tips) */}
        <SubToolSheet
          activeSubTab={activeSubTab}
          onClose={() => setActiveSubTab(null)}
          lang={lang}
          t={t}
          onApplyUnits={handleApplyUnits}
          options={{ ...options, includeAdditional }}
        />

        {/* PRIMARY VIEW 1: Input Screen (2-column full width, zero-scroll cockpit) */}
        {currentView === 'input' && (
          <InputScreen
            lang={lang}
            units={units}
            setUnits={setUnits}
            onCalculate={handleCalculate}
            includeAdditional={includeAdditional}
            setIncludeAdditional={setIncludeAdditional}
            options={options}
            setOptions={setOptions}
            onOpenAppliances={() => setActiveSubTab('appliances')}
            onOpenBudget={() => setActiveSubTab('budget')}
            onOpenTariffModal={() => setShowTariffModal(true)}
          />
        )}

        {/* PRIMARY VIEW 2: Result Screen (2-column full width dashboard, zero-scroll) */}
        {currentView === 'result' && (
          <ResultScreen
            lang={lang}
            units={units}
            setUnits={setUnits}
            billResult={billResult}
            options={{ ...options, includeAdditional }}
            onBackToInput={() => setCurrentView('input')}
            onOpenReceipt={() => setShowReceiptModal(true)}
            onOpenTips={() => setActiveSubTab('tips')}
            isDark={darkMode}
          />
        )}

      </main>

      {/* Official Tariff Rates Modal */}
      <TariffModal
        isOpen={showTariffModal}
        onClose={() => setShowTariffModal(false)}
        lang={lang}
      />

      {/* Official Printable Receipt Modal */}
      <BillInvoiceModal
        isOpen={showReceiptModal}
        onClose={() => setShowReceiptModal(false)}
        lang={lang}
        billResult={billResult}
        options={options}
      />

      {/* Minimal Full-Width Footer */}
      <footer className="mt-auto border-t border-zinc-200/80 dark:border-white/[0.08] bg-white/40 dark:bg-zinc-950/40 backdrop-blur-sm py-2.5 text-[11px] text-zinc-400">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between">
          <div className="flex items-center space-x-1.5 font-medium">
            <span className="font-bold text-zinc-700 dark:text-zinc-300">
              {t.appTitle}
            </span>
            <span>•</span>
            <span className="font-mono text-[10px]">BERC LT-A Domestic</span>
          </div>

          <div className="text-zinc-400">
            {lang === 'bn' ? 'সচেতনভাবে বিদ্যুৎ ব্যবহার করুন' : 'Conserve Energy, Save Power'}
          </div>
        </div>
      </footer>

    </div>
  );
}

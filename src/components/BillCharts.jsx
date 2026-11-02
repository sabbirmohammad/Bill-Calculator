import React, { useState } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
  PieChart,
  Pie,
  AreaChart,
  Area,
  CartesianGrid
} from 'recharts';
import { formatTaka, toBengaliNumber } from '../utils/calculator';
import { translations } from '../utils/translations';
import { BarChart3, PieChart as PieIcon, TrendingUp } from 'lucide-react';
import { playTickSound } from '../utils/audio';

export default function BillCharts({ lang, billResult, isDark }) {
  const t = translations[lang];
  const [chartView, setChartView] = useState('bar');

  const {
    stepsBreakdown,
    demandCharge,
    meterRent,
    vatAmount,
    totalBill,
    units
  } = billResult;

  const slabColors = [
    '#38bdf8', '#3b82f6', '#818cf8', '#f59e0b', '#f97316', '#ef4444'
  ];

  const barData = stepsBreakdown.map((s, idx) => ({
    name: lang === 'bn' ? (s.stepId === 'lifeline' ? 'L' : `ধাপ ${s.stepNumber}`) : (s.stepId === 'lifeline' ? 'L' : `S${s.stepNumber}`),
    fullName: lang === 'bn' ? s.titleBn : s.titleEn,
    units: s.units,
    cost: s.cost,
    rate: s.rate,
    isActive: s.isActive,
    color: s.stepId === 'lifeline' ? '#10b981' : slabColors[idx % slabColors.length]
  }));

  const donutData = [];
  stepsBreakdown.filter(s => s.cost > 0).forEach((s, idx) => {
    donutData.push({
      name: lang === 'bn' ? s.titleBn : s.titleEn,
      value: parseFloat(s.cost.toFixed(2)),
      color: s.stepId === 'lifeline' ? '#10b981' : slabColors[idx % slabColors.length]
    });
  });

  if (demandCharge > 0) {
    donutData.push({
      name: lang === 'bn' ? 'ডিমান্ড চার্জ' : 'Demand',
      value: parseFloat(demandCharge.toFixed(2)),
      color: '#71717a'
    });
  }

  if (meterRent > 0) {
    donutData.push({
      name: lang === 'bn' ? 'মিটার ভাড়া' : 'Rent',
      value: parseFloat(meterRent.toFixed(2)),
      color: '#a1a1aa'
    });
  }

  if (vatAmount > 0) {
    donutData.push({
      name: lang === 'bn' ? 'ভ্যাট (৫%)' : 'VAT (5%)',
      value: parseFloat(vatAmount.toFixed(2)),
      color: '#c084fc'
    });
  }

  const progressionData = [
    { label: '0–50L', rate: 4.63, title: 'Lifeline', isUserHere: billResult.isLifeline && units <= 50 },
    { label: '0–75', rate: 5.26, title: 'Step 1', isUserHere: !billResult.isLifeline && units > 0 && units <= 75 },
    { label: '76–200', rate: 7.20, title: 'Step 2', isUserHere: units > 75 && units <= 200 },
    { label: '201–300', rate: 7.59, title: 'Step 3', isUserHere: units > 200 && units <= 300 },
    { label: '301–400', rate: 8.02, title: 'Step 4', isUserHere: units > 300 && units <= 400 },
    { label: '401–600', rate: 12.67, title: 'Step 5', isUserHere: units > 400 && units <= 600 },
    { label: '600+', rate: 14.61, title: 'Step 6', isUserHere: units > 600 }
  ];

  const CustomBarTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-zinc-950 text-white p-2.5 rounded-lg shadow-lg border border-zinc-800 text-[11px] space-y-0.5 font-mono">
          <div className="font-sans font-bold text-zinc-100">{data.fullName}</div>
          <div className="text-zinc-400">
            {lang === 'bn' ? 'ইউনিট:' : 'Units:'} <span className="text-white font-semibold">{lang === 'bn' ? toBengaliNumber(data.units.toFixed(1)) : data.units.toFixed(1)}</span>
          </div>
          <div className="text-zinc-400">
            {lang === 'bn' ? 'রেট:' : 'Rate:'} <span className="text-amber-400 font-semibold">{t.currencySymbol}{lang === 'bn' ? toBengaliNumber(data.rate.toFixed(2)) : data.rate.toFixed(2)}</span>
          </div>
          <div className="pt-0.5 border-t border-zinc-800 text-emerald-400 font-bold">
            {t.currencySymbol}{formatTaka(data.cost, lang)}
          </div>
        </div>
      );
    }
    return null;
  };

  const CustomDonutTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0];
      const percent = totalBill > 0 ? ((data.value / totalBill) * 100).toFixed(1) : 0;
      return (
        <div className="bg-zinc-950 text-white p-2.5 rounded-lg shadow-lg border border-zinc-800 text-[11px] font-mono">
          <div className="font-sans font-bold text-zinc-100 mb-0.5">{data.name}</div>
          <div className="text-emerald-400 font-bold">
            {t.currencySymbol}{formatTaka(data.value, lang)} ({percent}%)
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white dark:bg-zinc-900/90 rounded-2xl border border-zinc-200/90 dark:border-white/[0.08] p-4 sm:p-5 shadow-xs transition-all">
      
      {/* Header */}
      <div className="flex items-center justify-between pb-3 border-b border-zinc-100 dark:border-white/[0.06] gap-2">
        <h3 className="text-xs sm:text-sm font-bold text-zinc-900 dark:text-white flex items-center gap-1.5">
          <BarChart3 className="w-4 h-4 text-amber-500" />
          <span>{lang === 'bn' ? 'ভিজ্যুয়াল চার্ট' : 'Visual Charts'}</span>
        </h3>

        {/* View Switcher */}
        <div className="flex bg-zinc-100 dark:bg-zinc-950 p-0.5 rounded-lg border border-zinc-200 dark:border-white/[0.06] text-[11px] font-medium">
          <button
            onClick={() => {
              playTickSound();
              setChartView('bar');
            }}
            className={`px-2 py-1 rounded transition-all ${
              chartView === 'bar'
                ? 'bg-white dark:bg-zinc-800 text-zinc-950 dark:text-white shadow-xs font-semibold'
                : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
            }`}
          >
            {lang === 'bn' ? 'বার' : 'Bar'}
          </button>

          <button
            onClick={() => {
              playTickSound();
              setChartView('donut');
            }}
            className={`px-2 py-1 rounded transition-all ${
              chartView === 'donut'
                ? 'bg-white dark:bg-zinc-800 text-zinc-950 dark:text-white shadow-xs font-semibold'
                : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
            }`}
          >
            {lang === 'bn' ? 'অনুপাত' : 'Donut'}
          </button>

          <button
            onClick={() => {
              playTickSound();
              setChartView('curve');
            }}
            className={`px-2 py-1 rounded transition-all ${
              chartView === 'curve'
                ? 'bg-white dark:bg-zinc-800 text-zinc-950 dark:text-white shadow-xs font-semibold'
                : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white'
            }`}
          >
            {lang === 'bn' ? 'কার্ভ' : 'Curve'}
          </button>
        </div>
      </div>

      {/* Canvas */}
      <div className="mt-3 h-48 sm:h-52 w-full">
        
        {chartView === 'bar' && (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={barData} margin={{ top: 10, right: 10, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={isDark ? '#27272a' : '#f4f4f5'} />
              <XAxis
                dataKey="name"
                tick={{ fill: isDark ? '#71717a' : '#71717a', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                tick={{ fill: isDark ? '#71717a' : '#71717a', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={false}
                tickLine={false}
                tickFormatter={(val) => `৳${val}`}
              />
              <Tooltip content={<CustomBarTooltip />} />
              <Bar dataKey="cost" radius={[4, 4, 0, 0]} maxBarSize={32}>
                {barData.map((entry, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={entry.isActive ? entry.color : (isDark ? '#27272a' : '#e4e4e7')}
                    opacity={entry.isActive ? 1 : 0.3}
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}

        {chartView === 'donut' && (
          <div className="flex items-center justify-center h-full gap-4">
            <div className="h-44 w-44">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Tooltip content={<CustomDonutTooltip />} />
                  <Pie
                    data={donutData}
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={70}
                    paddingAngle={2}
                    dataKey="value"
                  >
                    {donutData.map((entry, index) => (
                      <Cell key={`donut-${index}`} fill={entry.color} stroke="none" />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="flex flex-col gap-1.5 text-[11px] font-mono text-zinc-700 dark:text-zinc-300">
              {donutData.map((item, idx) => (
                <div key={idx} className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-1.5 truncate font-sans">
                    <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: item.color }}></span>
                    <span className="truncate">{item.name}</span>
                  </div>
                  <span className="font-bold text-zinc-950 dark:text-white shrink-0">
                    {t.currencySymbol}{formatTaka(item.value, lang)}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {chartView === 'curve' && (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={progressionData} margin={{ top: 10, right: 10, left: -15, bottom: 5 }}>
              <defs>
                <linearGradient id="curveGradCompact" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#f59e0b" stopOpacity={0.0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={isDark ? '#27272a' : '#f4f4f5'} />
              <XAxis
                dataKey="label"
                tick={{ fill: isDark ? '#71717a' : '#71717a', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                tick={{ fill: isDark ? '#71717a' : '#71717a', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={false}
                tickLine={false}
                tickFormatter={(val) => `৳${val}`}
                domain={[0, 16]}
              />
              <Tooltip
                formatter={(val) => [`৳${val}/u`, 'রেট']}
                contentStyle={{
                  backgroundColor: '#09090b',
                  border: '1px solid #27272a',
                  borderRadius: '8px',
                  color: '#fff',
                  fontSize: '11px',
                  fontFamily: 'monospace'
                }}
              />
              <Area
                type="stepAfter"
                dataKey="rate"
                stroke="#f59e0b"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#curveGradCompact)"
              />
            </AreaChart>
          </ResponsiveContainer>
        )}

      </div>

    </div>
  );
}

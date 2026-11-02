import React from 'react';
import { translations } from '../utils/translations';
import { Lightbulb, AlertTriangle, CheckCircle, TrendingDown, Flame, ThermometerSnowflake, Power } from 'lucide-react';

export default function EnergySavingTips({ lang }) {
  const t = translations[lang];

  const tips = [
    {
      id: 'slab_trap',
      icon: AlertTriangle,
      titleBn: '৪০১ ইউনিট স্ল্যাব ট্র্যাপ থেকে সাবধান!',
      titleEn: 'Beware the 401+ Unit Tariff Jump!',
      descBn: '৪০১ ইউনিটে পৌঁছালেই রেট ৮.০২ টাকা থেকে এক লাফে ১২.৬৭ টাকা হয়ে যায় (৫৮% বৃদ্ধি!)। তাই আপনার মাসিক রিডিং যদি ৩৮০-৩৯০ ইউনিটে থাকে, তবে একটু সতর্ক হয়ে ৪০০ ইউনিটের নিচে রাখুন—এতে প্রতি মাসে ১,০০০ থেকে ১,৫০০ টাকা অনায়াসেই বেঁচে যাবে।',
      descEn: 'Once usage exceeds 400 units, the tariff jumps from ৳8.02 straight to ৳12.67 per unit (a 58% spike!). Keeping your consumption under 400 units can save you ৳1,000–৳1,500 every month.'
    },
    {
      id: 'ac_temp',
      icon: ThermometerSnowflake,
      titleBn: 'এসি ২৫° সেলসিয়াসে চালান',
      titleEn: 'Set Air Conditioner to 25°C',
      descBn: 'এসি ১৮° বা ২০° এ না চালিয়ে ২৫°-২৬° সেলসিয়াসে রাখলে প্রতি ১ ডিগ্রির জন্য প্রায় ৬% বিদ্যুৎ সাশ্রয় হয়। সাথে সিলিং ফ্যান মাঝারি গতিতে চালু রাখলে ঘর দ্রুত ঠান্ডা ও আরামদায়ক থাকে।',
      descEn: 'Setting your AC at 25°C–26°C instead of 18°C saves around 6% energy per degree. Using a ceiling fan on low speed alongside distributes chilled air faster.'
    },
    {
      id: 'lifeline',
      icon: CheckCircle,
      titleBn: 'লাইফলাইন গ্রাহক সুবিধা (০-৫০ ইউনিট)',
      titleEn: 'Lifeline Special Privilege (0–50 Units)',
      descBn: 'যদি মাসিক খরচ ৫০ ইউনিটের মধ্যে রাখা সম্ভব হয়, তবে ফ্ল্যাট ৪.৬৩ টাকা রেটে বিল পাওয়া যায়। ৫০ পার হয়ে ৫১ হলেই রেট বেড়ে ৫.২৬ টাকা হয়ে যাবে এবং লাইফলাইন সুবিধা বাতিল হবে।',
      descEn: 'Consuming up to 50 units qualifies you for the lowest subsidized lifeline rate of ৳4.63/unit. Exceeding even by 1 unit triggers normal Step 1 pricing.'
    },
    {
      id: 'vampire',
      icon: Power,
      titleBn: 'ভ্যাম্পায়ার পাওয়ার বন্ধ করুন',
      titleEn: 'Eliminate Phantom / Standby Power',
      descBn: 'টিভি, মাইক্রোওয়েভ, ওয়াইফাই রাউটার বা মোবাইল চার্জার সুইচে লাগিয়ে রাখলে স্ট্যান্ডবাই মোডে ৫-১০% বিদ্যুৎ অপচয় হয়। ব্যবহার শেষে মূল সুইচ বন্ধ করে রাখুন।',
      descEn: 'Appliances in standby mode continue drawing vampire power. Switching them completely off at the wall socket reduces idle waste by 5% to 10%.'
    },
    {
      id: 'fridge',
      icon: TrendingDown,
      titleBn: 'ফ্রিজের সঠিক ব্যবহার ও অবস্থান',
      titleEn: 'Refrigerator Optimization',
      descBn: 'ফ্রিজ দেয়াল থেকে অন্তত ৪-৬ ইঞ্চি দূরে রাখুন যাতে কম্প্রেসর বাতাস পায়। গরম খাবার ফ্রিজে ঢুকাবেন না এবং ডোর গ্যাস্কেট ভালো আছে কিনা পরীক্ষা করুন।',
      descEn: 'Keep the refrigerator at least 4–6 inches from walls for adequate compressor heat exchange. Never put hot food inside and check door seals regularly.'
    },
    {
      id: 'iron_pump',
      icon: Flame,
      titleBn: 'ইস্ত্রি ও পানির পাম্প ব্যবহারে কৌশল',
      titleEn: 'High Wattage Appliance Strategy',
      descBn: 'প্রতিদিন অল্প অল্প কাপড় ইস্ত্রি না করে সপ্তাহের সব কাপড় একসাথে ইস্ত্রি করুন। এতে আয়রন গরম হওয়ার প্রাথমিক বাড়তি ওয়াট খরচ বেঁচে যায়। পানির পাম্প একবারে ট্যাংক ফুল করার জন্য ব্যবহার করুন।',
      descEn: 'Ironing bulk clothes once a week instead of daily saves repeated warm-up cycles. Run water pump in full cycles rather than multiple short bursts.'
    }
  ];

  return (
    <div className="space-y-6">
      
      {/* Title */}
      <div className="pb-4 border-b border-zinc-100 dark:border-white/[0.06]">
        <h3 className="text-base sm:text-lg font-extrabold text-zinc-950 dark:text-white flex items-center gap-2">
          <Lightbulb className="w-5 h-5 text-amber-500 fill-amber-500" />
          <span>{t.savingTipsTitle}</span>
        </h3>
        <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
          {t.savingTipsSubtitle}
        </p>
      </div>

      {/* Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
        {tips.map((item) => {
          const Icon = item.icon;
          const title = lang === 'bn' ? item.titleBn : item.titleEn;
          const desc = lang === 'bn' ? item.descBn : item.descEn;

          return (
            <div
              key={item.id}
              className="p-5 rounded-2xl bg-zinc-50/70 dark:bg-zinc-950/70 border border-zinc-200/80 dark:border-white/[0.06] flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center space-x-2.5 mb-2.5">
                  <div className="w-8 h-8 rounded-xl bg-white dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 flex items-center justify-center shrink-0 shadow-sm">
                    <Icon className="w-4 h-4" />
                  </div>
                  <h4 className="font-bold text-zinc-900 dark:text-white text-sm">
                    {title}
                  </h4>
                </div>

                <p className="text-xs text-zinc-600 dark:text-zinc-400 leading-relaxed">
                  {desc}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-zinc-200/60 dark:border-white/[0.04] text-[10px] font-mono text-zinc-400 uppercase tracking-wider">
                {lang === 'bn' ? 'সাশ্রয় সম্ভাবনা: উচ্চ' : 'Potential Impact: High'}
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
}

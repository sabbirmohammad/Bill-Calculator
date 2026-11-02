/**
 * Electricity Bill Calculation Utility
 * Based on Bangladesh Energy Regulatory Commission (BERC) Residential (LT-A) Tariff
 * 
 * Rates:
 * • 0–50 Units (Lifeline): ৳4.63 per unit
 * • 0–75 Units (Step 1): ৳5.26 per unit
 * • 76–200 Units (Step 2): ৳7.20 per unit
 * • 201–300 Units (Step 3): ৳7.59 per unit
 * • 301–400 Units (Step 4): ৳8.02 per unit
 * • 401–600 Units (Step 5): ৳12.67 per unit
 * • Above 600 Units (Step 6): ৳14.61 per unit
 */

export const TARIFF_RATES = [
  {
    id: 'lifeline',
    stepNumber: 0,
    titleBn: 'লাইফলাইন',
    rangeBn: '০ – ৫০ ইউনিট',
    titleEn: 'Lifeline',
    rangeEn: '0 – 50 Units',
    rate: 4.63,
    min: 0,
    max: 50,
    capacity: 50,
    isLifeline: true,
    color: 'emerald',
    badge: 'লাইফলাইন বিশেষ ছাড়'
  },
  {
    id: 'step1',
    stepNumber: 1,
    titleBn: 'ধাপ ১',
    rangeBn: '০ – ৭৫ ইউনিট',
    titleEn: 'Step 1',
    rangeEn: '0 – 75 Units',
    rate: 5.26,
    min: 0,
    max: 75,
    capacity: 75,
    color: 'cyan',
    badge: 'বেসিক ধাপ'
  },
  {
    id: 'step2',
    stepNumber: 2,
    titleBn: 'ধাপ ২',
    rangeBn: '৭৬ – ২০০ ইউনিট',
    titleEn: 'Step 2',
    rangeEn: '76 – 200 Units',
    rate: 7.20,
    min: 75,
    max: 200,
    capacity: 125,
    color: 'blue',
    badge: 'মধ্যম ব্যবহার'
  },
  {
    id: 'step3',
    stepNumber: 3,
    titleBn: 'ধাপ ৩',
    rangeBn: '২০১ – ৩০০ ইউনিট',
    titleEn: 'Step 3',
    rangeEn: '201 – 300 Units',
    rate: 7.59,
    min: 200,
    max: 300,
    capacity: 100,
    color: 'indigo',
    badge: 'স্বাভাবিক ব্যবহার'
  },
  {
    id: 'step4',
    stepNumber: 4,
    titleBn: 'ধাপ ৪',
    rangeBn: '৩০১ – ৪০০ ইউনিট',
    titleEn: 'Step 4',
    rangeEn: '301 – 400 Units',
    rate: 8.02,
    min: 300,
    max: 400,
    capacity: 100,
    color: 'amber',
    badge: 'উচ্চ ব্যবহার'
  },
  {
    id: 'step5',
    stepNumber: 5,
    titleBn: 'ধাপ ৫',
    rangeBn: '৪০১ – ৬০০ ইউনিট',
    titleEn: 'Step 5',
    rangeEn: '401 – 600 Units',
    rate: 12.67,
    min: 400,
    max: 600,
    capacity: 200,
    color: 'orange',
    badge: 'অতিরিক্ত ব্যবহার'
  },
  {
    id: 'step6',
    stepNumber: 6,
    titleBn: 'ধাপ ৬',
    rangeBn: '৬০০ ইউনিটের উপরে',
    titleEn: 'Step 6',
    rangeEn: 'Above 600 Units',
    rate: 14.61,
    min: 600,
    max: Infinity,
    capacity: Infinity,
    color: 'rose',
    badge: 'সর্বোচ্চ স্ল্যাব'
  }
];

export function calculateBill(units, options = {}) {
  const {
    demandChargePerKw = 42,   // ৳42 per KW (DESCO/DPDC residential standard)
    meterRent = 10,           // ৳10 single phase
    vatPercent = 5,           // 5% standard VAT
    sanctionedLoad = 1,       // 1 KW
    includeAdditional = true, // Whether to add Demand Charge, Meter Rent & VAT
    forceLifelineMode = 'auto' // 'auto' | 'lifeline' | 'standard'
  } = options;

  const validUnits = Math.max(0, parseFloat(units) || 0);

  // Determine if lifeline applies:
  // If 'auto', lifeline applies only when units > 0 and units <= 50.
  // If units > 50, Lifeline does NOT apply in standard BERC rules (it shifts to 0-75 slab @ 5.26).
  let isLifeline = false;
  if (forceLifelineMode === 'lifeline') {
    isLifeline = validUnits <= 50;
  } else if (forceLifelineMode === 'standard') {
    isLifeline = false;
  } else {
    isLifeline = validUnits > 0 && validUnits <= 50;
  }

  let stepsBreakdown = [];
  let energyCharge = 0;

  if (isLifeline && validUnits <= 50) {
    const cost = validUnits * 4.63;
    energyCharge = cost;
    stepsBreakdown.push({
      stepId: 'lifeline',
      stepNumber: 0,
      titleBn: 'লাইফলাইন (০ – ৫০ ইউনিট)',
      titleEn: 'Lifeline (0–50 Units)',
      rangeBn: '০ – ৫০',
      rangeEn: '0 – 50',
      rate: 4.63,
      units: validUnits,
      maxUnits: 50,
      cost: cost,
      isActive: validUnits > 0,
      isFullyUsed: validUnits === 50,
      progressPercent: (validUnits / 50) * 100,
      color: 'emerald'
    });
  } else {
    // Normal 6-step calculation
    let remaining = validUnits;

    const normalTiers = [
      { id: 'step1', num: 1, titleBn: 'ধাপ ১ (০ – ৭৫ ইউনিট)', titleEn: 'Step 1 (0–75 Units)', rangeBn: '০ – ৭৫', rangeEn: '0 – 75', maxUnits: 75, rate: 5.26, color: 'cyan' },
      { id: 'step2', num: 2, titleBn: 'ধাপ ২ (৭৬ – ২০০ ইউনিট)', titleEn: 'Step 2 (76–200 Units)', rangeBn: '৭৬ – ২০০', rangeEn: '76 – 200', maxUnits: 125, rate: 7.20, color: 'blue' },
      { id: 'step3', num: 3, titleBn: 'ধাপ ৩ (২০১ – ৩০০ ইউনিট)', titleEn: 'Step 3 (201–300 Units)', rangeBn: '২০১ – ৩০০', rangeEn: '201 – 300', maxUnits: 100, rate: 7.59, color: 'indigo' },
      { id: 'step4', num: 4, titleBn: 'ধাপ ৪ (৩০১ – ৪০০ ইউনিট)', titleEn: 'Step 4 (301–400 Units)', rangeBn: '৩০১ – ৪০০', rangeEn: '301 – 400', maxUnits: 100, rate: 8.02, color: 'amber' },
      { id: 'step5', num: 5, titleBn: 'ধাপ ৫ (৪০১ – ৬০০ ইউনিট)', titleEn: 'Step 5 (401–600 Units)', rangeBn: '৪০১ – ৬০০', rangeEn: '401 – 600', maxUnits: 200, rate: 12.67, color: 'orange' },
      { id: 'step6', num: 6, titleBn: 'ধাপ ৬ (৬০০ ইউনিটের উপরে)', titleEn: 'Step 6 (> 600 Units)', rangeBn: '৬০০+', rangeEn: '600+', maxUnits: Infinity, rate: 14.61, color: 'rose' }
    ];

    for (const step of normalTiers) {
      if (remaining <= 0) {
        stepsBreakdown.push({
          stepId: step.id,
          stepNumber: step.num,
          titleBn: step.titleBn,
          titleEn: step.titleEn,
          rangeBn: step.rangeBn,
          rangeEn: step.rangeEn,
          rate: step.rate,
          units: 0,
          maxUnits: step.maxUnits,
          cost: 0,
          isActive: false,
          isFullyUsed: false,
          progressPercent: 0,
          color: step.color
        });
        continue;
      }

      const allocated = Math.min(remaining, step.maxUnits);
      const cost = allocated * step.rate;
      energyCharge += cost;
      remaining -= allocated;

      const progress = step.maxUnits === Infinity ? 100 : Math.min(100, (allocated / step.maxUnits) * 100);

      stepsBreakdown.push({
        stepId: step.id,
        stepNumber: step.num,
        titleBn: step.titleBn,
        titleEn: step.titleEn,
        rangeBn: step.rangeBn,
        rangeEn: step.rangeEn,
        rate: step.rate,
        units: allocated,
        maxUnits: step.maxUnits,
        cost: cost,
        isActive: allocated > 0,
        isFullyUsed: allocated === step.maxUnits,
        progressPercent: progress,
        color: step.color
      });
    }
  }

  // Demand charge, meter rent, and VAT
  const demandCharge = includeAdditional ? (demandChargePerKw * (parseFloat(sanctionedLoad) || 1)) : 0;
  const rent = includeAdditional ? (parseFloat(meterRent) || 0) : 0;
  const subtotalBeforeVat = energyCharge + demandCharge + rent;
  const vatAmount = includeAdditional ? (subtotalBeforeVat * ((parseFloat(vatPercent) || 5) / 100)) : 0;
  const totalBill = subtotalBeforeVat + vatAmount;

  // Average cost per unit
  const effectiveRate = validUnits > 0 ? (totalBill / validUnits) : 0;
  const energyOnlyRate = validUnits > 0 ? (energyCharge / validUnits) : 0;

  // Comparison: What if this was 1 unit higher or lower?
  // Highest active tier rate:
  const activeTiers = stepsBreakdown.filter(s => s.units > 0);
  const highestActiveTier = activeTiers.length > 0 ? activeTiers[activeTiers.length - 1] : stepsBreakdown[0];

  return {
    units: validUnits,
    isLifeline,
    energyCharge,
    demandCharge,
    meterRent: rent,
    subtotalBeforeVat,
    vatPercent: parseFloat(vatPercent) || 5,
    vatAmount,
    totalBill,
    effectiveRate,
    energyOnlyRate,
    highestActiveRate: highestActiveTier ? highestActiveTier.rate : 5.26,
    stepsBreakdown
  };
}

/**
 * Reverse Calculation: Budget to Units
 * Estimates how many units can be consumed within a target monthly budget (BDT).
 */
export function estimateUnitsFromBudget(targetBudget, options = {}) {
  const {
    demandChargePerKw = 42,
    meterRent = 10,
    vatPercent = 5,
    sanctionedLoad = 1,
    includeAdditional = true
  } = options;

  const budget = Math.max(0, parseFloat(targetBudget) || 0);
  if (budget <= 0) return 0;

  let fixedCharges = 0;
  if (includeAdditional) {
    fixedCharges = (demandChargePerKw * sanctionedLoad) + meterRent;
  }
  const vatMultiplier = includeAdditional ? (1 + (vatPercent / 100)) : 1;

  // Target Energy Charge:
  const targetEnergyBudget = (budget / vatMultiplier) - fixedCharges;
  if (targetEnergyBudget <= 0) return 0;

  // 1. Check if it fits in Lifeline: max 50 * 4.63 = 231.50
  const maxLifelineCost = 50 * 4.63;
  if (targetEnergyBudget <= maxLifelineCost) {
    return Math.floor(targetEnergyBudget / 4.63);
  }

  // 2. Normal steps:
  // Step 1: 75 * 5.26 = 394.50
  const step1Cost = 75 * 5.26;
  if (targetEnergyBudget <= step1Cost) {
    return Math.floor(targetEnergyBudget / 5.26);
  }

  let remEnergy = targetEnergyBudget - step1Cost;
  let totalUnits = 75;

  // Step 2: 125 * 7.20 = 900.00
  const step2Cost = 125 * 7.20;
  if (remEnergy <= step2Cost) {
    return Math.floor(totalUnits + (remEnergy / 7.20));
  }
  remEnergy -= step2Cost;
  totalUnits += 125; // 200

  // Step 3: 100 * 7.59 = 759.00
  const step3Cost = 100 * 7.59;
  if (remEnergy <= step3Cost) {
    return Math.floor(totalUnits + (remEnergy / 7.59));
  }
  remEnergy -= step3Cost;
  totalUnits += 100; // 300

  // Step 4: 100 * 8.02 = 802.00
  const step4Cost = 100 * 8.02;
  if (remEnergy <= step4Cost) {
    return Math.floor(totalUnits + (remEnergy / 8.02));
  }
  remEnergy -= step4Cost;
  totalUnits += 100; // 400

  // Step 5: 200 * 12.67 = 2534.00
  const step5Cost = 200 * 12.67;
  if (remEnergy <= step5Cost) {
    return Math.floor(totalUnits + (remEnergy / 12.67));
  }
  remEnergy -= step5Cost;
  totalUnits += 200; // 600

  // Step 6: >600 @ 14.61
  totalUnits += remEnergy / 14.61;
  return Math.floor(totalUnits);
}

/**
 * Format Currency in Bengali / English
 */
export function formatTaka(amount, lang = 'bn') {
  const num = parseFloat(amount) || 0;
  const formatted = num.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
  if (lang === 'bn') {
    return toBengaliNumber(formatted);
  }
  return formatted;
}

/**
 * Convert English digits to Bengali digits
 */
export function toBengaliNumber(str) {
  if (str === null || str === undefined) return '';
  const numStr = String(str);
  const bengaliDigits = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];
  return numStr.replace(/[0-9]/g, match => bengaliDigits[parseInt(match, 10)]);
}

/**
 * Convert Bengali digits to English digits
 */
export function toEnglishNumber(str) {
  if (str === null || str === undefined) return '';
  const bnMap = {
    '০': '0', '১': '1', '২': '2', '৩': '3', '৪': '4',
    '৫': '5', '৬': '6', '৭': '7', '৮': '8', '৯': '9'
  };
  return String(str).replace(/[০-৯]/g, (char) => bnMap[char] || char);
}

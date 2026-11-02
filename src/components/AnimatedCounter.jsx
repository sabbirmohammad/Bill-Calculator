import React, { useEffect, useState, useRef } from 'react';
import { toBengaliNumber } from '../utils/calculator';

export default function AnimatedCounter({ value, duration = 600, lang = 'bn', decimals = 2, prefix = '৳' }) {
  const targetValue = parseFloat(value) || 0;
  const [displayValue, setDisplayValue] = useState(targetValue);
  const prevValueRef = useRef(targetValue);

  useEffect(() => {
    const startValue = prevValueRef.current;
    const endValue = targetValue;
    
    // If the difference is tiny, set immediately
    if (Math.abs(endValue - startValue) < 0.001) {
      setDisplayValue(endValue);
      prevValueRef.current = endValue;
      return;
    }

    let startTimestamp = null;
    let animId = null;

    const step = (timestamp) => {
      if (!startTimestamp) startTimestamp = timestamp;
      const progress = Math.min((timestamp - startTimestamp) / duration, 1);
      
      // Smooth cubic ease out
      const easeOutProgress = 1 - Math.pow(1 - progress, 3);
      const current = startValue + (endValue - startValue) * easeOutProgress;
      
      setDisplayValue(current);

      if (progress < 1) {
        animId = window.requestAnimationFrame(step);
      } else {
        setDisplayValue(endValue);
        prevValueRef.current = endValue;
      }
    };

    animId = window.requestAnimationFrame(step);

    return () => {
      if (animId) {
        window.cancelAnimationFrame(animId);
      }
    };
  }, [targetValue, duration]);

  const formatted = displayValue.toLocaleString('en-IN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });

  const finalString = lang === 'bn' ? toBengaliNumber(formatted) : formatted;

  return (
    <span className="tabular-nums font-mono-nums">
      {prefix}{finalString}
    </span>
  );
}

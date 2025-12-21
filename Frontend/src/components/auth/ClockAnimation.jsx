import { useState, useEffect } from "react";

// Clock Animation Component
export default function ClockAnimation() {
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const seconds = time.getSeconds() * 6;
  const minutes = time.getMinutes() * 6 + time.getSeconds() * 0.1;
  const hours = (time.getHours() % 12) * 30 + time.getMinutes() * 0.5;

  return (
    <div className="clock-container">
      <svg width="200" height="200" viewBox="0 0 200 200">
        <circle
          cx="100"
          cy="100"
          r="90"
          fill="rgba(255,255,255,0.1)"
          stroke="white"
          strokeWidth="4"
        />
        {[...Array(12)].map((_, i) => (
          <line
            key={i}
            x1="100"
            y1="20"
            x2="100"
            y2="30"
            stroke="white"
            strokeWidth="3"
            transform={`rotate(${i * 30} 100 100)`}
          />
        ))}
        <line
          x1="100"
          y1="100"
          x2="100"
          y2="50"
          stroke="white"
          strokeWidth="6"
          strokeLinecap="round"
          transform={`rotate(${hours} 100 100)`}
        />
        <line
          x1="100"
          y1="100"
          x2="100"
          y2="35"
          stroke="white"
          strokeWidth="4"
          strokeLinecap="round"
          transform={`rotate(${minutes} 100 100)`}
        />
        <line
          x1="100"
          y1="100"
          x2="100"
          y2="25"
          stroke="rgba(255,100,100,0.9)"
          strokeWidth="2"
          strokeLinecap="round"
          transform={`rotate(${seconds} 100 100)`}
        />
        <circle cx="100" cy="100" r="8" fill="white" />
      </svg>
    </div>
  );
}

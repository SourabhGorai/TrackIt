import { useState, useEffect } from "react";
import { Rocket } from "lucide-react";

export const RocketAnimation = () => {
  return (
    <div className="bug-container">
      <svg width="200" height="200" viewBox="0 0 200 200">
        <defs>
          <linearGradient id="rocketGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style={{ stopColor: '#FFD700', stopOpacity: 1 }} />
            <stop offset="100%" style={{ stopColor: '#FFA500', stopOpacity: 1 }} />
          </linearGradient>
        </defs>
        
        <g transform="translate(-47, 0)">
          <animateTransform
            attributeName="transform"
            type="translate"
            values="-20,20; -20,-5; -20,20"
            dur="2s"
            repeatCount="indefinite"
            additive="sum"
          />
          
          {/* Rocket body */}
          <path d="M 100 40 L 85 100 L 85 120 L 100 130 L 115 120 L 115 100 Z" fill="url(#rocketGradient)" stroke="#fff" strokeWidth="2" />
          
          {/* Rocket window */}
          <circle cx="100" cy="70" r="12" fill="#333" opacity="0.6" stroke="#fff" strokeWidth="2" />
          
          {/* Left fin */}
          <path d="M 85 100 L 70 120 L 85 120 Z" fill="#FF6B6B" stroke="#fff" strokeWidth="1.5" />
          
          {/* Right fin */}
          <path d="M 115 100 L 130 120 L 115 120 Z" fill="#FF6B6B" stroke="#fff" strokeWidth="1.5" />
          
          {/* Rocket top */}
          <path d="M 100 40 L 85 60 L 115 60 Z" fill="#FF4444" stroke="#fff" strokeWidth="2" />
          
          {/* Fire/exhaust */}
          <g opacity="0.8">
            <ellipse cx="100" cy="135" rx="10" ry="15" fill="#FF6B6B">
              <animate attributeName="ry" values="15;25;15" dur="0.3s" repeatCount="indefinite" />
            </ellipse>
            <ellipse cx="100" cy="140" rx="8" ry="12" fill="#FFA500">
              <animate attributeName="ry" values="12;20;12" dur="0.4s" repeatCount="indefinite" />
            </ellipse>
            <ellipse cx="100" cy="145" rx="6" ry="10" fill="#FFD700">
              <animate attributeName="ry" values="10;15;10" dur="0.5s" repeatCount="indefinite" />
            </ellipse>
          </g>
        </g>
        
        {/* Stars */}
        <circle cx="10" cy="60" r="2" fill="#fff">
          <animate attributeName="opacity" values="0.3;1;0.3" dur="1.5s" repeatCount="indefinite" />
        </circle>
        <circle cx="60" cy="80" r="2" fill="#fff">
          <animate attributeName="opacity" values="1;0.3;1" dur="2s" repeatCount="indefinite" />
        </circle>
        <circle cx="50" cy="140" r="2" fill="#fff">
          <animate attributeName="opacity" values="0.5;1;0.5" dur="1.8s" repeatCount="indefinite" />
        </circle>
        <circle cx="30" cy="90" r="2" fill="#fff">
          <animate attributeName="opacity" values="0.5;1;0.5" dur="1.8s" repeatCount="indefinite" />
        </circle>
        <circle cx="80" cy="20" r="2" fill="#fff">
          <animate attributeName="opacity" values="0.7;1;0.8" dur="1.8s" repeatCount="indefinite" />
        </circle>
      </svg>
    </div>
  );
};

export default RocketAnimation;
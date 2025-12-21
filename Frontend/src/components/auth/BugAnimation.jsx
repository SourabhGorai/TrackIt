import { useState, useEffect } from "react";
import { Bug } from "lucide-react";

// Bug Animation Component
export default function BugAnimation({ isSignUpMode }) {
  const [position, setPosition] = useState({ x: -200, y: 0 });
  const [color, setColor] = useState("white");
  const [rotation, setRotation] = useState(0);

  useEffect(() => {
    if (isSignUpMode) return; // Don't animate if in sign-up mode
    
    let walkProgress = 0;
    const walkSpeed = 0.8;
    const walkDistance = 400;
    
    const interval = setInterval(() => {
      walkProgress += walkSpeed;
      
      // Make the bug walk back and forth
      const x = Math.sin(walkProgress / 100) * walkDistance;
      const y = Math.sin(walkProgress / 50) * 10; // Slight vertical bobbing
      
      // Determine if bug is on blue or white background
      // Blue area is on the left side of the screen
      const screenWidth = window.innerWidth;
      const bugScreenX = screenWidth * 0.25 + x; // Approximate position on screen
      
      if (bugScreenX < screenWidth * 0.5) {
        setColor("white"); // On blue background
      } else {
        setColor("#4481eb"); // On white background
      }
      
      // Add walking animation by rotating slightly
      const walkCycle = Math.sin(walkProgress / 10) * 5;
      setRotation(walkCycle);
      
      setPosition({ x, y });
    }, 50);

    return () => clearInterval(interval);
  }, [isSignUpMode]);

  if (isSignUpMode) return null; // Don't render if in sign-up mode

  return (
    <div
      className="bug-container"
      style={{ 
        transform: `translate(${position.x}px, ${position.y}px) rotate(${rotation}deg)`,
        transition: 'transform 0.05s linear'
      }}
    >
      <Bug size={120} color={color} strokeWidth={1.5} />
    </div>
  );
}
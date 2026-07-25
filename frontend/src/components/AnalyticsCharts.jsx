import React, { useState } from 'react';

// SVG Bar Chart Component for Monthly Trends
export function BorrowingActivityChart({ data }) {
  const [hoveredBar, setHoveredBar] = useState(null);

  if (!data || Object.keys(data).length === 0) {
    return <div className="no-data">No borrowing trends data available</div>;
  }

  const entries = Object.entries(data);
  const values = entries.map(([, v]) => v);
  const maxValue = Math.max(...values, 5); // default grid scale max to at least 5

  const width = 500;
  const height = 260;
  const paddingLeft = 40;
  const paddingRight = 20;
  const paddingTop = 30;
  const paddingBottom = 40;

  const chartWidth = width - paddingLeft - paddingRight;
  const chartHeight = height - paddingTop - paddingBottom;

  const barWidth = (chartWidth / entries.length) * 0.6;
  const gap = (chartWidth / entries.length) * 0.4;

  return (
    <div className="chart-wrapper">
      <svg viewBox={`0 0 ${width} ${height}`} className="svg-chart bar-chart">
        <defs>
          <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#FDC83A" />
            <stop offset="100%" stopColor="#821124" />
          </linearGradient>
          <linearGradient id="barGradHover" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#FFE082" />
            <stop offset="100%" stopColor="#A8283B" />
          </linearGradient>
          <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feComposite in="SourceGraphic" in2="blur" operator="over" />
          </filter>
        </defs>

        {/* Y Axis Grid Lines */}
        {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
          const y = paddingTop + chartHeight * (1 - ratio);
          const gridVal = Math.round(maxValue * ratio);
          return (
            <g key={i} className="grid-line-group">
              <line
                x1={paddingLeft}
                y1={y}
                x2={width - paddingRight}
                y2={y}
                stroke="var(--color-border)"
                strokeDasharray="4 4"
                strokeWidth={1}
              />
              <text
                x={paddingLeft - 10}
                y={y + 4}
                textAnchor="end"
                fontSize="10"
                fill="var(--text-secondary)"
                fontWeight="500"
              >
                {gridVal}
              </text>
            </g>
          );
        })}

        {/* Bars */}
        {entries.map(([label, val], idx) => {
          const barHeight = (val / maxValue) * chartHeight;
          const x = paddingLeft + idx * (barWidth + gap) + gap / 2;
          const y = height - paddingBottom - barHeight;

          const isHovered = hoveredBar === idx;

          return (
            <g
              key={idx}
              onMouseEnter={() => setHoveredBar(idx)}
              onMouseLeave={() => setHoveredBar(null)}
              style={{ cursor: 'pointer' }}
            >
              {/* Animated Rect */}
              <rect
                x={x}
                y={y}
                width={barWidth}
                height={Math.max(barHeight, 2)}
                rx="6"
                ry="6"
                fill={isHovered ? 'url(#barGradHover)' : 'url(#barGrad)'}
                filter={isHovered ? 'url(#glow)' : 'none'}
                style={{
                  transition: 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)',
                  transformOrigin: `${x + barWidth / 2}px ${height - paddingBottom}px`,
                  transform: isHovered ? 'scaleX(1.05)' : 'none',
                }}
              />

              {/* Bar Label */}
              <text
                x={x + barWidth / 2}
                y={height - paddingBottom + 20}
                textAnchor="middle"
                fontSize="11"
                fill={isHovered ? 'var(--text-primary)' : 'var(--text-secondary)'}
                fontWeight={isHovered ? '600' : '500'}
                style={{ transition: 'color 0.2s ease' }}
              >
                {label}
              </text>

              {/* Tooltip text when hovered */}
              {isHovered && (
                <g>
                  <rect
                    x={x + barWidth / 2 - 25}
                    y={y - 30}
                    width="50"
                    height="22"
                    rx="4"
                    fill="#1A1A1A"
                    opacity="0.9"
                  />
                  <text
                    x={x + barWidth / 2}
                    y={y - 15}
                    textAnchor="middle"
                    fill="#FFFFFF"
                    fontSize="11"
                    fontWeight="700"
                  >
                    {val}
                  </text>
                </g>
              )}
            </g>
          );
        })}

        {/* X Axis Line */}
        <line
          x1={paddingLeft}
          y1={height - paddingBottom}
          x2={width - paddingRight}
          y2={height - paddingBottom}
          stroke="var(--color-border)"
          strokeWidth="1.5"
        />
      </svg>
    </div>
  );
}

// SVG Donut/Pie Chart Component for Category distribution
export function CategoryDistributionChart({ data }) {
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || Object.keys(data).length === 0) {
    return <div className="no-data">No category data available</div>;
  }

  const entries = Object.entries(data);
  const total = entries.reduce((sum, [, val]) => sum + val, 0);

  // Modern soft palette colors
  const colors = [
    '#821124', // Maroon
    '#FDC83A', // Gold
    '#2A9D8F', // Teal
    '#E76F51', // Terracotta
    '#457B9D', // Steel Blue
    '#A8DADC', // Mint
    '#E9C46A', // Sand
  ];

  // Donut Geometry
  const size = 220;
  const center = size / 2;
  const radius = 70;
  const strokeWidth = 24;
  const circumference = 2 * Math.PI * radius; // ~439.8

  let accumulatedPercent = 0;

  return (
    <div className="donut-chart-container">
      <div className="donut-svg-wrapper">
        <svg viewBox={`0 0 ${size} ${size}`} className="svg-chart donut-chart">
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke="var(--color-border)"
            strokeWidth={strokeWidth}
          />
          {entries.map(([label, val], idx) => {
            const percentage = val / total;
            const dashArray = `${percentage * circumference} ${circumference}`;
            const dashOffset = -accumulatedPercent * circumference;
            accumulatedPercent += percentage;

            const isHovered = hoveredIdx === idx;
            const color = colors[idx % colors.length];

            return (
              <circle
                key={idx}
                cx={center}
                cy={center}
                r={radius}
                fill="none"
                stroke={color}
                strokeWidth={isHovered ? strokeWidth + 4 : strokeWidth}
                strokeDasharray={dashArray}
                strokeDashoffset={dashOffset}
                transform={`rotate(-90 ${center} ${center})`}
                strokeLinecap="round"
                onMouseEnter={() => setHoveredIdx(idx)}
                onMouseLeave={() => setHoveredIdx(null)}
                style={{
                  transition: 'stroke-width 0.3s ease, stroke 0.3s ease, filter 0.3s ease',
                  cursor: 'pointer',
                  filter: isHovered ? `drop-shadow(0px 0px 8px ${color}88)` : 'none',
                }}
              />
            );
          })}

          {/* Center text showing values */}
          <g className="donut-center-text">
            <text
              x={center}
              y={center - 6}
              textAnchor="middle"
              fontSize="12"
              fill="var(--text-secondary)"
              fontWeight="600"
            >
              {hoveredIdx !== null ? entries[hoveredIdx][0] : 'TOTAL'}
            </text>
            <text
              x={center}
              y={center + 14}
              textAnchor="middle"
              fontSize="20"
              fill="var(--text-primary)"
              fontWeight="800"
            >
              {hoveredIdx !== null ? `${entries[hoveredIdx][1]} books` : `${total} books`}
            </text>
          </g>
        </svg>
      </div>

      {/* Legend list */}
      <div className="donut-legend">
        {entries.map(([label, val], idx) => {
          const color = colors[idx % colors.length];
          const isHovered = hoveredIdx === idx;
          const percentage = ((val / total) * 100).toFixed(0);

          return (
            <div
              key={idx}
              className={`legend-item ${isHovered ? 'highlight' : ''}`}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '6px 12px',
                borderRadius: '6px',
                background: isHovered ? 'rgba(0,0,0,0.03)' : 'transparent',
                transition: 'background 0.2s ease',
                cursor: 'pointer',
              }}
            >
              <span
                style={{
                  width: '12px',
                  height: '12px',
                  borderRadius: '50%',
                  backgroundColor: color,
                  marginRight: '8px',
                  display: 'inline-block',
                }}
              />
              <span className="legend-label" style={{ fontSize: '12px', color: 'var(--text-primary)', flex: 1 }}>
                {label}
              </span>
              <span className="legend-value" style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 'bold' }}>
                {val} ({percentage}%)
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

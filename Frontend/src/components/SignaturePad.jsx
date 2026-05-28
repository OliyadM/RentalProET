import { useRef, useEffect, useState, useCallback } from "react";
import { Trash2 } from "lucide-react";

/**
 * Canvas-based signature pad.
 *
 * Props:
 *   onChange(dataUrl | null) — called whenever the drawing changes.
 *                              Passes null when the pad is cleared.
 *   disabled                — disables drawing (read-only display mode)
 *   existingSignature       — Base64 data URL to display (read-only)
 */
export default function SignaturePad({ onChange, disabled = false, existingSignature = null }) {
  const canvasRef = useRef(null);
  const drawing   = useRef(false);
  const lastPos   = useRef({ x: 0, y: 0 });
  const [isEmpty, setIsEmpty] = useState(true);

  // ── Initialise canvas ──────────────────────────────────────────────────────
  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx    = canvas.getContext("2d");

    // Match the canvas pixel size to its CSS size for crisp rendering
    const rect = canvas.getBoundingClientRect();
    canvas.width  = rect.width  * window.devicePixelRatio;
    canvas.height = rect.height * window.devicePixelRatio;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

    ctx.strokeStyle = "#1e3a5f";
    ctx.lineWidth   = 2.5;
    ctx.lineCap     = "round";
    ctx.lineJoin    = "round";

    if (existingSignature) {
      const img = new Image();
      img.onload = () => ctx.drawImage(img, 0, 0, rect.width, rect.height);
      img.src    = existingSignature;
      setIsEmpty(false);
    }
  }, [existingSignature]);

  // ── Coordinate helpers ─────────────────────────────────────────────────────
  const getPos = (e) => {
    const canvas = canvasRef.current;
    const rect   = canvas.getBoundingClientRect();
    const src    = e.touches ? e.touches[0] : e;
    return {
      x: src.clientX - rect.left,
      y: src.clientY - rect.top,
    };
  };

  // ── Drawing handlers ───────────────────────────────────────────────────────
  const startDraw = useCallback((e) => {
    if (disabled) return;
    e.preventDefault();
    drawing.current = true;
    lastPos.current = getPos(e);
  }, [disabled]);

  const draw = useCallback((e) => {
    if (!drawing.current || disabled) return;
    e.preventDefault();

    const canvas = canvasRef.current;
    const ctx    = canvas.getContext("2d");
    const pos    = getPos(e);

    ctx.beginPath();
    ctx.moveTo(lastPos.current.x, lastPos.current.y);
    ctx.lineTo(pos.x, pos.y);
    ctx.stroke();

    lastPos.current = pos;
    setIsEmpty(false);
  }, [disabled]);

  const endDraw = useCallback((e) => {
    if (!drawing.current) return;
    e.preventDefault();
    drawing.current = false;

    // Notify parent with the current signature as a PNG data URL
    const dataUrl = canvasRef.current.toDataURL("image/png");
    onChange?.(dataUrl);
  }, [onChange]);

  // ── Clear ──────────────────────────────────────────────────────────────────
  const clear = () => {
    const canvas = canvasRef.current;
    const ctx    = canvas.getContext("2d");
    const rect   = canvas.getBoundingClientRect();
    ctx.clearRect(0, 0, rect.width * window.devicePixelRatio, rect.height * window.devicePixelRatio);
    setIsEmpty(true);
    onChange?.(null);
  };

  return (
    <div className="space-y-2">
      <div className={`relative border-2 rounded-lg overflow-hidden
        ${disabled ? "border-gray-200 bg-gray-50" : isEmpty ? "border-dashed border-gray-300 bg-white" : "border-primary bg-white"}`}
        style={{ height: "140px" }}>

        <canvas
          ref={canvasRef}
          style={{ width: "100%", height: "100%", touchAction: "none", cursor: disabled ? "default" : "crosshair" }}
          onMouseDown={startDraw}
          onMouseMove={draw}
          onMouseUp={endDraw}
          onMouseLeave={endDraw}
          onTouchStart={startDraw}
          onTouchMove={draw}
          onTouchEnd={endDraw}
        />

        {/* Placeholder text shown when empty and not disabled */}
        {isEmpty && !disabled && (
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
            <p className="text-gray-300 text-sm select-none">Sign here using your mouse or finger</p>
          </div>
        )}
      </div>

      {/* Clear button — only shown when there's something drawn */}
      {!disabled && !isEmpty && (
        <button
          type="button"
          onClick={clear}
          className="flex items-center gap-1 text-xs text-red-500 hover:text-red-700 transition"
        >
          <Trash2 size={12} /> Clear signature
        </button>
      )}
    </div>
  );
}

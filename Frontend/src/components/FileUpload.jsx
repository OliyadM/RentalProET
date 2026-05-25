import { useState } from "react";
import { Upload, X, FileText, Image, CheckCircle, AlertCircle } from "lucide-react";
import { filesAPI } from "../services/api";

export default function FileUpload({ 
  label, 
  value, 
  onChange, 
  folder = "rentalpro",
  accept = "image/*,.pdf",
  required = false,
  disabled = false,
  helperText = "Max 10MB. Supported: Images, PDF"
}) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [uploadNotConfigured, setUploadNotConfigured] = useState(false);

  const handleFileSelect = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file size (10MB)
    if (file.size > 10 * 1024 * 1024) {
      setError("File size must be less than 10MB");
      return;
    }

    setError(null);
    setUploading(true);

    try {
      const url = await filesAPI.upload(file, folder);
      onChange(url);
      setUploadNotConfigured(false);
    } catch (err) {
      const errorMsg = err.message || "Failed to upload file";
      
      // Check if Cloudinary is not configured
      if (errorMsg.includes("not configured") || errorMsg.includes("Cloudinary")) {
        setUploadNotConfigured(true);
        setError("File upload not configured. Please enter URL manually below.");
      } else {
        setError(errorMsg);
      }
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = () => {
    onChange("");
    setError(null);
  };

  const getFileIcon = () => {
    if (!value) return <Upload size={20} />;
    if (value.includes(".pdf")) return <FileText size={20} />;
    return <Image size={20} />;
  };

  const getFileName = () => {
    if (!value) return null;
    try {
      const url = new URL(value);
      const parts = url.pathname.split("/");
      return parts[parts.length - 1];
    } catch {
      return "Uploaded file";
    }
  };

  // If upload is not configured, show text input as fallback
  if (uploadNotConfigured) {
    return (
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
        <input
          type="url"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          required={required}
          placeholder="https://example.com/document.pdf"
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500"
        />
        <p className="mt-1 text-xs text-amber-600">
          ⚠️ File upload service not configured. Enter document URL manually.
        </p>
      </div>
    );
  }

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label} {required && <span className="text-red-500">*</span>}
      </label>

      {!value ? (
        <div className="relative">
          <input
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            disabled={disabled || uploading}
            className="hidden"
            id={`file-upload-${label}`}
          />
          <label
            htmlFor={`file-upload-${label}`}
            className={`
              flex items-center justify-center gap-2 w-full px-4 py-3 
              border-2 border-dashed border-gray-300 rounded-lg 
              cursor-pointer transition
              ${uploading || disabled 
                ? "bg-gray-50 cursor-not-allowed opacity-50" 
                : "hover:border-primary hover:bg-blue-50"
              }
            `}
          >
            {uploading ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-primary"></div>
                <span className="text-sm text-gray-600">Uploading...</span>
              </>
            ) : (
              <>
                <Upload size={20} className="text-gray-400" />
                <span className="text-sm text-gray-600">
                  Click to upload or drag and drop
                </span>
              </>
            )}
          </label>
        </div>
      ) : (
        <div className="flex items-center gap-3 p-3 bg-green-50 border border-green-200 rounded-lg">
          <div className="flex-shrink-0 text-green-600">
            {getFileIcon()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-green-900 truncate">
              {getFileName()}
            </p>
            <p className="text-xs text-green-600 flex items-center gap-1 mt-0.5">
              <CheckCircle size={12} />
              Uploaded successfully
            </p>
          </div>
          {!disabled && (
            <button
              type="button"
              onClick={handleRemove}
              className="flex-shrink-0 p-1 text-green-600 hover:text-red-600 hover:bg-red-50 rounded transition"
            >
              <X size={18} />
            </button>
          )}
        </div>
      )}

      {error && (
        <div className="mt-2 flex items-start gap-2 text-sm text-red-600">
          <AlertCircle size={16} className="flex-shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {helperText && !error && (
        <p className="mt-1 text-xs text-gray-500">{helperText}</p>
      )}
    </div>
  );
}

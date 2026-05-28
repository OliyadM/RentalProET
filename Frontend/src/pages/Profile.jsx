import { useState, useEffect } from "react";
import Layout from "../components/Layout";
import Toast from "../components/Toast";
import FileUpload from "../components/FileUpload";
import { profileAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { User, Building, FileText, CheckCircle, XCircle, Clock, AlertCircle } from "lucide-react";

// ── Inline field error ────────────────────────────────────────────────────────
function FieldError({ msg }) {
  if (!msg) return null;
  return <p className="text-xs text-red-600 mt-1">{msg}</p>;
}

const statusColors = {
  PENDING_PROFILE: "bg-gray-100 text-gray-700",
  PENDING_VERIFICATION: "bg-yellow-100 text-yellow-700",
  VERIFIED: "bg-green-100 text-green-700",
  REJECTED: "bg-red-100 text-red-700",
};

const statusIcons = {
  PENDING_PROFILE: Clock,
  PENDING_VERIFICATION: Clock,
  VERIFIED: CheckCircle,
  REJECTED: XCircle,
};

export default function Profile() {
  const { user } = useAuth();
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [form, setForm] = useState({
    dateOfBirth: "",
    residentialAddress: "",
    nationalIdNumber: "",
    nationalIdDocumentUrl: "",
    tinNumber: "",
    entityType: "INDIVIDUAL",
    businessRegNumber: "",
    businessRegDocumentUrl: "",
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const data = await profileAPI.getMyProfile();
      setProfile(data);
      setForm({
        dateOfBirth: data.dateOfBirth || "",
        residentialAddress: data.residentialAddress || "",
        nationalIdNumber: data.nationalIdNumber || "",
        nationalIdDocumentUrl: data.nationalIdDocumentUrl || "",
        tinNumber: data.tinNumber || "",
        entityType: data.entityType || "INDIVIDUAL",
        businessRegNumber: data.businessRegNumber || "",
        businessRegDocumentUrl: data.businessRegDocumentUrl || "",
      });
      
      // Auto-enable editing if profile is incomplete
      if (data.accountStatus === "PENDING_PROFILE") {
        setEditing(true);
      }
    } catch (error) {
      setToast({ type: "error", message: error.message || "Failed to load profile" });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Per-field validation
    const errs = {};
    if (!form.dateOfBirth)
      errs.dateOfBirth = "Date of birth is required";
    else if (new Date(form.dateOfBirth) >= new Date())
      errs.dateOfBirth = "Date of birth must be in the past";
    else {
      const age = (new Date() - new Date(form.dateOfBirth)) / (1000 * 60 * 60 * 24 * 365.25);
      if (age < 18) errs.dateOfBirth = "You must be at least 18 years old";
    }

    if (!form.residentialAddress.trim())
      errs.residentialAddress = "Residential address is required";
    else if (form.residentialAddress.trim().length < 10)
      errs.residentialAddress = "Please enter a full address (at least 10 characters)";

    if (!form.nationalIdNumber.trim())
      errs.nationalIdNumber = "National ID (FAN) number is required";
    else if (!/^\d{16}$/.test(form.nationalIdNumber.trim()))
      errs.nationalIdNumber = "FAN number must be exactly 16 digits";

    if (!form.tinNumber.trim())
      errs.tinNumber = "TIN number is required";
    else if (form.tinNumber.trim().length < 5)
      errs.tinNumber = "TIN number appears too short";

    if (form.entityType === "BUSINESS" && !form.businessRegNumber.trim())
      errs.businessRegNumber = "Business registration number is required for business entities";

    setFieldErrors(errs);
    if (Object.keys(errs).length) return;

    try {
      setSaving(true);
      const updated = await profileAPI.updateProfile(form);
      setProfile(updated);
      setEditing(false);
      setToast({ 
        type: "success", 
        message: updated.accountStatus === "PENDING_VERIFICATION" 
          ? "Profile submitted for verification!" 
          : "Profile updated successfully!" 
      });
    } catch (error) {
      setToast({ type: "error", message: error.message || "Failed to update profile" });
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // Clear the per-field error as the user corrects it
    if (fieldErrors[name]) setFieldErrors(prev => ({ ...prev, [name]: undefined }));
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      </Layout>
    );
  }

  const StatusIcon = statusIcons[profile?.accountStatus] || Clock;

  return (
    <Layout>
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">My Profile</h2>
          <p className="text-gray-500 text-sm mt-1">Manage your account information and verification status</p>
        </div>

        {/* Status Card */}
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-4">
              <div className={`p-3 rounded-lg ${statusColors[profile?.accountStatus]}`}>
                <StatusIcon size={24} />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Account Status</h3>
                <p className={`text-sm font-medium mt-1 ${statusColors[profile?.accountStatus]}`}>
                  {profile?.accountStatus?.replace(/_/g, " ")}
                </p>
                {profile?.accountStatus === "PENDING_PROFILE" && (
                  <p className="text-sm text-gray-600 mt-2">
                    Please complete your profile to access all features
                  </p>
                )}
                {profile?.accountStatus === "PENDING_VERIFICATION" && (
                  <p className="text-sm text-gray-600 mt-2">
                    Your profile is under review by our verification team
                  </p>
                )}
                {profile?.accountStatus === "VERIFIED" && (
                  <p className="text-sm text-gray-600 mt-2">
                    Your account is verified. You have full access to the platform.
                  </p>
                )}
                {profile?.accountStatus === "REJECTED" && (
                  <div className="mt-2">
                    <p className="text-sm text-red-600 font-medium">Verification Rejected</p>
                    {profile?.rejectionReason && (
                      <p className="text-sm text-gray-600 mt-1">
                        Reason: {profile.rejectionReason}
                      </p>
                    )}
                    <p className="text-sm text-gray-600 mt-1">
                      Please update your information and resubmit
                    </p>
                  </div>
                )}
              </div>
            </div>
            {!editing && profile?.accountStatus !== "PENDING_VERIFICATION" && (
              <button
                onClick={() => setEditing(true)}
                className="px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-blue-900 transition"
              >
                Edit Profile
              </button>
            )}
          </div>

          {profile?.verifiedAt && (
            <div className="mt-4 pt-4 border-t border-gray-100">
              <p className="text-xs text-gray-500">
                Verified by {profile.verifiedByName} on {new Date(profile.verifiedAt).toLocaleDateString()}
              </p>
              {profile.verificationNotes && (
                <p className="text-sm text-gray-600 mt-1">
                  Notes: {profile.verificationNotes}
                </p>
              )}
            </div>
          )}
        </div>

        {/* Profile Form */}
        <div className="bg-white rounded-xl shadow-sm p-6">
          <form onSubmit={handleSubmit}>
            {/* Basic Info */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <User size={20} /> Basic Information
              </h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                  <input
                    type="text"
                    value={profile?.firstName || ""}
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                  <input
                    type="text"
                    value={profile?.lastName || ""}
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  <input
                    type="email"
                    value={profile?.email || ""}
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                  <input
                    type="text"
                    value={profile?.phoneNumber || ""}
                    disabled
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500"
                  />
                </div>
              </div>
            </div>

            {/* KYC Information */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <FileText size={20} /> KYC Information
              </h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Date of Birth <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="date"
                    name="dateOfBirth"
                    value={form.dateOfBirth}
                    onChange={handleChange}
                    disabled={!editing}
                    max={new Date().toISOString().split("T")[0]}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500
                      ${fieldErrors.dateOfBirth ? "border-red-400 bg-red-50" : "border-gray-300"}`}
                  />
                  <FieldError msg={fieldErrors.dateOfBirth} />
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Residential Address <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="residentialAddress"
                    value={form.residentialAddress}
                    onChange={handleChange}
                    disabled={!editing}
                    placeholder="123 Main St, Addis Ababa"
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500
                      ${fieldErrors.residentialAddress ? "border-red-400 bg-red-50" : "border-gray-300"}`}
                  />
                  <FieldError msg={fieldErrors.residentialAddress} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    National ID Number (FAN) <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="nationalIdNumber"
                    value={form.nationalIdNumber}
                    onChange={handleChange}
                    disabled={!editing}
                    placeholder="1234567890123456"
                    maxLength={16}
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500
                      ${fieldErrors.nationalIdNumber ? "border-red-400 bg-red-50" : "border-gray-300"}`}
                  />
                  {editing && (
                    <p className="text-xs text-gray-400 mt-1">
                      FAN (Fayda Alliance Number) — must be exactly 16 digits
                    </p>
                  )}
                  <FieldError msg={fieldErrors.nationalIdNumber} />
                </div>
                <div>
                  <FileUpload
                    label="National ID Document"
                    value={form.nationalIdDocumentUrl}
                    onChange={(url) => setForm(prev => ({ ...prev, nationalIdDocumentUrl: url }))}
                    folder="kyc/national-id"
                    disabled={!editing}
                    helperText="Upload a clear photo or scan of your national ID"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    TIN Number <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="tinNumber"
                    value={form.tinNumber}
                    onChange={handleChange}
                    disabled={!editing}
                    placeholder="TIN987654321"
                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500
                      ${fieldErrors.tinNumber ? "border-red-400 bg-red-50" : "border-gray-300"}`}
                  />
                  <FieldError msg={fieldErrors.tinNumber} />
                </div>
              </div>
            </div>

            {/* Entity Type */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Building size={20} /> Entity Type
              </h3>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Are you registering as an individual or business? <span className="text-red-500">*</span>
                </label>
                <div className="flex gap-4">
                  <label className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="entityType"
                      value="INDIVIDUAL"
                      checked={form.entityType === "INDIVIDUAL"}
                      onChange={handleChange}
                      disabled={!editing}
                      className="text-primary focus:ring-primary"
                    />
                    <span className="text-sm text-gray-700">Individual</span>
                  </label>
                  <label className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="entityType"
                      value="BUSINESS"
                      checked={form.entityType === "BUSINESS"}
                      onChange={handleChange}
                      disabled={!editing}
                      className="text-primary focus:ring-primary"
                    />
                    <span className="text-sm text-gray-700">Business</span>
                  </label>
                </div>
              </div>

              {form.entityType === "BUSINESS" && (
                <div className="grid grid-cols-2 gap-4 p-4 bg-blue-50 rounded-lg">
                  <div className="col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Business Registration Number <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      name="businessRegNumber"
                      value={form.businessRegNumber}
                      onChange={handleChange}
                      disabled={!editing}
                      required={form.entityType === "BUSINESS"}
                      placeholder="BRN2024001"
                      className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent disabled:bg-gray-50 disabled:text-gray-500
                        ${fieldErrors.businessRegNumber ? "border-red-400 bg-red-50" : "border-gray-300"}`}
                    />
                    <FieldError msg={fieldErrors.businessRegNumber} />
                  </div>
                  <div className="col-span-2">
                    <FileUpload
                      label="Business Registration Document"
                      value={form.businessRegDocumentUrl}
                      onChange={(url) => setForm(prev => ({ ...prev, businessRegDocumentUrl: url }))}
                      folder="kyc/business-reg"
                      disabled={!editing}
                      helperText="Upload your business registration certificate"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            {editing && (
              <div className="flex gap-3 pt-4 border-t border-gray-200">
                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-2 bg-primary text-white rounded-lg font-medium hover:bg-blue-900 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? "Saving..." : "Save Profile"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditing(false);
                    loadProfile();
                  }}
                  disabled={saving}
                  className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Cancel
                </button>
              </div>
            )}
          </form>

          {profile?.accountStatus === "PENDING_VERIFICATION" && (
            <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start gap-3">
              <AlertCircle size={20} className="text-yellow-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="text-sm font-medium text-yellow-800">Profile Under Review</p>
                <p className="text-sm text-yellow-700 mt-1">
                  Your profile is currently being reviewed by our verification team. You'll be notified once the review is complete.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

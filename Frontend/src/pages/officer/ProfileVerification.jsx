import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import Modal from "../../components/Modal";
import { profileAPI } from "../../services/api";
import { CheckCircle, XCircle, Clock, User, FileText, Building, Eye } from "lucide-react";

const statusColors = {
  PENDING_PROFILE: "bg-gray-100 text-gray-700",
  PENDING_VERIFICATION: "bg-yellow-100 text-yellow-700",
  VERIFIED: "bg-green-100 text-green-700",
  REJECTED: "bg-red-100 text-red-700",
};

export default function ProfileVerification() {
  const navigate = useNavigate();
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(true);
  const [profiles, setProfiles] = useState([]);
  const [selectedProfile, setSelectedProfile] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [action, setAction] = useState(null); // "VERIFIED" or "REJECTED"
  const [verificationNotes, setVerificationNotes] = useState("");
  const [rejectionReason, setRejectionReason] = useState("");
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    loadPendingProfiles();
  }, []);

  const loadPendingProfiles = async () => {
    try {
      setLoading(true);
      const data = await profileAPI.getPendingProfiles();
      setProfiles(data);
    } catch (error) {
      setToast({ type: "error", message: error.message || "Failed to load profiles" });
    } finally {
      setLoading(false);
    }
  };

  const handleViewProfile = (profile) => {
    setSelectedProfile(profile);
    setShowModal(true);
    setAction(null);
    setVerificationNotes("");
    setRejectionReason("");
  };

  const handleVerify = async () => {
    if (!action) {
      setToast({ type: "error", message: "Please select an action" });
      return;
    }

    if (action === "REJECTED" && !rejectionReason.trim()) {
      setToast({ type: "error", message: "Rejection reason is required" });
      return;
    }

    try {
      setProcessing(true);
      await profileAPI.verifyProfile(
        selectedProfile.id,
        action,
        verificationNotes,
        action === "REJECTED" ? rejectionReason : null
      );
      
      setToast({ 
        type: "success", 
        message: `Profile ${action === "VERIFIED" ? "verified" : "rejected"} successfully` 
      });
      
      setShowModal(false);
      setSelectedProfile(null);
      loadPendingProfiles();
    } catch (error) {
      setToast({ type: "error", message: error.message || "Failed to process verification" });
    } finally {
      setProcessing(false);
    }
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

  return (
    <Layout>
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Profile Verification</h2>
        <p className="text-gray-500 text-sm mt-1">Review and verify user profiles</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-xl shadow-sm p-6">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-yellow-100 rounded-lg">
              <Clock size={24} className="text-yellow-600" />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-900">{profiles.length}</p>
              <p className="text-sm text-gray-500">Pending Review</p>
            </div>
          </div>
        </div>
      </div>

      {/* Profiles List */}
      <div className="bg-white rounded-xl shadow-sm">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-800">Pending Profiles</h3>
        </div>

        {profiles.length === 0 ? (
          <div className="px-6 py-12 text-center">
            <Clock size={48} className="mx-auto text-gray-300 mb-3" />
            <p className="text-gray-500">No profiles pending verification</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-xs uppercase">
                <tr>
                  <th className="px-6 py-3 text-left">User</th>
                  <th className="px-6 py-3 text-left">Email</th>
                  <th className="px-6 py-3 text-left">Phone</th>
                  <th className="px-6 py-3 text-left">Entity Type</th>
                  <th className="px-6 py-3 text-left">Status</th>
                  <th className="px-6 py-3 text-left">Submitted</th>
                  <th className="px-6 py-3 text-left">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {profiles.map((profile) => (
                  <tr key={profile.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white text-sm font-bold">
                          {profile.firstName?.[0]}{profile.lastName?.[0]}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">
                            {profile.firstName} {profile.lastName}
                          </p>
                          <p className="text-xs text-gray-500">{profile.role}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-gray-600">{profile.email}</td>
                    <td className="px-6 py-4 text-gray-600">{profile.phoneNumber}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1 px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-medium">
                        {profile.entityType === "BUSINESS" ? <Building size={12} /> : <User size={12} />}
                        {profile.entityType}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-2 py-1 rounded text-xs font-medium ${statusColors[profile.accountStatus]}`}>
                        {profile.accountStatus?.replace(/_/g, " ")}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-gray-500">
                      {new Date(profile.updatedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4">
                      <button
                        onClick={() => handleViewProfile(profile)}
                        className="flex items-center gap-1 px-3 py-1.5 bg-primary text-white rounded text-xs font-medium hover:bg-blue-900 transition"
                      >
                        <Eye size={14} /> Review
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Verification Modal */}
      {showModal && selectedProfile && (
        <Modal onClose={() => setShowModal(false)} title="Review Profile">
          <div className="space-y-6">
            {/* User Info */}
            <div>
              <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <User size={18} /> User Information
              </h4>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <p className="text-gray-500">Name</p>
                  <p className="font-medium text-gray-900">
                    {selectedProfile.firstName} {selectedProfile.lastName}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500">Email</p>
                  <p className="font-medium text-gray-900">{selectedProfile.email}</p>
                </div>
                <div>
                  <p className="text-gray-500">Phone</p>
                  <p className="font-medium text-gray-900">{selectedProfile.phoneNumber}</p>
                </div>
                <div>
                  <p className="text-gray-500">Role</p>
                  <p className="font-medium text-gray-900">{selectedProfile.role}</p>
                </div>
              </div>
            </div>

            {/* KYC Info */}
            <div>
              <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <FileText size={18} /> KYC Information
              </h4>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <p className="text-gray-500">Date of Birth</p>
                  <p className="font-medium text-gray-900">
                    {selectedProfile.dateOfBirth ? new Date(selectedProfile.dateOfBirth).toLocaleDateString() : "N/A"}
                  </p>
                </div>
                <div>
                  <p className="text-gray-500">National ID</p>
                  <p className="font-medium text-gray-900">{selectedProfile.nationalIdNumber || "N/A"}</p>
                </div>
                <div className="col-span-2">
                  <p className="text-gray-500">Residential Address</p>
                  <p className="font-medium text-gray-900">{selectedProfile.residentialAddress || "N/A"}</p>
                </div>
                <div>
                  <p className="text-gray-500">TIN Number</p>
                  <p className="font-medium text-gray-900">{selectedProfile.tinNumber || "N/A"}</p>
                </div>
                <div>
                  <p className="text-gray-500">National ID Document</p>
                  {selectedProfile.nationalIdDocumentUrl ? (
                    <a
                      href={selectedProfile.nationalIdDocumentUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-primary hover:underline text-sm"
                    >
                      View Document
                    </a>
                  ) : (
                    <p className="text-gray-400">Not provided</p>
                  )}
                </div>
              </div>
            </div>

            {/* Business Info */}
            {selectedProfile.entityType === "BUSINESS" && (
              <div>
                <h4 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                  <Building size={18} /> Business Information
                </h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p className="text-gray-500">Business Reg Number</p>
                    <p className="font-medium text-gray-900">{selectedProfile.businessRegNumber || "N/A"}</p>
                  </div>
                  <div>
                    <p className="text-gray-500">Business Reg Document</p>
                    {selectedProfile.businessRegDocumentUrl ? (
                      <a
                        href={selectedProfile.businessRegDocumentUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-primary hover:underline text-sm"
                      >
                        View Document
                      </a>
                    ) : (
                      <p className="text-gray-400">Not provided</p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Verification Action */}
            <div className="pt-4 border-t border-gray-200">
              <h4 className="font-semibold text-gray-900 mb-3">Verification Decision</h4>
              
              <div className="flex gap-3 mb-4">
                <button
                  onClick={() => setAction("VERIFIED")}
                  className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-lg border-2 transition ${
                    action === "VERIFIED"
                      ? "border-green-500 bg-green-50 text-green-700"
                      : "border-gray-200 hover:border-green-300"
                  }`}
                >
                  <CheckCircle size={20} />
                  <span className="font-medium">Verify</span>
                </button>
                <button
                  onClick={() => setAction("REJECTED")}
                  className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-lg border-2 transition ${
                    action === "REJECTED"
                      ? "border-red-500 bg-red-50 text-red-700"
                      : "border-gray-200 hover:border-red-300"
                  }`}
                >
                  <XCircle size={20} />
                  <span className="font-medium">Reject</span>
                </button>
              </div>

              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Verification Notes (Optional)
                  </label>
                  <textarea
                    value={verificationNotes}
                    onChange={(e) => setVerificationNotes(e.target.value)}
                    rows={3}
                    placeholder="Add any notes about this verification..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>

                {action === "REJECTED" && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Rejection Reason <span className="text-red-500">*</span>
                    </label>
                    <textarea
                      value={rejectionReason}
                      onChange={(e) => setRejectionReason(e.target.value)}
                      rows={3}
                      required
                      placeholder="Explain why this profile is being rejected..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-4 border-t border-gray-200">
              <button
                onClick={handleVerify}
                disabled={!action || processing}
                className="flex-1 px-6 py-2 bg-primary text-white rounded-lg font-medium hover:bg-blue-900 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {processing ? "Processing..." : "Submit Decision"}
              </button>
              <button
                onClick={() => setShowModal(false)}
                disabled={processing}
                className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Cancel
              </button>
            </div>
          </div>
        </Modal>
      )}
    </Layout>
  );
}

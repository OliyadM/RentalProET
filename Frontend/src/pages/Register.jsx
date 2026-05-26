import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";

// ── Validation helpers ────────────────────────────────────────────────────────
const EMAIL_RE   = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
// Ethiopian mobile: 09XXXXXXXX or +2519XXXXXXXX (10 or 13 digits)
const PHONE_RE   = /^(\+2519\d{8}|09\d{8})$/;

function validate(form) {
  const e = {};
  if (!form.firstName.trim())                    e.firstName   = "First name is required";
  else if (form.firstName.trim().length < 2)     e.firstName   = "First name must be at least 2 characters";

  if (!form.lastName.trim())                     e.lastName    = "Last name is required";
  else if (form.lastName.trim().length < 2)      e.lastName    = "Last name must be at least 2 characters";

  if (!form.email.trim())                        e.email       = "Email is required";
  else if (!EMAIL_RE.test(form.email.trim()))    e.email       = "Enter a valid email address";

  if (!form.phone.trim())                        e.phone       = "Phone number is required";
  else if (!PHONE_RE.test(form.phone.trim()))    e.phone       = "Enter a valid Ethiopian phone number (e.g. 0911234567)";

  if (!form.password)                            e.password    = "Password is required";
  else if (form.password.length < 6)             e.password    = "Password must be at least 6 characters";
  else if (!/[A-Za-z]/.test(form.password))      e.password    = "Password must contain at least one letter";
  else if (!/\d/.test(form.password))            e.password    = "Password must contain at least one number";

  if (form.role === "SUBCITY_STAFF" && !form.subCityZone)
                                                 e.subCityZone = "Sub-city zone is required for government officers";
  return e;
}

// ── Inline field error ────────────────────────────────────────────────────────
function FieldError({ msg }) {
  if (!msg) return null;
  return <p className="text-xs text-red-600 mt-1">{msg}</p>;
}

const SUB_CITIES = ["Bole","Kirkos","Yeka","Arada","Lideta","Kolfe","Nifas Silk","Akaky"];

export default function Register() {
  const [form, setForm] = useState({
    firstName: "", lastName: "", email: "", phone: "",
    password: "", role: "LANDLORD", subCityZone: "",
  });
  const [errors, setErrors]           = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading]         = useState(false);
  const { login } = useAuth();
  const navigate  = useNavigate();

  const set = (field) => (e) => {
    setForm(prev => ({ ...prev, [field]: e.target.value }));
    // Clear the per-field error as the user types
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }));
  };

  const blurValidate = (field) => {
    const next = validate({ ...form });
    setErrors(prev => ({ ...prev, [field]: next[field] }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError("");
    const errs = validate(form);
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    try {
      const registerData = {
        firstName:   form.firstName.trim(),
        lastName:    form.lastName.trim(),
        email:       form.email.trim(),
        phoneNumber: form.phone.trim(),
        password:    form.password,
        role:        form.role,
        subCityZone: form.role === "SUBCITY_STAFF" ? form.subCityZone : null,
      };
      const user = await authAPI.register(registerData);
      login(user);
      if (user.role === "LANDLORD")           navigate("/landlord/dashboard");
      else if (user.role === "TENANT")        navigate("/tenant/dashboard");
      else if (user.role === "ADMINISTRATOR") navigate("/admin/dashboard");
      else                                    navigate("/officer/dashboard");
    } catch (err) {
      const data = err.response?.data;
      if (data && typeof data === "object" && !data.message) {
        // @Valid field map from backend
        const msgs = Object.entries(data).map(([f, m]) => `${f}: ${m}`).join(" · ");
        setServerError(msgs);
      } else {
        setServerError(data?.message || err.message || "Registration failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (field) =>
    `w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary
     ${errors[field] ? "border-red-400 bg-red-50" : "border-gray-300"}`;

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-lg w-full max-w-md p-8">
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-primary rounded-xl mb-3">
            <span className="text-white font-bold text-xl">RP</span>
          </div>
          <h1 className="text-2xl font-bold text-primary">Create Account</h1>
          <p className="text-gray-500 text-sm mt-1">RentalPro ET — Registration</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3" noValidate>
          {serverError && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
              {serverError}
            </div>
          )}

          {/* Name row */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                First Name <span className="text-red-500">*</span>
              </label>
              <input
                value={form.firstName}
                onChange={set("firstName")}
                onBlur={() => blurValidate("firstName")}
                placeholder="Abebe"
                className={inputClass("firstName")}
              />
              <FieldError msg={errors.firstName} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Last Name <span className="text-red-500">*</span>
              </label>
              <input
                value={form.lastName}
                onChange={set("lastName")}
                onBlur={() => blurValidate("lastName")}
                placeholder="Kebede"
                className={inputClass("lastName")}
              />
              <FieldError msg={errors.lastName} />
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              value={form.email}
              onChange={set("email")}
              onBlur={() => blurValidate("email")}
              placeholder="you@example.com"
              autoComplete="email"
              className={inputClass("email")}
            />
            <FieldError msg={errors.email} />
          </div>

          {/* Phone */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Phone <span className="text-red-500">*</span>
            </label>
            <input
              type="tel"
              value={form.phone}
              onChange={set("phone")}
              onBlur={() => blurValidate("phone")}
              placeholder="0911234567"
              autoComplete="tel"
              className={inputClass("phone")}
            />
            <FieldError msg={errors.phone} />
          </div>

          {/* Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Password <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              value={form.password}
              onChange={set("password")}
              onBlur={() => blurValidate("password")}
              placeholder="Min. 6 chars, letters and numbers"
              autoComplete="new-password"
              className={inputClass("password")}
            />
            {errors.password
              ? <FieldError msg={errors.password} />
              : <p className="text-xs text-gray-400 mt-1">At least 6 characters with letters and numbers</p>}
          </div>

          {/* Role */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
            <select
              value={form.role}
              onChange={set("role")}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="LANDLORD">Landlord</option>
              <option value="TENANT">Tenant</option>
              <option value="SUBCITY_STAFF">Government Officer</option>
            </select>
          </div>

          {/* Sub-city — only for officers */}
          {form.role === "SUBCITY_STAFF" && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Sub-city Zone <span className="text-red-500">*</span>
              </label>
              <select
                value={form.subCityZone}
                onChange={set("subCityZone")}
                onBlur={() => blurValidate("subCityZone")}
                className={inputClass("subCityZone")}
              >
                <option value="">Select sub-city</option>
                {SUB_CITIES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
              <FieldError msg={errors.subCityZone} />
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary text-white rounded-lg py-2.5 text-sm font-semibold hover:bg-blue-900 transition disabled:opacity-50 mt-2"
          >
            {loading ? "Creating account..." : "Create Account"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-5">
          Already have an account?{" "}
          <Link to="/login" className="text-primary font-medium hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  );
}

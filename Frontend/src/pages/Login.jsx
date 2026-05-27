import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authAPI } from "../services/api";

// ── Validation helpers ────────────────────────────────────────────────────────
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validate(email, password) {
  const errs = {};
  if (!email.trim())               errs.email    = "Email is required";
  else if (!EMAIL_RE.test(email))  errs.email    = "Enter a valid email address";
  if (!password)                   errs.password = "Password is required";
  else if (password.length < 6)   errs.password = "Password must be at least 6 characters";
  return errs;
}

// ── Field error helper ────────────────────────────────────────────────────────
function FieldError({ msg }) {
  if (!msg) return null;
  return <p className="text-xs text-red-600 mt-1">{msg}</p>;
}

export default function Login() {
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors]     = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading]   = useState(false);
  const { login } = useAuth();
  const navigate  = useNavigate();

  // Validate a single field on blur so errors appear as the user leaves each input
  const blurValidate = (field) => {
    const next = validate(email, password);
    setErrors(prev => ({ ...prev, [field]: next[field] }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError("");
    const errs = validate(email, password);
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setErrors({});
    setLoading(true);
    try {
      const user = await authAPI.login(email.trim(), password);
      login(user);
      if (user.role === "LANDLORD")           navigate("/landlord/dashboard");
      else if (user.role === "TENANT")        navigate("/tenant/dashboard");
      else if (user.role === "ADMINISTRATOR") navigate("/admin/dashboard");
      else                                    navigate("/officer/dashboard");
    } catch (err) {
      const data = err.response?.data;
      setServerError(data?.message || "Invalid email or password. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (field) =>
    `w-full border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent
     ${errors[field] ? "border-red-400 bg-red-50" : "border-gray-300"}`;

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-lg w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-primary rounded-xl mb-4">
            <span className="text-white font-bold text-xl">RP</span>
          </div>
          <h1 className="text-2xl font-bold text-primary">RentalPro ET</h1>
          <p className="text-gray-500 text-sm mt-1">Rental Compliance Management System</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          {serverError && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
              {serverError}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email Address <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onBlur={() => blurValidate("email")}
              placeholder="you@example.com"
              autoComplete="email"
              className={inputClass("email")}
            />
            <FieldError msg={errors.email} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Password <span className="text-red-500">*</span>
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => blurValidate("password")}
              placeholder="••••••••"
              autoComplete="current-password"
              className={inputClass("password")}
            />
            <FieldError msg={errors.password} />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary text-white rounded-lg py-2.5 text-sm font-semibold hover:bg-blue-900 transition disabled:opacity-50"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{" "}
          <Link to="/register" className="text-primary font-medium hover:underline">Register</Link>
        </p>

        <div className="mt-6 p-4 bg-gray-50 rounded-lg text-xs text-gray-500">
          <p className="font-medium mb-1">Demo accounts:</p>
          <p>Landlord: test@example.com / password123</p>
          <p>Tenant: tenant@test.com / password123</p>
          <p>Officer: officer@test.com / password123</p>
          <p>Admin: admin@rentalpro.et / Admin@123</p>
        </div>
      </div>
    </div>
  );
}

// Add Declaration will go here
import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { AlertTriangle, CheckCircle, TrendingUp } from "lucide-react";
import Layout from "../../components/Layout";
import { declarationsAPI } from "../../services/api";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

export default function AddDeclaration() {
  const { contractId } = useParams();
  const navigate = useNavigate();
  const [period, setPeriod] = useState("");
  const [rent, setRent] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const declaration = await declarationsAPI.create(contractId, period + "-01", parseFloat(rent));
      setResult(declaration);
    } catch (error) {
      console.error("Declaration error:", error);
    } finally {
      setLoading(false);
    }
  };

  const deviation = result ? (((result.declaredRent - result.aiBenchmarkRent) / result.aiBenchmarkRent) * 100).toFixed(1) : null;

  return (
    <Layout>
      <div className="max-w-xl">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Add Rent Declaration</h2>
        <p className="text-gray-500 text-sm mb-6">Declare rent for the selected period</p>

        <div className="bg-white rounded-xl shadow-sm p-6 mb-5">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Declaration Period *</label>
              <input type="month" value={period} onChange={e => setPeriod(e.target.value)} required
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Declared Monthly Rent (ETB) *</label>
              <input type="number" value={rent} onChange={e => setRent(e.target.value)} required min="1"
                placeholder="Enter rent amount"
                className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
            </div>
            <div className="flex gap-3">
              <button type="button" onClick={() => navigate(`/landlord/contracts/${contractId}`)}
                className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
              <button type="submit" disabled={loading}
                className="flex-1 bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 disabled:opacity-50">
                {loading ? "Analyzing..." : "Submit Declaration"}
              </button>
            </div>
          </form>
        </div>

        {result && (
          <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
            <h3 className="font-semibold text-gray-800 flex items-center gap-2">
              <TrendingUp size={18} className="text-accent" /> Declaration Analysis
            </h3>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="p-3 bg-gray-50 rounded-lg">
                <p className="text-xs text-gray-500 mb-1">Your Declared Rent</p>
                <p className="font-bold text-gray-900 text-lg">{fmt(result.declaredRent)}</p>
              </div>
              <div className="p-3 bg-blue-50 rounded-lg">
                <p className="text-xs text-primary mb-1">AI Benchmark</p>
                <p className="font-bold text-primary text-lg">{fmt(result.aiBenchmarkRent)}</p>
              </div>
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-gray-50">
              <span className="text-sm text-gray-600">Deviation from benchmark</span>
              <span className={`text-lg font-bold ${Math.abs(deviation) > 15 ? "text-danger" : Math.abs(deviation) > 5 ? "text-accent" : "text-success"}`}>
                {deviation > 0 ? "+" : ""}{deviation}%
              </span>
            </div>

            <div className={`flex items-start gap-3 p-4 rounded-lg ${result.isAnomaly ? "bg-red-50 border border-red-200" : "bg-green-50 border border-green-200"}`}>
              {result.isAnomaly
                ? <AlertTriangle size={18} className="text-danger mt-0.5 flex-shrink-0" />
                : <CheckCircle size={18} className="text-success mt-0.5 flex-shrink-0" />}
              <div>
                <p className={`font-semibold text-sm ${result.isAnomaly ? "text-danger" : "text-success"}`}>
                  {result.isAnomaly ? "Anomaly Detected" : "No Anomaly Detected"}
                </p>
                {result.anomalyReason && <p className="text-xs mt-0.5 text-gray-600">{result.anomalyReason}</p>}
              </div>
            </div>

            <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
              <span className="text-sm text-gray-700">Estimated Tax</span>
              <span className="font-bold text-primary">{fmt(result.estimatedTax)}</span>
            </div>

            <p className="text-xs text-gray-400 text-center">Declaration saved. AI analysis is advisory only.</p>

            <button onClick={() => navigate(`/landlord/contracts/${contractId}`)}
              className="w-full bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900">
              Back to Contract
            </button>
          </div>
        )}
      </div>
    </Layout>
  );
}
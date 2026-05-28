// Add Declaration will go here
import { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { AlertTriangle, CheckCircle, TrendingUp } from "lucide-react";
import Layout from "../../components/Layout";
import Toast from "../../components/Toast";
import { contractsAPI, declarationsAPI, profileAPI, propertiesAPI } from "../../services/api";

function fmt(n) { return "ETB " + Number(n).toLocaleString(); }

const INDIVIDUAL_BANDS = [
  { max: 24000, rate: 0.0, deductible: 0, label: "0 - 24,000 ETB" },
  { max: 48000, rate: 0.1, deductible: 2400, label: "24,001 - 48,000 ETB" },
  { max: 78000, rate: 0.2, deductible: 6000, label: "48,001 - 78,000 ETB" },
  { max: 120000, rate: 0.25, deductible: 9900, label: "78,001 - 120,000 ETB" },
  { max: 168000, rate: 0.3, deductible: 15900, label: "120,001 - 168,000 ETB" },
  { max: Number.POSITIVE_INFINITY, rate: 0.3, deductible: 15900, label: "Above 168,000 ETB" },
];

const DEDUCTION_ELIGIBLE_TYPES = new Set([
  "HOUSE",
  "APARTMENT_BUILDING",
  "MIXED_USE_BUILDING",
]);

function round2(value) {
  return Math.round(value * 100) / 100;
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export default function AddDeclaration() {
  const { contractId } = useParams();
  const navigate = useNavigate();
  const [period, setPeriod] = useState("");
  const [rent, setRent] = useState("");
  const [claimDeduction, setClaimDeduction] = useState(false);
  const [result, setResult] = useState(null);
  const [context, setContext] = useState(null);
  const [contextLoading, setContextLoading] = useState(true);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const loadContext = async () => {
      setContextLoading(true);
      try {
        const [profile, contract] = await Promise.all([
          profileAPI.getMyProfile(),
          contractsAPI.getById(contractId),
        ]);
        const property = await propertiesAPI.getById(contract.propertyId);

        if (!cancelled) {
          setContext({
            entityType: profile.entityType || "INDIVIDUAL",
            propertyType: property.propertyType,
            contractedRent: contract.monthlyRent,
          });
        }
      } catch {
        if (!cancelled) {
          setToast({
            type: "error",
            message: "Could not load declaration context. Tax preview may be unavailable.",
          });
        }
      } finally {
        if (!cancelled) setContextLoading(false);
      }
    };

    loadContext();
    return () => { cancelled = true; };
  }, [contractId]);

  const preview = useMemo(() => {
    const monthlyRent = Number(rent);
    if (!monthlyRent || monthlyRent <= 0 || !context) return null;

    const annualGross = round2(monthlyRent * 12);
    const isBusiness = context.entityType === "BUSINESS";
    const deductionEligible = DEDUCTION_ELIGIBLE_TYPES.has(context.propertyType);
    const deductionApplied = !isBusiness && claimDeduction && deductionEligible;
    const deductionPercent = deductionApplied ? 0.4 : 0;
    const deductionAmount = round2(annualGross * deductionPercent);
    const taxableAnnualIncome = round2(Math.max(0, annualGross - deductionAmount));

    let annualTax;
    let bandLabel;
    if (isBusiness) {
      annualTax = round2(annualGross * 0.3);
      bandLabel = "Flat 30% (Business)";
    } else {
      const band = INDIVIDUAL_BANDS.find((b) => taxableAnnualIncome <= b.max) || INDIVIDUAL_BANDS.at(-1);
      annualTax = round2(Math.max(0, taxableAnnualIncome * band.rate - band.deductible));
      bandLabel = band.label;
    }

    const monthlyTax = round2(annualTax / 12);
    const effectiveTaxRate = annualGross > 0 ? round2(annualTax / annualGross) : 0;
    const mixedUseDeductionWarning = deductionApplied && context.propertyType === "MIXED_USE_BUILDING";

    return {
      annualGross,
      deductionApplied,
      deductionAmount,
      taxableAnnualIncome,
      annualTax,
      monthlyTax,
      effectiveTaxRate,
      bandLabel,
      mixedUseDeductionWarning,
    };
  }, [rent, claimDeduction, context]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!period) {
      setError("Declaration period is required.");
      return;
    }
    if (!rent || Number(rent) <= 0) {
      setError("Declared rent must be greater than zero.");
      return;
    }
    if (context?.contractedRent && Number(rent) < context.contractedRent) {
      setError(`Declared rent cannot be less than the contracted amount of ETB ${context.contractedRent.toLocaleString()}.`);
      return;
    }
    setLoading(true);
    try {
      const declaration = await declarationsAPI.create(
        contractId,
        `${period}-01`,
        parseFloat(rent),
        claimDeduction
      );
      setResult(declaration);
      // Scroll to results panel so it's immediately visible
      setTimeout(() => {
        document.getElementById("declaration-result")?.scrollIntoView({ behavior: "smooth" });
      }, 100);
    } catch (error) {
      setError(error.message || "Failed to submit declaration.");
    } finally {
      setLoading(false);
    }
  };

  const deviation = result && result.aiBenchmarkRent
    ? (((result.declaredRent - result.aiBenchmarkRent) / result.aiBenchmarkRent) * 100).toFixed(1)
    : null;

  const downloadTaxSummaryPdf = async () => {
    if (!result?.id) return;
    setDownloadingPdf(true);
    try {
      const blob = await declarationsAPI.downloadTaxSummaryPdf(result.id);
      downloadBlob(blob, `tax-summary-${result.id}.pdf`);
    } catch (e) {
      setToast({
        type: "error",
        message: e.message || "Failed to download tax summary PDF.",
      });
    } finally {
      setDownloadingPdf(false);
    }
  };

  return (
    <Layout>
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}
      <div className="max-w-xl">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Add Rent Declaration</h2>
        <p className="text-gray-500 text-sm mb-6">Declare rent for the selected period</p>

        <div className="bg-white rounded-xl shadow-sm p-6 mb-5">
          {!result && (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
                {error}
              </div>
            )}
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
              {context?.contractedRent && (
                <p className="text-xs text-gray-500 mt-1">
                  Contracted rent: <span className="font-medium text-gray-700">ETB {context.contractedRent.toLocaleString()}</span> — declared amount cannot be lower
                </p>
              )}
            </div>

            {!contextLoading && context && (
              <div className="p-4 rounded-lg bg-gray-50 border border-gray-200">
                <p className="text-xs text-gray-500 mb-2">
                  Taxpayer Type: <span className="font-medium text-gray-700">{context.entityType}</span>
                </p>
                {context.entityType === "INDIVIDUAL" ? (
                  <label className="flex items-start gap-2 text-sm text-gray-700">
                    <input
                      type="checkbox"
                      checked={claimDeduction}
                      onChange={(e) => setClaimDeduction(e.target.checked)}
                      className="mt-0.5 accent-primary"
                    />
                    <span>
                      Claim 40% residential deduction
                      {!DEDUCTION_ELIGIBLE_TYPES.has(context.propertyType) && (
                        <span className="text-gray-500"> (not applicable for this property type)</span>
                      )}
                    </span>
                  </label>
                ) : (
                  <p className="text-sm text-gray-600">
                    Business taxpayer: flat 30% tax applies (no residential deduction).
                  </p>
                )}
              </div>
            )}

            {preview && (
              <div className="p-4 rounded-lg bg-blue-50 border border-blue-200 space-y-2 text-sm">
                <p className="font-semibold text-primary">Live Tax Preview (Advisory)</p>
                <div className="grid grid-cols-2 gap-2 text-gray-700">
                  <p>Annual Gross:</p><p className="font-medium text-right">{fmt(preview.annualGross)}</p>
                  <p>Deduction:</p><p className="font-medium text-right">{fmt(preview.deductionAmount)}</p>
                  <p>Taxable Annual:</p><p className="font-medium text-right">{fmt(preview.taxableAnnualIncome)}</p>
                  <p>Annual Tax:</p><p className="font-medium text-right">{fmt(preview.annualTax)}</p>
                  <p>Monthly Tax:</p><p className="font-medium text-right">{fmt(preview.monthlyTax)}</p>
                  <p>Effective Rate:</p><p className="font-medium text-right">{(preview.effectiveTaxRate * 100).toFixed(2)}%</p>
                </div>
                <p className="text-xs text-gray-600">Applied Band: {preview.bandLabel}</p>
                {preview.mixedUseDeductionWarning && (
                  <p className="text-xs text-yellow-700">
                    Residential deduction applied - officer should verify commercial split.
                  </p>
                )}
                <p className="text-xs text-gray-500">
                  This estimate covers rental income only. Total tax liability may differ if landlord has other income sources.
                </p>
              </div>
            )}

            <div className="flex gap-3">
              <button type="button" onClick={() => navigate(`/landlord/contracts/${contractId}`)}
                className="flex-1 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">Cancel</button>
              <button type="submit" disabled={loading}
                className="flex-1 bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900 disabled:opacity-50">
                {loading ? "Analyzing..." : "Submit Declaration"}
              </button>
            </div>
          </form>
          )}
        </div>

        {result && (
          <div id="declaration-result" className="bg-white rounded-xl shadow-sm p-6 space-y-4">
            <h3 className="font-semibold text-gray-800 flex items-center gap-2">
              <TrendingUp size={18} className="text-accent" /> Declaration Analysis
            </h3>

            {/* Rent comparison */}
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="p-3 bg-gray-50 rounded-lg">
                <p className="text-xs text-gray-500 mb-1">Your Declared Rent</p>
                <p className="font-bold text-gray-900 text-lg">{fmt(result.declaredRent)}</p>
              </div>
              <div className="p-3 bg-blue-50 rounded-lg">
                <p className="text-xs text-primary mb-1">Benchmark Suggested</p>
                <p className="font-bold text-primary text-lg">{fmt(result.aiBenchmarkRent)}</p>
              </div>
            </div>

            {/* Benchmark range */}
            {result.benchmarkLowerBound != null && (
              <div className="p-3 bg-gray-50 rounded-lg text-sm">
                <p className="text-xs text-gray-500 mb-2">Expected Range</p>
                <div className="flex items-center gap-2">
                  <span className="text-gray-600">ETB {result.benchmarkLowerBound?.toLocaleString()}</span>
                  <div className="flex-1 h-2 bg-gray-200 rounded-full relative">
                    {/* Marker for declared rent position */}
                    {(() => {
                      const lo = result.benchmarkLowerBound;
                      const hi = result.benchmarkUpperBound;
                      const val = result.declaredRent;
                      const pct = Math.min(100, Math.max(0, ((val - lo) / (hi - lo)) * 100));
                      const inRange = val >= lo && val <= hi;
                      return (
                        <div
                          className={`absolute top-1/2 -translate-y-1/2 w-3 h-3 rounded-full border-2 border-white ${inRange ? "bg-success" : "bg-danger"}`}
                          style={{ left: `${pct}%`, transform: "translate(-50%, -50%)" }}
                          title={`Your rent: ETB ${val?.toLocaleString()}`}
                        />
                      );
                    })()}
                  </div>
                  <span className="text-gray-600">ETB {result.benchmarkUpperBound?.toLocaleString()}</span>
                </div>
                {result.benchmarkPricePerM2 != null && (
                  <p className="text-xs text-gray-400 mt-1">
                    Market rate: ETB {result.benchmarkPricePerM2?.toFixed(0)}/m²
                    {result.benchmarkSampleSize != null && ` · Based on ${result.benchmarkSampleSize} comparable properties`}
                    {result.benchmarkFallbackLevel != null && result.benchmarkFallbackLevel > 1 && (
                      <span className="text-yellow-600"> (Level {result.benchmarkFallbackLevel} match — lower confidence)</span>
                    )}
                  </p>
                )}
              </div>
            )}

            {/* Anomaly result */}
            <div className={`flex items-start gap-3 p-4 rounded-lg ${result.isAnomaly ? "bg-red-50 border border-red-200" : "bg-green-50 border border-green-200"}`}>
              {result.isAnomaly
                ? <AlertTriangle size={18} className="text-danger mt-0.5 flex-shrink-0" />
                : <CheckCircle size={18} className="text-success mt-0.5 flex-shrink-0" />}
              <div className="flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <p className={`font-semibold text-sm ${result.isAnomaly ? "text-danger" : "text-success"}`}>
                    {result.isAnomaly ? "Anomaly Detected" : "No Anomaly Detected"}
                  </p>
                  {result.anomalySeverity && (
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                      result.anomalySeverity === "HIGH"   ? "bg-red-100 text-red-700" :
                      result.anomalySeverity === "MEDIUM" ? "bg-orange-100 text-orange-700" :
                                                            "bg-yellow-100 text-yellow-700"}`}>
                      {result.anomalySeverity}
                    </span>
                  )}
                  {result.anomalyDirection && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 font-medium">
                      {result.anomalyDirection === "UNDER_REPORTED" ? "Under-reported" : "Over-reported"}
                    </span>
                  )}
                </div>
                {result.anomalyReason && (
                  <p className="text-xs mt-1 text-gray-600">{result.anomalyReason}</p>
                )}
              </div>
            </div>

            {/* Tax summary */}
            <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
              <span className="text-sm text-gray-700">Estimated Monthly Tax</span>
              <span className="font-bold text-primary">{fmt(result.estimatedTax)}</span>
            </div>

            {result.annualTax != null && (
              <div className="grid grid-cols-2 gap-3 text-sm bg-gray-50 rounded-lg p-3">
                <div>
                  <p className="text-xs text-gray-500">Annual Tax</p>
                  <p className="font-semibold text-gray-800">{fmt(result.annualTax)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Effective Rate</p>
                  <p className="font-semibold text-gray-800">{((result.effectiveTaxRate || 0) * 100).toFixed(2)}%</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Tax Rule</p>
                  <p className="font-semibold text-gray-800">{result.taxRuleVersion || "N/A"}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500">Deduction Applied</p>
                  <p className="font-semibold text-gray-800">{result.deductionApplied ? "Yes" : "No"}</p>
                </div>
              </div>
            )}

            {result.taxAdvisoryNote && (
              <p className="text-xs text-gray-500 bg-gray-50 border border-gray-200 rounded-lg p-3">
                {result.taxAdvisoryNote}
              </p>
            )}

            <p className="text-xs text-gray-400 text-center">Declaration saved. Analysis is advisory only.</p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <button
                onClick={downloadTaxSummaryPdf}
                disabled={downloadingPdf}
                className="w-full border border-primary text-primary py-2.5 rounded-lg text-sm font-medium hover:bg-blue-50 disabled:opacity-50"
              >
                {downloadingPdf ? "Generating PDF..." : "Download Tax Summary PDF"}
              </button>
              <button onClick={() => navigate(`/landlord/contracts/${contractId}`)}
                className="w-full bg-primary text-white py-2.5 rounded-lg text-sm font-medium hover:bg-blue-900">
                Back to Contract
              </button>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
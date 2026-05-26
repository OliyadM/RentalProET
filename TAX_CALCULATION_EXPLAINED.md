# Tax Calculation System - How It Works

## Overview
The system calculates rental income tax based on **Ethiopian Income Tax Amendment Proclamation No. 1395/2025**, effective July 7, 2025.

## Tax Calculation Logic

### For BUSINESS Entities
**Simple Flat Rate:**
- **30% flat tax** on annual gross rental income
- No deductions allowed
- Formula: `Annual Tax = Annual Gross Rent × 30%`

**Example:**
- Monthly Rent: 10,000 ETB
- Annual Gross: 120,000 ETB
- Annual Tax: 120,000 × 0.30 = **36,000 ETB**
- Monthly Tax: 3,000 ETB

---

### For INDIVIDUAL Landlords
**Progressive Tax Bands with Optional Deduction:**

#### Step 1: Calculate Annual Gross Rent
```
Annual Gross Rent = Monthly Rent × 12
```

#### Step 2: Apply Residential Deduction (Optional)
**40% deduction** available for:
- ✅ HOUSE
- ✅ APARTMENT_BUILDING
- ✅ MIXED_USE_BUILDING

**NOT available for:**
- ❌ COMMERCIAL_BUILDING
- ❌ WAREHOUSE_INDUSTRIAL

```
Deduction Amount = Annual Gross Rent × 40%
Taxable Annual Income = Annual Gross Rent - Deduction Amount
```

#### Step 3: Apply Progressive Tax Bands

| Taxable Annual Income | Tax Rate | Deductible Amount | Band Label |
|----------------------|----------|-------------------|------------|
| 0 – 24,000 ETB | 0% | 0 | Tax-Free |
| 24,001 – 48,000 ETB | 10% | 2,400 | Low Income |
| 48,001 – 78,000 ETB | 20% | 6,000 | Middle Income |
| 78,001 – 120,000 ETB | 25% | 9,900 | Upper Middle |
| 120,001 – 168,000 ETB | 30% | 15,900 | High Income |
| Above 168,000 ETB | 30% | 15,900 | Very High Income |

**Formula:**
```
Annual Tax = (Taxable Annual Income × Band Rate) - Band Deductible Amount
Monthly Tax = Annual Tax ÷ 12
```

---

## Examples

### Example 1: Individual with Deduction
**Scenario:**
- Monthly Rent: 5,000 ETB
- Entity Type: INDIVIDUAL
- Property Type: HOUSE
- Claim Deduction: YES

**Calculation:**
1. Annual Gross: 5,000 × 12 = **60,000 ETB**
2. Deduction (40%): 60,000 × 0.40 = **24,000 ETB**
3. Taxable Income: 60,000 - 24,000 = **36,000 ETB**
4. Tax Band: 24,001 – 48,000 (10% rate, 2,400 deductible)
5. Annual Tax: (36,000 × 0.10) - 2,400 = **1,200 ETB**
6. Monthly Tax: 1,200 ÷ 12 = **100 ETB**
7. Effective Rate: 1,200 ÷ 60,000 = **2%**

### Example 2: Individual without Deduction
**Scenario:**
- Monthly Rent: 5,000 ETB
- Entity Type: INDIVIDUAL
- Property Type: HOUSE
- Claim Deduction: NO

**Calculation:**
1. Annual Gross: 5,000 × 12 = **60,000 ETB**
2. No Deduction Applied
3. Taxable Income: **60,000 ETB**
4. Tax Band: 48,001 – 78,000 (20% rate, 6,000 deductible)
5. Annual Tax: (60,000 × 0.20) - 6,000 = **6,000 ETB**
6. Monthly Tax: 6,000 ÷ 12 = **500 ETB**
7. Effective Rate: 6,000 ÷ 60,000 = **10%**

### Example 3: Tax-Free Income
**Scenario:**
- Monthly Rent: 2,000 ETB
- Entity Type: INDIVIDUAL
- Property Type: HOUSE
- Claim Deduction: NO

**Calculation:**
1. Annual Gross: 2,000 × 12 = **24,000 ETB**
2. Tax Band: 0 – 24,000 (0% rate)
3. Annual Tax: **0 ETB** (Tax-Free)
4. Monthly Tax: **0 ETB**

### Example 4: Business Entity
**Scenario:**
- Monthly Rent: 10,000 ETB
- Entity Type: BUSINESS

**Calculation:**
1. Annual Gross: 10,000 × 12 = **120,000 ETB**
2. Flat Rate: 30%
3. Annual Tax: 120,000 × 0.30 = **36,000 ETB**
4. Monthly Tax: 36,000 ÷ 12 = **3,000 ETB**
5. Effective Rate: **30%**

---

## Special Cases

### Mixed-Use Properties
- Deduction is allowed BUT triggers a warning
- Officer should verify the residential/commercial split
- Advisory note added: "Residential deduction applied — officer should verify commercial split."

### Multiple Income Sources
- System calculates rental income tax only
- Advisory note: "This estimate covers rental income only. Total tax liability may differ if the landlord has other income sources."

---

## Code Location

**Service Interface:**
`Backend/src/main/java/com/rentalpro/service/TaxCalculationService.java`

**Implementation:**
`Backend/src/main/java/com/rentalpro/service/impl/TaxCalculationServiceImpl.java`

**Tax Rules:**
`Backend/src/main/java/com/rentalpro/model/tax/TaxRule1395.java`

**Tax Bands:**
`Backend/src/main/java/com/rentalpro/model/tax/TaxBand.java`

---

## Key Features

1. **Accurate Calculations:** Based on official Ethiopian tax law
2. **Progressive Taxation:** Fair taxation based on income levels
3. **Deduction Support:** 40% deduction for residential properties
4. **Business Simplicity:** Flat 30% rate for business entities
5. **Advisory Notes:** Contextual warnings and guidance
6. **Rounding:** All amounts rounded to 2 decimal places
7. **Validation:** Prevents negative rent values

---

## Future Enhancements

The tax rules are currently hardcoded but designed to be admin-configurable in future sprints:
- Adjustable tax bands
- Configurable rates
- Custom deduction percentages
- Multiple tax rule versions

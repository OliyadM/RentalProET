# Officer Contract Review Page - Enhancement Proposal

## Current Problem
The officer contract review page (`/officer/contracts`) only shows contracts with status `PENDING_OFFICER_REVIEW`. Once approved or rejected, they disappear. This makes it impossible for officers to:
- Track what they've already reviewed
- Search through historical contracts
- Review rejected contracts
- Monitor contract compliance over time

## Available Contract Statuses
```
DRAFT                    → Landlord creating
PENDING_CONFIRMATION     → Waiting for tenant
PENDING_OFFICER_REVIEW   → Waiting for officer ✅ (currently shown)
ACTIVE                   → Approved by officer
REJECTED                 → Rejected by officer
UNDER_APPEAL             → Tenant appealed
UNDER_REVIEW             → Under review
TERMINATED               → Contract ended
EXPIRED                  → Contract expired
```

## Proposed Solution - Feasible & Important Features

### ✅ PHASE 1: ESSENTIAL (Implement Now)

#### 1. **Show All Contracts (Not Just Pending)**
- Default view: Show ALL contracts officer can review
- Officers need to see their review history
- **Backend**: Already exists - `GET /contracts/by-status/{status}`
- **Implementation**: Call endpoint for multiple statuses or create new endpoint

#### 2. **Status Filter** (CRITICAL)
```
- All Contracts (default)
- Pending Review (PENDING_OFFICER_REVIEW)
- Approved (ACTIVE)
- Rejected (REJECTED)
- Under Appeal (UNDER_APPEAL)
```
**Why**: Officers need to focus on pending work but also review history
**Backend**: Use existing `findByStatus()` method

#### 3. **Search by Text** (CRITICAL)
- Single search box: Search landlord name, tenant name, OR property address
- **Backend**: Need to add custom query method to repository
- **Implementation**: 
  ```java
  @Query("SELECT c FROM RentalContract c WHERE " +
         "LOWER(c.landlord.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(c.landlord.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(c.tenant.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(c.tenant.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(c.propertyAddress) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<RentalContract> searchContracts(@Param("search") String search);
  ```

#### 4. **Sort Options** (IMPORTANT)
```
- Newest First (default - by tenantConfirmedAt DESC)
- Oldest First (by tenantConfirmedAt ASC)
- Highest Rent (by monthlyRent DESC)
```
**Why**: Officers prioritize high-value contracts or oldest pending
**Backend**: Add sorting to repository query

#### 5. **Sub-City Filter** (IMPORTANT)
- Filter by property sub-city
- **Why**: Officers are assigned to specific sub-cities
- **Backend**: Add to query:
  ```java
  WHERE c.rentalUnit.property.subCity = :subCity
  ```

### ⚠️ PHASE 2: NICE TO HAVE (Later)

#### 6. **Date Range Filter**
- Filter by submission date (tenantConfirmedAt)
- **Why**: Useful for compliance audits
- **Complexity**: Medium - need date picker component

#### 7. **Anomaly Flag Filter**
- Filter contracts with anomaly flags
- **Problem**: Anomaly is on DECLARATIONS, not contracts
- **Workaround**: Show if contract has ANY flagged declarations
- **Complexity**: High - requires join with declarations table

#### 8. **Risk Score**
- Not currently implemented in the system
- Would require new calculation logic
- **Skip for now**

---

## Recommended Implementation Order

### Step 1: Backend Enhancement
Add to `RentalContractRepository.java`:
```java
@Query("SELECT c FROM RentalContract c WHERE " +
       "(:status IS NULL OR c.status = :status) AND " +
       "(:subCity IS NULL OR c.rentalUnit.property.subCity = :subCity) AND " +
       "(:search IS NULL OR " +
       "LOWER(c.landlord.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(c.landlord.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(c.tenant.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(c.tenant.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(c.propertyAddress) LIKE LOWER(CONCAT('%', :search, '%')))")
List<RentalContract> findContractsForOfficer(
    @Param("status") ContractStatus status,
    @Param("subCity") String subCity,
    @Param("search") String search,
    Sort sort
);
```

Add to `RentalContractController.java`:
```java
@GetMapping("/officer/all")
@PreAuthorize("hasAnyRole('SUBCITY_STAFF', 'ADMINISTRATOR')")
public ResponseEntity<List<ContractResponse>> getContractsForOfficer(
    @RequestParam(required = false) ContractStatus status,
    @RequestParam(required = false) String subCity,
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "tenantConfirmedAt,desc") String sort
) {
    return ResponseEntity.ok(contractService.getContractsForOfficer(status, subCity, search, sort));
}
```

### Step 2: Frontend Enhancement
Update `Frontend/src/pages/officer/Contracts.jsx`:
- Add filter bar with:
  - Search input (text)
  - Status dropdown (All, Pending, Approved, Rejected, Under Appeal)
  - Sub-city dropdown (get from properties or hardcode common ones)
  - Sort dropdown (Newest, Oldest, Highest Rent)
- Update API call to use new endpoint with filters
- Show status badge in table
- Keep approve/reject buttons only for PENDING_OFFICER_REVIEW status

### Step 3: API Service
Add to `Frontend/src/services/api.js`:
```javascript
getContractsForOfficer: (filters) => 
  api.get('/contracts/officer/all', { params: filters })
```

---

## Summary - What to Implement

### MUST HAVE (Phase 1):
1. ✅ Show all contracts (not just pending)
2. ✅ Status filter dropdown
3. ✅ Search by landlord/tenant/property
4. ✅ Sort by newest/oldest/highest rent
5. ✅ Sub-city filter

### SKIP FOR NOW:
- ❌ Date range filter (complex UI, low priority)
- ❌ Anomaly flag filter (requires declaration join, complex)
- ❌ Risk score (not implemented in system)

### RESULT:
Officers can:
- See all contracts they've reviewed
- Filter by status to focus on pending work
- Search for specific landlords/tenants/properties
- Sort by priority (newest, highest rent)
- Filter by their assigned sub-city
- Track their review history

This gives 80% of the value with 20% of the complexity.

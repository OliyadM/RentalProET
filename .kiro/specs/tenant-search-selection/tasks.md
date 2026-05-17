# Implementation Plan: Tenant Search and Selection

## Overview

This implementation plan breaks down the tenant search and selection feature into discrete coding tasks. The approach follows an incremental pattern: backend API first, then frontend integration, with testing integrated throughout. Each task builds on previous work to ensure continuous integration without orphaned code.

## Tasks

- [ ] 1. Create backend search infrastructure
  - [ ] 1.1 Create TenantSearchDTO class
    - Create `TenantSearchDTO.java` in the dto package
    - Add fields: UUID id, String name, String email
    - Add constructor, getters, and setters
    - _Requirements: 1.3, 1.4_
  
  - [ ] 1.2 Extend UserRepository with search method
    - Add `findByEmailContainingIgnoreCaseAndRole(String email, Role role)` method to UserRepository interface
    - Spring Data JPA will auto-implement the query
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ]* 1.3 Write property test for repository search
    - **Property 1: Search returns only matching tenants**
    - **Property 13: Case-insensitive email matching**
    - **Property 14: Results ordered alphabetically**
    - **Validates: Requirements 1.1, 6.2, 6.3**
  
  - [ ]* 1.4 Write unit tests for repository edge cases
    - Test empty query returns empty list
    - Test query with special characters
    - Test exact email match
    - Test partial email match
    - _Requirements: 1.1, 6.2_

- [ ] 2. Implement UserService layer
  - [ ] 2.1 Create UserService interface and implementation
    - Create `UserService.java` interface with `searchTenantsByEmail(String email)` method
    - Create `UserServiceImpl.java` with implementation
    - Inject UserRepository dependency
    - Map User entities to TenantSearchDTO objects
    - _Requirements: 1.1, 1.3_
  
  - [ ]* 2.2 Write property test for DTO mapping
    - **Property 2: Search results contain only safe fields**
    - **Validates: Requirements 1.3, 1.4**
  
  - [ ]* 2.3 Write unit tests for UserService
    - Test searchTenantsByEmail with valid query
    - Test DTO excludes sensitive fields (password, phone, address)
    - Test empty results handling
    - _Requirements: 1.3, 1.4_

- [ ] 3. Create UserController with search endpoint
  - [ ] 3.1 Implement UserController
    - Create `UserController.java` with @RestController annotation
    - Add GET `/api/users/search` endpoint with @GetMapping
    - Accept `email` query parameter
    - Validate query length (minimum 2 characters)
    - Return empty list for invalid queries
    - Call UserService.searchTenantsByEmail()
    - Return ResponseEntity with List<TenantSearchDTO>
    - _Requirements: 1.1, 1.2_
  
  - [ ]* 3.2 Write unit tests for UserController
    - Test search with valid query returns 200 and results
    - Test search with empty query returns empty list
    - Test search with 1-character query returns empty list
    - Test search with null query returns empty list
    - _Requirements: 1.1, 1.2_

- [ ] 4. Configure security for search endpoint
  - [ ] 4.1 Update SecurityConfig
    - Add `.requestMatchers("/api/users/search").hasRole("LANDLORD")` to security filter chain
    - Ensure endpoint requires authentication
    - _Requirements: 7.1, 7.2, 7.3, 7.4_
  
  - [ ]* 4.2 Write integration tests for security
    - Test LANDLORD role can access search endpoint
    - Test TENANT role receives 403 Forbidden
    - Test unauthenticated request receives 401 Unauthorized
    - Test invalid token receives 401 Unauthorized
    - _Requirements: 1.5, 7.2, 7.3_

- [ ] 5. Checkpoint - Backend complete
  - Ensure all backend tests pass
  - Manually test search endpoint with Postman or curl
  - Verify security configuration works correctly
  - Ask the user if questions arise

- [ ] 6. Create TenantSearch React component
  - [ ] 6.1 Create TenantSearch component file
    - Create `TenantSearch.jsx` in components folder
    - Set up component state: searchQuery, searchResults, isLoading, isOpen, searchError
    - Add props: onSelect, selectedTenant, error
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ] 6.2 Implement search input and debouncing
    - Create input field with onChange handler
    - Implement debounced search function using lodash.debounce or custom hook (300ms delay)
    - Call axios.get('/api/users/search') with email query parameter
    - Update searchResults state with response data
    - Handle loading state during API call
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [ ]* 6.3 Write property tests for debouncing
    - **Property 10: Search requests are debounced**
    - **Property 11: Rapid typing cancels previous requests**
    - **Validates: Requirements 5.1, 5.2**
  
  - [ ] 6.4 Implement search results dropdown
    - Render dropdown when isOpen is true and results exist
    - Display each result with tenant name and email
    - Limit visible results to 10 items with scrolling
    - Show "No results" message when searchResults is empty and query length >= 2
    - Show loading indicator when isLoading is true
    - _Requirements: 2.1, 2.2, 2.4_
  
  - [ ]* 6.5 Write property tests for UI rendering
    - **Property 3: UI displays all result information**
    - **Property 4: UI limits visible results**
    - **Property 12: Loading indicator lifecycle**
    - **Validates: Requirements 2.1, 2.4, 5.3, 5.4**
  
  - [ ] 6.6 Implement tenant selection and clear
    - Add onClick handler to result items that calls onSelect(tenant)
    - Update searchQuery to display selected tenant name and email
    - Close dropdown on selection (setIsOpen(false))
    - Add clear button that resets state and calls onSelect(null)
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [ ]* 6.7 Write property tests for selection behavior
    - **Property 5: Tenant selection populates form**
    - **Property 6: Dropdown closes on selection**
    - **Property 7: Clear action resets state**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
  
  - [ ] 6.8 Implement error handling
    - Add try-catch around API call
    - Set searchError state on API failure
    - Display error message in UI
    - Display validation error from props
    - _Requirements: 2.3_
  
  - [ ]* 6.9 Write unit tests for TenantSearch component
    - Test input change triggers search
    - Test search displays loading indicator
    - Test search displays results
    - Test "no results" message displays correctly
    - Test error message displays on API failure
    - Test clicking result calls onSelect
    - Test clear button resets state
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.3, 3.4, 5.3_

- [ ] 7. Update CreateContract component
  - [ ] 7.1 Modify CreateContract form state
    - Update formData state to use tenantId (UUID string) instead of tenant object
    - Add selectedTenant state to track selected tenant object
    - Add errors state for validation messages
    - _Requirements: 4.1_
  
  - [ ] 7.2 Integrate TenantSearch component
    - Import TenantSearch component
    - Replace manual tenant input fields (name, email, phone) with TenantSearch component
    - Pass onSelect handler that updates selectedTenant and formData.tenantId
    - Pass selectedTenant and errors.tenant as props
    - _Requirements: 3.1, 4.1_
  
  - [ ] 7.3 Implement tenant selection handler
    - Create handleTenantSelect function
    - Update selectedTenant state with selected tenant object
    - Update formData.tenantId with tenant.id
    - Clear tenant validation error when tenant is selected
    - _Requirements: 3.1, 4.1_
  
  - [ ]* 7.4 Write property test for form integration
    - **Property 8: Contract submission sends tenant UUID**
    - **Validates: Requirements 4.1**
  
  - [ ] 7.5 Add form validation for tenant selection
    - In handleSubmit, check if formData.tenantId is empty
    - If empty, set errors.tenant and prevent submission
    - Display validation error below TenantSearch component
    - _Requirements: 4.2_
  
  - [ ] 7.6 Handle successful contract creation
    - On successful API response, reset formData to initial state
    - Reset selectedTenant to null
    - Clear all errors
    - Display success message
    - _Requirements: 4.3_
  
  - [ ]* 7.7 Write property test for form reset
    - **Property 9: Successful submission resets form**
    - **Validates: Requirements 4.3**
  
  - [ ] 7.8 Handle contract creation errors
    - On API error response, extract error message
    - Display backend validation error (e.g., "Tenant not found")
    - Preserve form state so user can correct and retry
    - _Requirements: 4.4_
  
  - [ ]* 7.9 Write unit tests for CreateContract updates
    - Test tenant selection updates form state
    - Test form validation prevents submission without tenant
    - Test successful submission resets form
    - Test error response displays error message
    - Test tenant selection clears validation error
    - _Requirements: 3.1, 4.1, 4.2, 4.3, 4.4_

- [ ] 8. Add styling for TenantSearch component
  - [ ] 8.1 Create CSS for TenantSearch
    - Style search input field with focus states
    - Style dropdown container with max-height and scrolling
    - Style result items with hover effects
    - Style loading indicator
    - Style error messages
    - Style clear button
    - Ensure responsive design for mobile
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 9. Final checkpoint and integration testing
  - [ ]* 9.1 Write end-to-end integration tests
    - Test complete flow: search → select → submit contract
    - Test error flow: search fails → displays error → retry succeeds
    - Test validation flow: submit without tenant → error → select tenant → submit succeeds
    - _Requirements: All requirements_
  
  - [ ] 9.2 Manual testing checklist
    - Test search with various email queries
    - Test search with special characters
    - Test rapid typing and debouncing
    - Test selecting different tenants
    - Test clearing selection
    - Test form validation
    - Test successful contract creation
    - Test error handling for network failures
    - Test with LANDLORD and TENANT roles
    - Test on different browsers
  
  - [ ] 9.3 Final checkpoint
    - Ensure all tests pass (unit, property, integration)
    - Verify no console errors in browser
    - Verify no backend errors in logs
    - Ask the user if questions arise or if ready to deploy

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Backend tasks (1-4) should be completed before frontend tasks (6-8)
- Checkpoint at task 5 ensures backend is solid before frontend work
- Property tests validate universal correctness properties across randomized inputs
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end flows

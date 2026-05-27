# Notification System — User Verification Lifecycle Design

## Files to Modify (No New Files Required)

```
Backend/src/main/java/com/rentalpro/
├── model/enums/
│   └── NotificationType.java          MODIFY — add 4 new enum values
├── service/impl/
│   └── UserServiceImpl.java           MODIFY — inject NotificationService, add 4 trigger calls

Frontend/src/components/
└── NotificationBell.jsx               MODIFY — add routing for 4 new types
```

---

## Change 1 — `NotificationType.java`

Add four new values to the existing enum. Place them in a new `// Account lifecycle` section for clarity.

```java
public enum NotificationType {
    // Contract lifecycle
    CONTRACT_CONFIRMED,
    CONTRACT_REJECTED,
    CONTRACT_SUBMITTED,

    // Appeals
    APPEAL_SUBMITTED,
    APPEAL_RESOLVED,
    APPEAL_REJECTED,

    // Declarations
    DECLARATION_ANOMALY,

    // Properties
    PROPERTY_VERIFIED,

    // Account lifecycle  ← NEW SECTION
    ACCOUNT_CREATED,          // User registered → notify user
    PROFILE_PENDING_REVIEW,   // Profile submitted → notify officers
    ACCOUNT_VERIFIED,         // Officer approved → notify user
    ACCOUNT_REJECTED,         // Officer rejected → notify user

    // General
    SYSTEM
}
```

---

## Change 2 — `UserServiceImpl.java`

### 2a — Inject `NotificationService`

Add to the field declarations (Lombok `@RequiredArgsConstructor` handles injection automatically):

```java
private final NotificationService notificationService;
```

### 2b — Trigger in `register()` — REQ-1

Insert after `userRepository.save(user)`, before the `authenticationManager.authenticate()` call:

```java
// Notify the new user — wrapped so notification failure never blocks registration
try {
    notificationService.send(
        user.getId(),
        NotificationType.ACCOUNT_CREATED,
        "Welcome to RentalPro ET! Your account has been created. " +
        "Please complete your profile to get started.",
        user.getId()
    );
} catch (Exception e) {
    log.warn("Failed to send ACCOUNT_CREATED notification to user {}: {}", user.getId(), e.getMessage());
}
```

**Why try/catch here:** `notificationService.send()` does a `userRepository.findById()` internally. Since `user` was just saved in the same transaction, the entity is guaranteed to exist. However, wrapping in try/catch satisfies REQ-5 — any unexpected failure in the notification layer must not roll back the registration.

### 2c — Trigger in `updateProfile()` — REQ-2

Insert immediately after `user.setAccountStatus(AccountStatus.PENDING_VERIFICATION)`:

```java
// Fan-out to officers in the user's sub-city — REQ-2
// Only fan out if the user has a sub-city assigned (LANDLORD/TENANT may not)
if (user.getSubCityZone() != null && !user.getSubCityZone().isBlank()) {
    try {
        String roleLabel = user.getRole().name().charAt(0)
                + user.getRole().name().substring(1).toLowerCase();
        String msg = String.format(
                "%s %s (%s) has submitted their profile for verification in %s.",
                user.getFirstName(), user.getLastName(), roleLabel, user.getSubCityZone());
        notificationService.sendToSubCityOfficers(
                user.getSubCityZone(),
                NotificationType.PROFILE_PENDING_REVIEW,
                msg,
                user.getId());
    } catch (Exception e) {
        log.warn("Failed to send PROFILE_PENDING_REVIEW notification for user {}: {}", user.getId(), e.getMessage());
    }
}
```

### 2d — Triggers in `verifyProfile()` — REQ-3 and REQ-4

Insert after `userRepository.save(user)` at the end of `verifyProfile()`, replacing the bare `return mapToProfileResponse(user)`:

```java
// Send outcome notification to the user — REQ-3 / REQ-4
try {
    if (request.getStatus() == AccountStatus.VERIFIED) {
        notificationService.send(
                user.getId(),
                NotificationType.ACCOUNT_VERIFIED,
                "Your account has been verified! You now have full access to RentalPro ET.",
                user.getId());
    } else if (request.getStatus() == AccountStatus.REJECTED) {
        String msg = String.format(
                "Your account verification was rejected. Reason: %s. " +
                "Please update your profile and resubmit.",
                request.getRejectionReason());
        notificationService.send(
                user.getId(),
                NotificationType.ACCOUNT_REJECTED,
                msg,
                user.getId());
    }
} catch (Exception e) {
    log.warn("Failed to send verification outcome notification to user {}: {}", user.getId(), e.getMessage());
}

return mapToProfileResponse(user);
```

---

## Change 3 — `NotificationBell.jsx`

### 3a — Add new types to `resolveNavPath()`

Extend the existing function with a new block for account lifecycle types:

```js
const accountTypes = [
  "ACCOUNT_CREATED",
  "ACCOUNT_VERIFIED",
  "ACCOUNT_REJECTED",
];

if (accountTypes.includes(type)) {
  // Route to the user's own profile page when it exists
  // For now return null (informational — no navigation target yet)
  return null;
}

if (type === "PROFILE_PENDING_REVIEW") {
  // Officers navigate to the pending profiles list
  if (userRole === "SUBCITY_STAFF" || userRole === "ADMINISTRATOR") {
    return "/officer/profiles";   // update this path when the page is built
  }
  return null;
}
```

---

## Execution Order Within Transactions

The notification calls are placed **after** the primary `userRepository.save()` in each method. This is intentional:

1. The user entity is saved first — the notification's `relatedEntityId` (the user's UUID) is guaranteed to exist in the database before the notification row references it.
2. The try/catch wrapper means a notification failure cannot cause a rollback of the outer `@Transactional` method.
3. `notificationService.send()` opens its own `@Transactional` context (it is annotated `@Transactional` in `NotificationServiceImpl`). Since the outer transaction is already active, Spring will use the same transaction by default (REQUIRED propagation). The try/catch ensures that if the notification transaction fails, the exception is caught before it can propagate up and trigger a rollback of the outer transaction.

---

## What Is NOT Changed

- `NotificationService` interface — no new methods needed. `send()` and `sendToSubCityOfficers()` cover all four triggers.
- `NotificationServiceImpl` — no changes. The existing implementation handles the new types automatically since `NotificationType` is just an enum stored as a string.
- `NotificationController` — no changes. The existing endpoints serve all notification types.
- `Notification` entity — no changes. The `message` field (500 chars) is sufficient for all new messages.
- `NotificationRepository` — no changes.
- Database schema — no migration needed. `ddl-auto: update` will handle the new enum values automatically since they are stored as `VARCHAR` strings.

---

## Testing Checklist

| Scenario | Expected Notification | Recipient |
|----------|----------------------|-----------|
| New LANDLORD registers | ACCOUNT_CREATED | Landlord |
| New TENANT registers | ACCOUNT_CREATED | Tenant |
| New SUBCITY_STAFF registers | ACCOUNT_CREATED | Officer |
| Landlord completes profile (all fields filled) | PROFILE_PENDING_REVIEW | All officers in landlord's sub-city |
| Tenant completes profile (no subCityZone) | No notification sent | — |
| Officer sets status to VERIFIED | ACCOUNT_VERIFIED | The verified user |
| Officer sets status to REJECTED | ACCOUNT_REJECTED (with reason) | The rejected user |
| Notification service throws exception during registration | Registration succeeds, warning logged | — |

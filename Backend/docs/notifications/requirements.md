# Notification System — User Verification Lifecycle Requirements

## Current State (Scan Results)

### What exists
- A fully operational `NotificationService` with `send()` and `sendToSubCityOfficers()` methods
- A `Notification` JPA entity persisted to the `notifications` table
- A `NotificationType` enum with 10 existing types
- A `NotificationController` exposing `GET /notifications/my-notifications`, `GET /notifications/unread-count`, `PUT /notifications/{id}/read`, `PUT /notifications/read-all`
- Active notification triggers in `RentalContractServiceImpl` (contract lifecycle) and `AppealServiceImpl` (appeal lifecycle)

### What is missing
The `UserServiceImpl` has **zero notification calls**. The entire user verification lifecycle — registration, profile submission, officer approval, officer rejection — produces no notifications for any party. Specifically:

| Event | Current Behavior | Required Behavior |
|-------|-----------------|-------------------|
| User registers | Silent | Notify user: account created, complete your profile |
| User submits complete profile (`PENDING_VERIFICATION`) | Silent | Notify all officers in user's sub-city: new profile awaiting review |
| Officer sets status to `VERIFIED` | Silent | Notify user: account verified, platform access granted |
| Officer sets status to `REJECTED` | Silent | Notify user: account rejected, include rejection reason |

### Delivery Mechanism
All notifications use the existing **database-persisted in-app bell** mechanism. There is no email or WebSocket infrastructure. All new notifications must use `notificationService.send()` or `notificationService.sendToSubCityOfficers()` — no new delivery channel is introduced.

---

## Requirements

### REQ-1 — Registration Welcome Notification
When a new user successfully registers (any role: LANDLORD, TENANT, SUBCITY_STAFF), a notification MUST be sent to that user immediately after their account is saved.

- **Recipient:** the newly registered user
- **Trigger:** end of `UserServiceImpl.register()`, after `userRepository.save()`
- **Message:** `"Welcome to RentalPro ET! Your account has been created. Please complete your profile to get started."`
- **Type:** `NotificationType.ACCOUNT_CREATED` (new enum value)
- **relatedEntityId:** the new user's own UUID

**Exception:** The `DatabaseSeeder` creates the ADMINISTRATOR account programmatically. The seeder MUST NOT trigger this notification — admin accounts are system-created, not self-registered.

---

### REQ-2 — Profile Submitted for Review Notification (Officer Fan-out)
When a user completes their profile and the system transitions their status to `PENDING_VERIFICATION` (inside `UserServiceImpl.updateProfile()`), a notification MUST be fanned out to all `SUBCITY_STAFF` officers whose `subCityZone` matches the user's sub-city.

- **Recipients:** all `SUBCITY_STAFF` users with `subCityZone` matching the registering user's sub-city
- **Trigger:** inside `updateProfile()`, immediately after `user.setAccountStatus(AccountStatus.PENDING_VERIFICATION)`
- **Message:** `"{firstName} {lastName} ({role}) has submitted their profile for verification in {subCity}."`
- **Type:** `NotificationType.PROFILE_PENDING_REVIEW` (new enum value)
- **relatedEntityId:** the user's UUID

**Edge case:** If the user has no `subCityZone` set (e.g. a TENANT who is not sub-city scoped), the fan-out MUST be skipped silently. No error should be thrown.

---

### REQ-3 — Account Verified Notification
When an officer sets a user's status to `VERIFIED` inside `UserServiceImpl.verifyProfile()`, a notification MUST be sent to the verified user.

- **Recipient:** the user whose profile was just verified
- **Trigger:** inside `verifyProfile()`, after `userRepository.save()`, when `request.getStatus() == AccountStatus.VERIFIED`
- **Message:** `"Your account has been verified! You now have full access to RentalPro ET."`
- **Type:** `NotificationType.ACCOUNT_VERIFIED` (new enum value)
- **relatedEntityId:** the user's UUID

---

### REQ-4 — Account Rejected Notification
When an officer sets a user's status to `REJECTED` inside `UserServiceImpl.verifyProfile()`, a notification MUST be sent to the rejected user including the rejection reason.

- **Recipient:** the user whose profile was rejected
- **Trigger:** inside `verifyProfile()`, after `userRepository.save()`, when `request.getStatus() == AccountStatus.REJECTED`
- **Message:** `"Your account verification was rejected. Reason: {rejectionReason}. Please update your profile and resubmit."`
- **Type:** `NotificationType.ACCOUNT_REJECTED` (new enum value)
- **relatedEntityId:** the user's UUID

---

### REQ-5 — Notification Must Not Block the Primary Operation
All four notification calls MUST be wrapped so that a failure in the notification layer (e.g. database error writing to the `notifications` table) does NOT roll back the primary transaction (registration, profile update, or verification). Notifications are secondary — the core operation must always succeed.

---

### REQ-6 — No New Delivery Channel
All notifications use the existing in-app bell mechanism only. No email sending, no WebSocket push, no SMS. This is a deliberate scope constraint for this iteration.

---

### REQ-7 — Frontend Bell Navigation
The `NotificationBell.jsx` component's `resolveNavPath()` function currently has no routing for the new account-lifecycle notification types. It MUST be updated so:
- `ACCOUNT_CREATED` → no navigation (informational only)
- `PROFILE_PENDING_REVIEW` → officer navigates to the pending profiles list (route TBD)
- `ACCOUNT_VERIFIED` → user navigates to their own profile page (route TBD)
- `ACCOUNT_REJECTED` → user navigates to their own profile page to resubmit (route TBD)

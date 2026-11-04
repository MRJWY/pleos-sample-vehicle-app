# DriveClub initial Android MVP

Branch: `feat/driveclub-mvp`. The launcher now opens DriveClub. The existing
Pleos SDK diagnostic screen is available from the demo's parked state; Android
Back returns to DriveClub. The SDK ViewModel is instantiated only when opening
the diagnostic screen, so a missing Vehicle Service does not prevent the demo.

## Implemented

- Four-week evaluation with inclusive 90-point threshold and three driving days
  per week. Missing days, delayed scores, insufficient observation and failed
  scores are separate states. An unresolved week never joins streaks.
- Current/best streak, source-score average, same-source/model demo ranking and
  shared ranks; distance is not a tiebreaker.
- Normal, low-score and missing-score fixtures, visibly labeled as test data.
- Local consent, a small local reflection board and erasure action.
- A durable, single-participant/single-season simulated reward receipt. Repeated
  requests and process restarts do not append additional receipts. No real coupon
  is issued. Clearing demo data also resets this simulated receipt.
- Pure season-prize allocation with shared boundary slots and a 200,000 KRW cap;
  nine unit tests cover the season rules, missing data and boundary ties.
- A demo parked/driving switch. The initial unknown/driving state restricts
  detailed interactions. This is NOT connected to the vehicle's driving status.

## Run and verify

Use JDK 17, Android SDK 35 and an Android Studio environment able to resolve the
existing Google, Maven, Gradle and Pleos dependencies:

```bash
bash gradlew testDebugUnitTest assembleDebug
bash gradlew installDebug
```

1. Launch, switch the test state to parked and consent to local demo storage.
2. Check four passed weeks in My Record, then choose the low-score fixture:
   week 3 fails, best streak is 2, current streak is 1.
3. Choose missing-score fixture: week 2 is pending, reward is ineligible.
4. Choose normal fixture; request the simulated reward, restart, confirm the
   receipt is retained and cannot be issued again.
5. Add a reflection, restart and confirm it remains. Erase demo data and confirm
   consent, reflections and simulated receipt are cleared.
6. Return to driving/unknown test state; detailed screens disappear.
7. Open SDK diagnostics only on a safely parked Pleos device/emulator.

## Verification status in this workspace

`git pull --ff-only`: already up to date before implementation.
`bash gradlew testDebugUnitTest assembleDebug`: blocked before compilation because
services.gradle.org could not be reached (`Network is unreachable`). No passing
unit test, compiled APK, emulator screenshot or real-device behavior is claimed.

## Next integration work / limitations

This is a local vertical slice, not the complete service-plan acceptance scope.
There is no login, backend, real score source, driver attribution, score refresh,
Odometer/CarInfo adapter, automatic driving-state gate, cross-device community,
comments/report/block workflow, administrator ledger or real coupon provider.
The existing SDK explorer remains available for capability inspection; its values
are not presented as personal driving records. No raw location/video is collected.

The fixed fixtures cover a completed season, not a live season clock. A production
season needs timezone-aware dates, source revision/versioning, identity verification,
consent-scoped ingestion, server-side eligibility and atomic idempotent payouts.
Production rewards must remain blocked until those pieces are verified. The local
preference store is demo state, not an authoritative or tamper-resistant ledger.
The season-prize function is unit-tested in source but not connected to real payouts.

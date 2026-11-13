# 플레오마루: pet garden MVP

The launcher now opens **플레오마루**, an original pet-and-garden experience.
A woolly companion welcomes the user into a small forest clearing. The earlier
DriveClub season engine is reused for optional score-history rewards; its former
ranking/coupon screen is no longer the launcher experience.

## Playable local slice

- Create a resident by choosing a name, species (rabbit, bear, cat), and lapel pin (leaf, sun, moon). Existing gardens add a resident without changing pet or rewards.
- Name and adopt one sheep companion; receive 10 starter leaves.
- Three daily care actions (pet, feed, rest), each giving 5 XP and 3 leaves once
  per Korea calendar day. No driving required; no absence penalty or pet decay.
- A test 12 km journey gives 12 XP/leaves once per day. The engine shares a
  20 XP/leaves daily distance cap across journeys, with persistent journey IDs.
- Normal/low/missing test score histories reuse the four-week validation engine.
  A fully eligible test season grants 15 leaves once, independently of scenario
  switching. Missing/low scores never damage the pet.
- Three original garden decorations, purchase-once ownership and free placement/
  storage. Original Compose Canvas artwork changes with placed decorations.
- Local village preview with your resident, your sheep and the fixed NPC neighbor 플레몽.
  No online presence or multiplayer is claimed.
- Names, growth, care limits, reward IDs, ownership and placement persist in a
  separate preference store. UI state changes only after a successful save.
- Confirmed erasure of this pet garden's local state and consent. Older DriveClub
  demo preferences are separate and are not migrated or erased by this action.

## Driving restrictions and data provenance

This build remains a development demo. Distance and scores are fixtures, not SDK
measurements. It does not collect location or actual vehicle history. The parked
switch is a developer simulation; every Activity creation starts restricted and
hides the game until the test switch is enabled. A production release needs real
CarUxRestrictions integration and lifecycle verification, including SDK diagnostics.
The manual switch is not an acceptable production safety gate.

The existing SDK explorer remains available from the parked development screen.
The Vehicle ViewModel is lazily opened there, not required to start the garden.

## Build / verification

JDK 17 and Android SDK 35:

```bash
bash gradlew testDebugUnitTest assembleDebug
bash gradlew installDebug
```

The 22 garden, resident and season tests passed using the Kotlin 2.0 compiler and JUnit on the JVM.
The Android APK and device workflow still need verification in Android Studio.

## Manual acceptance checks

1. Launch: game is hidden in driving/unknown test mode. Enable parked test mode.
2. Consent, create your resident, then name and adopt your sheep. Check starter leaves = 10.
3. Care three times: XP = 15, leaves = 19. Each action is unavailable until the
   next Korea calendar day. Driving is not required.
4. Buy flowers: leaves = 9; flowers appear. Store and place for no extra charge.
5. Apply the test return journey: XP = 27, leaves = 21. Repeat/restart: no payout.
6. Low/missing scores: season reward disabled; pet unchanged. Normal scores:
   grant 15 leaves once. Switch scenarios and restart: cannot claim again.
7. Visit the village and greet 플레몽. The fixed NPC addresses your resident by name.
8. Restart and check name, XP, inventory and placed items. Toggle driving mode:
   game scene and controls disappear.
9. Delete: cancel preserves the garden; confirm removes garden data and consent.

## Next scope

Real Odometer/CarInfo + verified driver attribution; approved safety-score source;
automatic platform driving restrictions; server-authoritative reward idempotency;
authenticated shared village and presence; richer animation, audio and content.
Local time/preferences can be altered, so local rewards are demonstration state,
not trusted multiplayer economy. The game works without safety-score integration.

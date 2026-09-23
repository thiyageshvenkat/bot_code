# BIOBUZZ Robot Setup and Validation

This code compiles and its rule/model tests run on a computer. It has not been
calibrated or field-tested on the physical robot. Treat every value marked
`TUNE` in the mechanism-specific files under the `constants` package as a
starting placeholder.

## Required Robot Configuration names

| Name | Expected device |
| --- | --- |
| `front_left_drive` | mecanum drive motor |
| `front_right_drive` | mecanum drive motor |
| `back_left_drive` | mecanum drive motor |
| `back_right_drive` | mecanum drive motor |
| `pinpoint` | goBILDA Pinpoint odometry computer |
| `intake` | intake motor with current sensing |
| `magazine_gate` | single-element index servo |
| `flywheel_left`, `flywheel_right` | encoder-equipped launcher motors |
| `feeder` | launcher feeder motor |
| `hood` | launcher angle servo |
| `flower_lift` | encoder-equipped lift motor |
| `flower_gate` | top-deposit servo |
| `Webcam 1` | USB webcam aimed at hive cell tags |

Optional digital inputs are `intake_beam`, `magazine_exit_beam`,
`flower_bottom_limit`, and `flower_top_limit`. The code can run without them,
but beam counting then requires the operator to keep the inventory model honest,
and the lift relies only on encoder limits.

## Bring-up order

1. Put the robot on blocks, remove all scoring elements, disconnect launcher
   belts if practical, and verify an emergency stop is within reach.
2. Confirm each configuration name and motor/servo port before initializing an
   OpMode. A missing required device intentionally prevents initialization.
3. Run **BIOBUZZ Pit Diagnostics**. Powered checks require holding gamepad 2
   `START`; releasing it stops drive/intake/shooter and faults a moving lift.
4. At low power, verify positive drive, strafe, and turn directions. Correct
   motor directions in `pedroPathing/Constants.java`, never by swapping gamepad
   signs until the wheel convention is correct.
5. Push each beam break and limit switch by hand. Confirm telemetry changes in
   the expected direction before connecting a mechanism load.
6. Measure Pinpoint X/Y offsets, encoder directions, track width, wheel radius,
   and motion constraints using the Pedro tuning OpModes.
7. Determine safe closed/open endpoints for every servo without driving into a
   hard stop. Update the relevant mechanism config one mechanism at a time.
8. Tune the intake jam-current threshold using logged free-running and stalled
   current. Leave margin for battery voltage and mechanism wear.
9. Characterize launcher speed and hood angle from multiple measured distances.
   Replace the two-point `ShotModel` values only after repeatable trials.
10. Measure the actual start, shooting, and loading-zone park poses. Test each
    alliance route slowly with no scoring elements, then add one preload at a
    time.

## Competition controls

### Driver (gamepad 1)

- Left stick: field-centric translation
- Right stick X: turn
- Right trigger: precision speed
- Left bumper: replace manual turn with hive-tag aiming

### Operator (gamepad 2)

- `A`: collect pollen
- `X`: collect your selected alliance nectar
- `Y`: reverse intake and reject the newest tracked element
- `B`: stop intake
- Right bumper: spin up and set hood from visible-cell range
- Right trigger: request one hive shot, only when tag-aligned and at speed
- D-pad up: stage the next legal element for flower scoring
- D-pad down: deposit the staged flower element
- Left bumper: stow flower lift

During TeleOp initialization, D-pad up/down corrects the starting inventory.
Choose the red or blue OpMode matching the Driver Station alliance so opposing
nectar is rejected by the inventory model.

## Pre-match checklist

- Pull the latest official manual and Team Updates; rules can change.
- Confirm the Driver Station alliance matches the chosen TeleOp/Auto.
- Confirm exactly four preload pollen in both the robot and init telemetry.
- Test E-stop, battery retention, wheel/odometry fasteners, beam breaks, lift
  limits, camera stream, and tag alignment.
- Dry-run autonomous from the taped start pose and verify the loading-zone park.
- Inspect flywheels, guards, hood, feeder, flower gate, and every moving wire.
- Keep a conservative park-only autonomous available until shot and path tuning
  are repeatable on a regulation field.

## Build commands

From the repository root, with `ANDROID_HOME` set to the installed Android SDK:

```powershell
.\gradlew.bat :TeamCode:testDebugUnitTest :TeamCode:assembleDebug
```

The TeamCode APK is then written under `TeamCode/build/outputs/apk/debug/`.

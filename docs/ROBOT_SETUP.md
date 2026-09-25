# BIOBUZZ Robot Setup and Validation

This code compiles and its rule/model tests run on a computer. It has not been
calibrated or field-tested on the physical robot. Treat every value marked
`TUNE` in `RobotConfig` as a starting placeholder.

## Required Robot Configuration names

| Name | Expected device |
| --- | --- |
| `front_left_drive` | mecanum drive motor |
| `front_right_drive` | mecanum drive motor |
| `back_left_drive` | mecanum drive motor |
| `back_right_drive` | mecanum drive motor |
| `pinpoint` | goBILDA Pinpoint odometry computer |
| `intake` | single intake motor |
| `flywheel_left`, `flywheel_right` | encoder-equipped launcher motors |
| `feeder` | launcher feeder motor |
| `hood` | launcher angle servo |
| `limelight` | Limelight 3A running pollen and 36h11 AprilTag pipelines |

No beam-break or magazine sensor is documented. Manual inventory correction and
automatic intake lockout remain disabled, so CAD must prevent control of a fifth
element until sensors make the software inventory trustworthy.

## Bring-up order

1. Put the robot on blocks, remove all scoring elements, disconnect launcher
   belts if practical, and verify an emergency stop is within reach.
2. Confirm each configuration name and motor/servo port before initializing an
   OpMode. A missing required device intentionally prevents initialization.
3. Run **BIOBUZZ Pit Diagnostics**. Powered checks require holding gamepad 2
   `START`; releasing it stops drive, intake, and shooter.
4. At low power, verify positive drive, strafe, and turn directions. Correct
   motor directions in `pedroPathing/Constants.java`, never by swapping gamepad
   signs until the wheel convention is correct.
5. Configure Limelight pipeline 0 for the `yellow_pollen` detector and pipeline
   1 for 36h11 AprilTags. Confirm the diagnostic sees the correct own-alliance
   Hive Cell tag ranges: red 30-37 and blue 38-45.
6. Measure Pinpoint X/Y offsets, encoder directions, track width, wheel radius,
   and motion constraints using the Pedro tuning OpModes.
7. Determine safe hood endpoints without driving the servo into a hard stop.
8. Characterize launcher speed and hood angle from multiple measured distances.
   Replace the two-point `ShotModel` values only after repeatable trials.
9. Validate the red waypoints imported from
    `biobuzz_HARDCODED_LINES_WITH_CORNER_CURVE.pp` and the blue mirrored route.
    Test slowly with no scoring elements, then add one preload at a time.

## Competition controls

### Driver (gamepad 1)

- Left stick: field-centric translation
- Right stick X: turn
- Right trigger: precision speed

### Operator (gamepad 2)

- `A` or `X`: run intake inward
- `Y`: reverse intake
- `B` or no intake button: stop intake
- Hold right bumper: select Hive tags and keep the shooter spinning
- Right trigger: feed one tracked element when the flywheels are ready

Choose the red or blue OpMode matching the Driver Station alliance so opposing
nectar is rejected by the inventory model.

## Pre-match checklist

- Pull the latest official manual and Team Updates; rules can change.
- Confirm the Driver Station alliance matches the chosen TeleOp/Auto.
- Confirm exactly four preload pollen in both the robot and init telemetry.
- Test E-stop, battery retention, wheel/odometry fasteners, Limelight results,
  intake direction, and launcher direction.
- Dry-run autonomous from the taped start pose. Verify it identifies the
  upward-facing own-alliance Cell, turns toward its tag cluster, pauses during a
  Hive tip, targets the newly upward Cell, and reaches the loading-zone park.
- Inspect flywheels, guards, hood, feeder, intake, and every moving wire.
- Keep a conservative park-only autonomous available until shot and path tuning
  are repeatable on a regulation field.

## Build commands

From the repository root, with `ANDROID_HOME` set to the installed Android SDK:

```powershell
.\gradlew.bat :TeamCode:testDebugUnitTest :TeamCode:assembleDebug
```

The TeamCode APK is then written under `TeamCode/build/outputs/apk/debug/`.

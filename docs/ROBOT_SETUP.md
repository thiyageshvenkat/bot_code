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
Sensorless TeleOp displays its inventory as untracked and permits operator-requested
feed pulses regardless of that count. It does not reset the count to four at INIT.
Auto still tracks the four assumed preloads and cannot feed a fifth one.

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
   1 for 36h11 AprilTags, **82.55 mm (3.25 inch)** marker size, and **Full 3D**.
   Confirm the correct own-alliance Hive Cell tag ranges: red 30-37, blue 38-45.
   The pit diagnostic currently displays red targets; use blue Auto INIT for blue checks.
6. Measure Pinpoint X/Y offsets, encoder directions, track width, wheel radius,
   and motion constraints using the Pedro tuning OpModes.
7. Determine safe hood endpoints without driving the servo into a hard stop.
8. Characterize launcher speed and hood angle from multiple measured distances.
   Replace the two-point `ShotModel` values only after repeatable trials.
9. Validate the red waypoints imported from
    `biobuzz_HARDCODED_LINES_WITH_CORNER_CURVE.pp` and the blue mirrored route.
    Test slowly with no scoring elements, then add one preload at a time.

### Hive camera verification (unloaded robot first)

The adapter uses Limelight's documented optical coordinates (camera X right,
Y down, Z forward) and its per-tag camera-space poses, not VisionPortal's
`ftcPose.roll` or a static-field `botpose`. It is written against the documented
2026 coordinate convention; re-check this adapter before installing firmware
that changes those axes. Mount the camera upright (not sideways/inverted),
looking along the launch direction. A pitched-up camera is allowed, but the
horizontal aim offset and shot table must be calibrated at the fixed Auto position.

- Check both stable Hive positions: the downward Cell must not be selected,
  including when its tags and the upward Cell's tags are visible together.
- Hide individual tags. The estimated opening bearing should stay consistent;
  disagreeing per-tag poses deliberately produce no target.
- Turn/tilt the Hive while keeping it horizontally centered. Auto must restart
  qualification when the opening position or 3D rotation changes appreciably.
- Disconnect the camera, freeze its input, or select the wrong pipeline. No new
  automatic feed should start; a pulse already started still finishes normally.
- Verify a target to the right makes the unloaded robot turn right. Do not
  compensate incorrect drive motor directions by reversing the aim gain.
- Tune pose-drift limits, stable-observation time, and post-feed delay using
  camera observations of real ball flight and Hive tips. A motion estimate is
  not physical confirmation that the damper reached its stop.

Opening geometry comes from SDK 12's `AprilTagGameDatabase.getBioBuzzCluster`:
member X offsets -6.50, -2.75, 2.75, 6.50 inches; Y 7.1874; Z -5.622.
Each tag is transformed to the same opening point before detections are combined.
Java tests cover synthetic geometry and firing qualification; they do not validate
the installed camera firmware, mount, tag-size setting, or actual ballistic accuracy.

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
- Right trigger: request one feed pulse when the flywheels are ready; release
  and press again if the previous request was made before spin-up completed

TeleOp is manually aimed: Hive telemetry is advisory, not an automatic aim or
firing interlock. The operator must select a legal opening and a calibrated shot
position. Keep a clear launcher during tests because sensorless control can dry-feed.

Choose the red or blue OpMode matching the Driver Station alliance so opposing
nectar is rejected by the inventory model when tracking is enabled. This does
not physically identify or reject nectar entering the intake.

## Pre-match checklist

- Pull the latest official manual and Team Updates; rules can change.
- Confirm the Driver Station alliance matches the chosen TeleOp/Auto.
- Confirm exactly four preload pollen in both the robot and init telemetry.
- Do not initialize TeleOp during the motionless transition if hood stowing
  would move the servo; wait until TeleOp begins in that case (G403).
- Test E-stop, battery retention, wheel/odometry fasteners, Limelight results,
  intake direction, and launcher direction.
- Dry-run autonomous from the taped start pose. Verify it estimates the correct
  own-alliance opening and withholds new feeds during measured Hive movement.
  This preload-only Auto cannot reposition around the Hive after a tip: if the
  new opening is inaccessible, it waits until the shooting cutoff and parks.
- Time the park route: 27-second shooting cutoff to 29-second safety stop is
  only two powered seconds. This is not a verified parking time at 0.35 path speed.
- Inspect flywheels, guards, hood, feeder, intake, and every moving wire.
- Keep a conservative park-only autonomous available until shot and path tuning
  are repeatable on a regulation field.

## Build commands

From the repository root, with `ANDROID_HOME` set to the installed Android SDK:

```powershell
.\gradlew.bat :TeamCode:testDebugUnitTest :TeamCode:assembleDebug
```

The TeamCode APK is then written under `TeamCode/build/outputs/apk/debug/`.

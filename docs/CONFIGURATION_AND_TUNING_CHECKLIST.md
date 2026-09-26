# BIOBUZZ Configuration and Tuning Checklist

Use this as the single checklist for bringing the code onto the physical robot. A checked item means it was verified on the current mechanical and electrical build—not that the starting value merely compiled.

Record the date, battery voltage, robot revision, and final measured value beside every tuned item. Recheck affected sections after changing motors, gearing, wheels, odometry pods, camera mounting, flywheel compression, hood geometry, feeder geometry, or weight distribution.

## 1. Before applying power

- [ ] Confirm the robot uses no more than the permitted motors and servos. The current design expects eight motors: four drive, intake, two motors driving one shared flywheel, and feeder; it also expects one hood servo.
- [ ] Confirm every mechanism moves freely by hand and no wire can enter the drivetrain, intake, feeder, or flywheel.
- [ ] Confirm the flywheel has a guard and the robot can be stopped immediately during testing.
- [ ] Confirm both flywheel motors are actually coupled to the same physical flywheel and are intended to assist each other.
- [ ] Confirm the battery, main switch, Control Hub, Expansion Hub if used, and all motor/encoder ports match the wiring plan.
- [ ] Confirm the robot starts inside the legal starting dimensions and remains within expansion limits throughout every mechanism motion.

## 2. REV Robot Configuration

The following names must match exactly. Change the REV configuration or the corresponding string in `RobotConfig.java`; do not maintain two different naming schemes.

| Checked | Configuration name | Expected device | Code constant |
| --- | --- | --- | --- |
| [ ] | `front_left_motor` | front-left mecanum motor | `Drive.FRONT_LEFT` |
| [ ] | `front_right_motor` | front-right mecanum motor | `Drive.FRONT_RIGHT` |
| [ ] | `back_left_motor` | back-left mecanum motor | `Drive.BACK_LEFT` |
| [ ] | `back_right_motor` | back-right mecanum motor | `Drive.BACK_RIGHT` |
| [ ] | `pinpoint` | goBILDA Pinpoint computer | `Drive.PINPOINT` |
| [ ] | `intake_left` | left intake motor | `Intake.LEFT_MOTOR` |
| [ ] | `intake_right` | right intake motor | `Intake.RIGHT_MOTOR` |
| [ ] | `flywheel_left` | left motor on shared flywheel | `Shooter.LEFT_FLYWHEEL` |
| [ ] | `flywheel_right` | right motor on shared flywheel | `Shooter.RIGHT_FLYWHEEL` |
| [ ] | `feeder` | shooter feeder motor | `Shooter.FEEDER` |
| [ ] | `hood` | shooter hood servo | `Shooter.HOOD` |
| [ ] | `limelight` | Required launcher-aligned Limelight 3A | `Vision.HIVE_LIMELIGHT` |
| [ ] | `pollen_limelight` | Optional pollen-detection Limelight 3A | `Vision.POLLEN_LIMELIGHT` |

- [ ] Select the correct motor model for every motor in the REV configuration.
- [ ] Confirm each encoder is connected to the port belonging to the motor whose speed it reports.
- [ ] Confirm both Hubs have unique addresses and the saved configuration is active on the Robot Controller.
- [ ] Initialize **BIOBUZZ Pit Diagnostics** with scoring elements removed. A missing or incorrectly named required device should fail during initialization rather than halfway through a match.

## 3. Drivetrain and Pinpoint

Competition drive configuration is split between `RobotConfig.java` and `pedroPathing/Constants.java`.

### Motor and wheel directions

Current starting directions:

- front-left and back-left: `REVERSE`
- front-right and back-right: `FORWARD`
- Pinpoint X and Y pods: `FORWARD`
- Pinpoint pod type: `goBILDA_4_BAR_POD`

- [ ] Put the robot securely on blocks and command low-power forward movement. All wheels must contribute to physical forward motion.
- [ ] Verify positive strafe and positive turn. Fix incorrect motor directions in `pedroPathing/Constants.java`; do not hide a wiring/direction error by changing joystick signs.
- [ ] Push the unpowered robot straight forward and sideways while watching Pinpoint telemetry. Confirm the reported axes and pod directions have the expected signs.
- [ ] Confirm the installed pods are actually goBILDA 4-bar pods. Select the correct pod type or measured custom resolution if not.

### Pinpoint geometry

Run the supplied `PinpointTuner`, then install its measured results.

| Checked | Value | Starting value | Where to save it | Measurement |
| --- | --- | --- | --- | --- |
| [ ] | forward-tracking pod sideways offset | `0 in` | `Drive.PINPOINT_X_OFFSET_IN` | ______ |
| [ ] | sideways-tracking pod forward/back offset | `0 in` | `Drive.PINPOINT_Y_OFFSET_IN` | ______ |
| [ ] | X pod direction | `FORWARD` | `Constants.localizerConfig` | ______ |
| [ ] | Y pod direction | `FORWARD` | `Constants.localizerConfig` | ______ |
| [ ] | pod type/resolution | 4-bar preset | `Constants.localizerConfig` | ______ |

Zero offsets are valid only if the corresponding pod measuring wheel is physically on the robot rotation centerline.

### Pedro Foresight tuning

The values below are placeholders in `pedroPathing/Constants.java`. Run the supplied `ForesightTuner` on the finished robot and replace the entire generated group together.

| Checked | Foresight output | Starting value |
| --- | --- | --- |
| [ ] | forward translational feedback | `0.10` |
| [ ] | strafe translational feedback | `0.10` |
| [ ] | heading feedback | `1.0` |
| [ ] | coast feedforward | `0.01` |
| [ ] | brake feedforward | `0.01` |
| [ ] | heading linear/quadratic braking | `0.05`, `0.005` |
| [ ] | forward/strafe linear braking | `0.10`, `0.10` |
| [ ] | forward/strafe quadratic braking | `0.001`, `0.001` |
| [ ] | maximum achievable forward velocity | `50.0` |
| [ ] | maximum achievable strafe velocity | `40.0` |
| [ ] | natural forward deceleration | `80.0` |
| [ ] | natural strafe deceleration | `80.0` |

Do not increase gains to compensate for a low speed limit. Tune localization and the controller first, then raise `AUTO_PATH_SPEED_LIMIT` gradually.

### Driver feel and drive limits

| Checked | Constant | Starting value | How to tune | Final |
| --- | --- | --- | --- | --- |
| [ ] | `STICK_DEADBAND` | `0.06` | Set just above measured idle-stick noise. | ______ |
| [ ] | `NORMAL_DRIVE_POWER_LIMIT` | `1.0` | Lower only for control, wheel slip, or brownout problems. | ______ |
| [ ] | `PRECISION_SCALE` | `0.35` | Driver-test alignment near the Hive and loading zone. | ______ |
| [ ] | `AUTO_PATH_SPEED_LIMIT` | `0.35` | Increase only after repeatable tuned paths and stops. | ______ |

- [ ] Verify field-centric forward after placing the robot at several headings.
- [ ] Verify the driver can deliberately enter and leave precision mode with gamepad 1 right trigger.
- [ ] Test with a competition-weight robot and realistic battery voltage, not an unloaded chassis only.

## 4. Intake and software inventory

| Checked | Constant/choice | Starting value | How to verify | Final |
| --- | --- | --- | --- | --- |
| [ ] | `COLLECT_POWER` | `1.0` | Collect repeatedly without stalls, damage, or throwing elements. | ______ |
| [ ] | `REVERSE_POWER` | `-0.75` | Reliably clear a jam without ejecting dangerously. | ______ |
| [ ] | physical intake direction | collect on positive power | Verify using Pit Diagnostics. | ______ |
| [ ] | `ENFORCE_INVENTORY_CAPACITY` | `false` | Leave false without trustworthy sensors; enable only after sensor-backed counting exists. | ______ |
| [ ] | `MANUAL_INVENTORY_CONTROLS_ENABLED` | `false` | Leave false unless the team deliberately adopts manual bookkeeping. | ______ |

- [ ] Confirm CAD or another physical design feature prevents the robot from controlling more than four scoring elements while software capacity enforcement is disabled.
- [ ] Confirm the driver understands TeleOp inventory is deliberately shown as untracked and does not prevent a sensorless feed.
- [ ] If sensors are later added, test entry, exit, reverse, jam, bounce, rapid intake/outtake, OpMode restart, and simultaneous detections before enabling capacity enforcement.
- [ ] Verify the intake cannot deliberately retain opponent Nectar. The current sensorless code cannot identify what physically entered.

## 5. Shooter hardware and direction

Do these checks with belts removed when practical, then reconnect at low power. Never run coupled motors until their directions are known.

| Checked | Constant | Starting value | Required verification | Final |
| --- | --- | --- | --- | --- |
| [ ] | `RIGHT_FLYWHEEL_REVERSED` | `false` | Both motors must assist the shared flywheel rather than fight. | ______ |
| [ ] | `FEEDER_REVERSED` | `false` | Positive feed power must move pollen toward the flywheel. | ______ |
| [ ] | `SHOOTER_ENCODER_TICKS_PER_MOTOR_REVOLUTION` | `28` | Confirm both installed motor SKUs and the encoder-to-flywheel gearing. | ______ |
| [ ] | encoder velocity signs | positive while shooting | Both displayed RPM values must be positive and plausible. | ______ |
| [ ] | hood servo direction/range | REV configuration/default | Increasing commanded position must move the hood in the expected direction. | ______ |

The value `28` is motor-shaft encoder ticks per revolution for the intended 1:1 6000-RPM motors. If the motor SKU or interpretation of encoder ticks differs, displayed RPM and readiness will be wrong. Flywheel-to-motor gearing affects physical flywheel RPM even when motor RPM telemetry is correct.

## 6. Shooter feed and readiness

Tune with one pollen first, a guarded flywheel, and repeated battery-voltage conditions.

| Checked | Constant | Starting value | What it controls | Final |
| --- | --- | --- | --- | --- |
| [ ] | `SHOOTER_BASE_TARGET_RPM` | `4000 RPM` | Center of the preliminary two-point shot model. | ______ |
| [ ] | `SHOOTER_MAX_READY_ERROR_RPM` | `160 RPM` | Maximum error for each flywheel motor before feed. | ______ |
| [ ] | `READY_HOLD_SECONDS` | `0.12 s` | Continuous time both motors must remain in tolerance. | ______ |
| [ ] | `FEED_POWER` | `0.85` | Feeder power during one pulse. | ______ |
| [ ] | `FEED_SECONDS` | `0.18 s` | Pulse duration; too short may half-feed, too long may double-feed/jam. | ______ |
| [ ] | `HOOD_STOW` | `0.16` | Safe stopped position without hitting a servo hard stop. | ______ |
| [ ] | `HOOD_NEAR` | `0.43` | Hood position at measured near distance. | ______ |
| [ ] | `HOOD_FAR` | `0.62` | Hood position at measured far distance. | ______ |
| [ ] | `NEAR_DISTANCE_IN` | `30 in` | Actual launcher reference to Cell opening at near calibration. | ______ |
| [ ] | `FAR_DISTANCE_IN` | `84 in` | Actual launcher reference to Cell opening at far calibration. | ______ |

- [ ] Define and consistently use one physical distance reference: preferably launcher exit to Cell-opening center.
- [ ] Measure success rate, left/right motor RPM, recovery time, and battery voltage for every shot.
- [ ] Verify the feeder cannot send a second pollen during one pulse.
- [ ] Verify a failed/slow flywheel motor prevents feed.
- [ ] Verify release of the spin-up control finishes an active feed pulse before stopping.
- [ ] Verify hood stowing during initialization is safe. If it would move during the Auto-to-TeleOp transition, wait until TeleOp begins before INIT.

### Hidden shot-model values

`ShotModel.java` currently interpolates linearly between two hood positions and multiplies base RPM by `0.88` at the near point and `1.12` at the far point. These multipliers are preliminary even though they are not yet in `RobotConfig`.

- [ ] Measure the actual successful RPM at the near distance and replace the implied `0.88 × base` target.
- [ ] Measure the actual successful RPM at the far distance and replace the implied `1.12 × base` target.
- [ ] Test middle distances. If linear interpolation is inaccurate, replace the two-point model with a measured lookup table rather than adding arbitrary corrections.
- [ ] Retest with low and freshly charged batteries; closed-loop velocity should reduce but may not eliminate variation.

## 7. Limelight configuration

### Physical installation

- [ ] Mount the Limelight rigidly and upright—not sideways or inverted—with an unobstructed view along the launch direction.
- [ ] Record the camera position and angle relative to the launcher. Tighten and witness-mark the mount.
- [ ] Calibrate camera intrinsics at the resolution used by the AprilTag pipeline if the default calibration is inadequate.
- [ ] Confirm focus, exposure, gain, resolution, and motion blur under event-like lighting.

### Pipeline 0: pollen detector

| Checked | Setting | Required/current expectation | Final |
| --- | --- | --- | --- |
| [ ] | pipeline index | `0` (`POLLEN_PIPELINE`) | ______ |
| [ ] | detector class label | exactly `yellow_pollen` (`POLLEN_CLASS`) | ______ |
| [ ] | model/input resolution | must match the deployed detector | ______ |
| [ ] | confidence threshold | `MIN_CONFIDENCE = 0.40` starting point | ______ |
| [ ] | exposure/gain | stable detection without excessive blur | ______ |

- [ ] Capture representative true-positive and false-positive images: different pollen distances, partial occlusion, field lighting, yellow robot parts, and spectators/backgrounds.
- [ ] Choose `MIN_CONFIDENCE` from those observations. Do not tune it using only clean pit images.
- [ ] Confirm the reported target bearing sign: an object to camera-right must report the expected positive direction.

### Pipeline 1: Hive AprilTags

| Checked | Setting | Required/current expectation | Final |
| --- | --- | --- | --- |
| [ ] | pipeline index | `1` (`HIVE_APRILTAG_PIPELINE`) | ______ |
| [ ] | family | AprilTag Classic `36h11` | ______ |
| [ ] | physical tag size | `82.55 mm` / `3.25 in` | ______ |
| [ ] | 3D solving | **Full 3D enabled** | ______ |
| [ ] | resolution | prioritize stable 3D pose while meeting frame-rate needs | ______ |
| [ ] | ID handling | own-alliance IDs: red 30–37, blue 38–45 | ______ |

- [ ] Confirm `LLResult.getPipelineIndex()` reports the requested pipeline after each switch.
- [ ] Confirm every detected tag reports a finite camera-space 3D pose and a changing timestamp.
- [ ] Verify all four tags belonging to one Cell estimate nearly the same opening location.
- [ ] Hide individual tags and confirm the opening bearing remains stable.
- [ ] View both Hive stable positions. The downward/non-scorable Cell must not qualify even when its tags are visible.
- [ ] Tip the Hive while keeping its image horizontally centered. Autonomous must reset the stable-target interval.
- [ ] Disconnect or freeze the camera. No new automatic feed may begin.
- [ ] Verify a target to robot-right makes the unloaded robot turn right. Correct wheel directions first; do not use a negative gain to hide a drivetrain sign error.

## 8. Vision-assisted autonomous tuning

| Checked | Constant | Starting value | How to tune | Final |
| --- | --- | --- | --- | --- |
| [ ] | `HIVE_AIM_BEARING_DEGREES` | `0°` | Offset that aligns camera-derived opening with launcher at the fixed shot pose. | ______ |
| [ ] | `HIVE_AIM_TOLERANCE_DEGREES` | `2°` | Largest measured aim error that still reliably enters the opening. | ______ |
| [ ] | `HIVE_AIM_TURN_POWER_PER_DEGREE` | `0.018` | Increase until correction is responsive without oscillation. | ______ |
| [ ] | `HIVE_AIM_MAX_TURN_POWER` | `1.0` | Full power is allowed by default; lower only if testing shows overshoot or lost tags. | ______ |
| [ ] | `MAX_FRAME_AGE_MS` | `150 ms` | Must exceed normal measured processing/transport latency but reject freezes. | ______ |
| [ ] | `HIVE_MAX_POSITION_DIFFERENCE_IN` | `0.75 in` | Maximum disagreement between tags estimating the same opening in one image. | ______ |
| [ ] | `HIVE_MAX_ROTATION_DIFFERENCE_DEGREES` | `3°` | Maximum rotation disagreement between tags in one image. | ______ |
| [ ] | `HIVE_UPRIGHT_MARGIN_DEGREES` | `15°` | Reject ambiguous nearly sideways Cell orientations. | ______ |
| [ ] | `HIVE_POST_FEED_WAIT_SECONDS` | `0 s` | Adds no delay by default; increase only after a repeatable problem is observed. | ______ |

These checks estimate target orientation and motion. They do not physically prove that the Hive damper reached its stop or that the shot will land.

## 9. Autonomous paths and timing

Every coordinate in `BiobuzzAutoPlan.java` came from the supplied Pedro path and must be verified on the actual field. Blue is generated by mirroring red across a 144-inch field model; verify it independently rather than assuming the transform is correct.

| Checked | Red waypoint | Starting pose `(x, y, heading°)` | Final/notes |
| --- | --- | --- | --- |
| [ ] | start | `(56, 8, 90)` | ______ |
| [ ] | south Hive shot | `(59.25, 42, 90)` | ______ |
| [ ] | garden collect | `(10, 10, 270)` | ______ |
| [ ] | north approach | `(31.9496, 92.4893, 270)` | ______ |
| [ ] | north curve exit | `(46.6173, 102.9301, 270)` | ______ |
| [ ] | curve control 1 | `(33.9541, 100.0223, 0)` | ______ |
| [ ] | curve control 2 | `(38.8433, 103.5025, 0)` | ______ |
| [ ] | north Hive shot | `(59.25, 102, 270)` | ______ |
| [ ] | top Flower collect | `(48, 132, 90)` | ______ |
| [ ] | loading-zone park | `(8, 108, 180)` | ______ |

Only the start-to-south-Hive and south-Hive-to-park paths are currently executed by competition Auto. The other generated paths exist but are not yet part of the Auto state machine.

- [ ] Tape the exact starting pose and verify the robot contacts the required wall/location legally.
- [ ] Test red paths slowly with no pollen.
- [ ] Test blue paths independently with no pollen.
- [ ] Confirm every route remains on the alliance-safe side during Auto and clears field elements and robots.
- [ ] Validate the shot pose gives the camera and launcher a usable opening view.
- [ ] Validate the park endpoint actually meets the game’s parking criteria.
- [ ] Time the entire path with a normal and low battery.

| Checked | Constant | Starting value | Required decision | Final |
| --- | --- | --- | --- | --- |
| [ ] | `SHOOT_CUTOFF_SECONDS` | `27 s` | Leaves only 2 powered seconds before the current safety cutoff; change based on measured park time. | ______ |
| [ ] | `MATCH_SAFETY_CUTOFF_SECONDS` | `29 s` | Confirm all powered motion stops before Auto ends. | ______ |
| [ ] | `PATH_TIMEOUT_SECONDS` | disabled | Decide whether a measured path-failure timeout is needed; keep its `TUNE` comment. | ______ |
| [ ] | `ALIGN_TIMEOUT_SECONDS` | disabled | Decide whether failure to acquire a target should park early; keep its `TUNE` comment. | ______ |

- [ ] Test path failure, lost localization, blocked route, disconnected Limelight, no visible target, shooter never ready, and an interrupted feed.
- [ ] Keep a verified park-only autonomous available until preload scoring and park timing are repeatable.

## 10. Driver controls and OpMode behavior

- [ ] Confirm red and blue Auto/TeleOp selections match the Driver Station alliance.
- [ ] Driver approves field-centric translation, turn direction, deadband, normal speed, and precision speed.
- [ ] Operator approves intake `A/X`, reverse `Y`, shooter spin-up right bumper, and one feed request per right-trigger press.
- [ ] Operator understands a trigger press made before shooter readiness is rejected; release and press again after READY.
- [ ] Operator understands Hive telemetry is advisory in TeleOp. It does not aim automatically or prevent a manually aimed illegal/bad shot.
- [ ] Confirm releasing right bumper after an active pulse completes that pulse and then stops the shooter.
- [ ] Confirm `stop()` leaves drivetrain, intake, flywheel, and feeder unpowered.
- [ ] Confirm initialization does not create prohibited powered movement during the Auto-to-TeleOp transition.

## 11. Diagnostics and regression checks

- [ ] Run **BIOBUZZ Pit Diagnostics** with the robot lifted and launcher clear. Hardware should power only while gamepad 2 `START` is held.
- [ ] Confirm releasing the diagnostic arm control stops drive, intake, and shooter.
- [ ] Watch both flywheel RPM values during spin-up and feed recovery.
- [ ] Confirm Limelight connection, active mode, pollen result, and Hive-opening estimate telemetry are understandable to the team.
- [ ] Run the Java tests: `./gradlew :TeamCode:testDebugUnitTest`.
- [ ] Build the APK: `./gradlew :TeamCode:assembleDebug`.
- [ ] Re-run tests after every constant, coordinate, or control change.
- [ ] Conduct repeated full match simulations with competition battery, bumpers, guards, realistic load, Auto-to-TeleOp transition, and end-of-match stopping.

## 12. Template tools and build choices

These files are part of the retained template. They are not all competition constants.

- [ ] In `TeamCode/build.gradle`, keep exactly the intended telemetry UI source set active: Panels is currently included and FTC Dashboard is excluded. Change this only if the team deliberately switches dashboards.
- [ ] Use `PinpointTuner` for Pinpoint geometry/directions and `ForesightTuner` for Pedro controller values.
- [ ] Use the generic `PIDSVAGFTuner` only when deliberately tuning a mechanism that uses `PIDSVAGFController`. Its motor names, inversion array, limits, gravity mode, and test powers must be configured for that mechanism before running it. The current competition drivetrain/intake/shooter code does not consume those PIDSVAGF coefficients.
- [ ] Do not copy values from `OctoQuadTuner`, `OTOSTuner`, two-wheel, or three-wheel localizer tools into the Pinpoint configuration. They target different hardware layouts.
- [ ] Do not tune constants inside template tuner procedures merely to make competition driving feel different. Install the tuner’s generated results into the competition `Constants.java`/`RobotConfig.java` locations identified above.

## 13. Final recorded configuration

Fill this in after validation so another team member can reproduce the robot:

- Robot mechanical revision: ______
- Date tested: ______
- Tested by: ______
- Control Hub/Expansion Hub configuration filename: ______
- Robot Controller SDK version: `12.0.0` / other: ______
- Limelight OS version: ______
- Pollen pipeline export/version: ______
- AprilTag pipeline export/version: ______
- Camera resolution and calibration: ______
- Flywheel motor SKU and gearing: ______
- Pinpoint pod type and firmware: ______
- Competition battery voltage range tested: ______
- Red Auto best/worst measured duration: ______
- Blue Auto best/worst measured duration: ______
- Near-shot successes/attempts: ______
- Far-shot successes/attempts: ______
- Remaining known limitations: ______

## Source files that contain configuration

- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/constants/RobotConfig.java`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedroPathing/Constants.java`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/control/ShotModel.java`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/auto/BiobuzzAutoPlan.java`
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/teleop/BiobuzzTeleOpBase.java`
- `TeamCode/build.gradle`

Supporting instructions and safety context remain in `docs/ROBOT_SETUP.md` and `docs/BIOBUZZ_SOFTWARE_PLAN.md`.

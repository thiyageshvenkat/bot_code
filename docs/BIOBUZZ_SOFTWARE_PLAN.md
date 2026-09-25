# BIOBUZZ Software Plan

This plan is based on the official 2026-2027 BIOBUZZ Competition Manual,
version TU02 (September 24, 2026). Re-check the manual and Team Updates before
every event.

## Game constraints represented in software

- A robot may control at most four scoring elements (G407). The software model
  rejects a fifth entry, but it cannot enforce the physical limit until CAD or
  sensors prevent the intake from collecting a fifth element.
- The inventory model rejects opposing-alliance nectar (G408). Sensorless TeleOp
  cannot identify what the intake physically collects; the driver/CAD must enforce this.
- Hive tips are attempted only by launching scoring elements into the
  upward-facing cell (G417). Autonomous checks fresh tag poses for an upright
  own-alliance Cell, transforms tag centers to the opening, and waits for low
  pose motion before feeding. This estimates a scoring target, not a guarantee
  that the Hive is settled or that a shot will land. TeleOp remains driver-aimed.
- Autonomous lasts 30 seconds and TeleOp lasts 120 seconds.
- The autonomous plan includes leaving the wall, scoring the four preload
  pollen, and parking in the loading zone.

## Robot architecture

1. Pedro Pathing 3 mecanum drivetrain with Pinpoint localization.
2. Single-roller intake and four-element inventory model. Auto tracks four
   assumed preloads; sensorless TeleOp does not pretend its count is maintained.
3. Two motors driving one shared flywheel, plus a feeder and adjustable hood.
4. Limelight 3A neural pollen detection and Hive AprilTag targeting.
5. A small coordinator for launcher readiness and timed feed completion.
6. Competition TeleOp, preload autonomous, and diagnostics OpModes.

## Hardware assumptions

Hardware names and preliminary calibration values live in `RobotConfig`.
The documented intake, launcher, Limelight, drivetrain, and Pinpoint are
required. Directions, odometry offsets, PID values, hood endpoints, shot tables,
and autonomous paths must be measured on the physical robot before competition.
The current design already uses the eight motors allowed by R503. A future
Flower mechanism cannot add a ninth motor without reallocating an actuator.

The current autonomous is still preload-and-park, not the full collection
route. It does not drive around the Hive to follow a newly upward Cell. If no
qualifying opening can be seen from its shot position, it waits until the
shooting cutoff and parks. The configured 27-second shooting cutoff and
29-second safety stop leave only two powered seconds for parking; time that
route at the configured speed before using it in a match.

## Primary sources

- https://ftc-resources.firstinspires.org/ftc/game
- https://ftc-resources.firstinspires.org/ftc/game/manual-09
- https://ftc-resources.firstinspires.org/ftc/game/manual-10
- https://ftc-resources.firstinspires.org/ftc/game/manual-11
- https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- https://pedropathing.com/docs/pathing/tuning/constants
- https://pedropathing.com/docs/pathing/guide/teleop-usage
- https://ftc-docs.firstinspires.org/en/latest/tech_tips/tech-tips/tech-tip-apriltag-clusters/tech-tip-apriltag-clusters.html
- https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-coordinate-systems

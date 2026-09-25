# BIOBUZZ Software Plan

This plan is based on the official 2026-2027 BIOBUZZ Competition Manual,
version TU02 (September 24, 2026). Re-check the manual and Team Updates before
every event.

## Game constraints represented in software

- A robot may control at most four scoring elements (G407). The software model
  rejects a fifth entry, but it cannot enforce the physical limit until CAD or
  sensors prevent the intake from collecting a fifth element.
- Opposing-alliance nectar is never intentionally collected (G408).
- Hive tips are attempted only by launching scoring elements into the
  upward-facing cell (G417). The Limelight identifies that cell from its
  official AprilTag cluster before autonomous can feed.
- Autonomous lasts 30 seconds and TeleOp lasts 120 seconds.
- The autonomous plan includes leaving the wall, scoring the four preload
  pollen, and parking in the loading zone.

## Robot architecture

1. Pedro Pathing 3 mecanum drivetrain with Pinpoint localization.
2. Single-roller intake and operator-maintained four-element inventory.
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

## Primary sources

- https://ftc-resources.firstinspires.org/ftc/game
- https://ftc-resources.firstinspires.org/ftc/game/manual-09
- https://ftc-resources.firstinspires.org/ftc/game/manual-10
- https://ftc-resources.firstinspires.org/ftc/game/manual-11
- https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- https://pedropathing.com/docs/pathing/tuning/constants
- https://pedropathing.com/docs/pathing/guide/teleop-usage

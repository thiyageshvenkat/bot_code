# BIOBUZZ Software Plan

This plan is based on the official 2026-2027 BIOBUZZ Competition Manual,
version TU01 (September 17, 2026). Re-check the manual and Team Updates before
every event.

## Game constraints represented in software

- A robot may control at most four scoring elements (G407). The inventory model
  rejects a fifth element; the operator updates it because no counter sensor is
  documented.
- Opposing-alliance nectar is never intentionally collected (G408).
- Hive tips are attempted only by launching scoring elements into the
  upward-facing cell (G417).
- Autonomous lasts 30 seconds and TeleOp lasts 120 seconds.
- The autonomous plan includes leaving the wall, scoring the four preload
  pollen, and parking in the loading zone.

## Robot architecture

1. Pedro Pathing 3 mecanum drivetrain with Pinpoint localization.
2. Single-roller intake and operator-maintained four-element inventory.
3. Dual-wheel launcher, feeder, and adjustable hood for hive cells.
4. Limelight 3A neural detection for pollen collection.
5. A small coordinator for launcher readiness and confirmed feed completion.
6. Competition TeleOp, preload autonomous, and diagnostics OpModes.

## Hardware assumptions

Hardware names and preliminary calibration values live in focused files in the
`constants` package (`DriveConfig`, `ShooterConfig`, and so on).
The documented intake, launcher, Limelight, drivetrain, and Pinpoint are
required. Directions, odometry offsets, PID values, hood endpoints, shot tables,
and autonomous paths must be measured on the physical robot before competition.

## Primary sources

- https://ftc-resources.firstinspires.org/ftc/game
- https://ftc-resources.firstinspires.org/ftc/game/manual-09
- https://ftc-resources.firstinspires.org/ftc/game/manual-10
- https://ftc-resources.firstinspires.org/ftc/game/manual-11
- https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- https://pedropathing.com/docs/pathing/tuning/constants
- https://pedropathing.com/docs/pathing/guide/teleop-usage

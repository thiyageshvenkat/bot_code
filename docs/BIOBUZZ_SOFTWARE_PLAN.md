# BIOBUZZ Software Plan

This plan is based on the official 2026-2027 BIOBUZZ Competition Manual,
version TU01 (September 17, 2026). Re-check the manual and Team Updates before
every event.

## Game constraints represented in software

- A robot may control at most four scoring elements (G407). The magazine model
  rejects a fifth element and the intake stops when capacity is reached.
- Opposing-alliance nectar is never intentionally collected (G408).
- Nectar placement into a flower is locked until the final 60 seconds (G410).
- Hive tips are attempted only by launching scoring elements into the
  upward-facing cell (G417).
- Flower scoring enters pollen or nectar through the top; only pollen may be
  retrieved through the bottom (G418).
- Autonomous lasts 30 seconds. TeleOp lasts 120 seconds, with flower ownership
  unlocking at 60 seconds remaining.
- The autonomous plan includes leaving the wall, scoring the four preload
  pollen, and parking in the loading zone.

## Robot architecture

1. Pedro Pathing 3 mecanum drivetrain with Pinpoint localization.
2. Four-element inventory and intake with beam-break edge counting.
3. Indexed magazine that cannot intentionally exceed four elements.
4. Dual-wheel launcher, feeder, and adjustable hood for hive cells.
5. Lift-and-gate flower mechanism for controlled top entry.
6. AprilTag targeting for the moving hive cells. Cell tags are used for relative
   aiming, not assumed to be fixed field-localization landmarks.
7. A superstructure coordinator that owns rule and mechanism interlocks.
8. Competition TeleOp, preload autonomous, and diagnostics OpModes.

## Hardware assumptions

Hardware names and preliminary calibration values live in `RobotConfig`.
Core mechanism devices, drivetrain, Pinpoint, and webcam are required; the three
beam/limit sensors are optional. Directions, odometry offsets, PID values, servo
endpoints, shot tables, and autonomous poses must be measured on the physical
robot before competition.

## Primary sources

- https://ftc-resources.firstinspires.org/ftc/game
- https://ftc-resources.firstinspires.org/ftc/game/manual-09
- https://ftc-resources.firstinspires.org/ftc/game/manual-10
- https://ftc-resources.firstinspires.org/ftc/game/manual-11
- https://ftc-resources.firstinspires.org/ftc/game/tu-combined
- https://pedropathing.com/docs/pathing/tuning/constants
- https://pedropathing.com/docs/pathing/guide/teleop-usage

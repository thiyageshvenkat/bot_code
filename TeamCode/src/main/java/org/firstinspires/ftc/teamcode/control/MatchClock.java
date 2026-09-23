package org.firstinspires.ftc.teamcode.control;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.game.BiobuzzRules;

/** TeleOp clock used for the rule-sensitive final-minute flower unlock. */
public final class MatchClock {
    private final ElapsedTime timer = new ElapsedTime();
    private boolean running;

    public void startTeleop() {
        timer.reset();
        running = true;
    }

    public double elapsedSeconds() {
        return running ? timer.seconds() : 0.0;
    }

    public double remainingSeconds() {
        return Math.max(0.0, BiobuzzRules.TELEOP_SECONDS - elapsedSeconds());
    }

    public boolean flowerUnlocked() {
        return BiobuzzRules.flowerScoringUnlocked(elapsedSeconds());
    }
}

package frc.robot.subsystems.turret;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.misc.LimeLight;
import frc.robot.subsystems.turret.subsystems.TurretFlywheel;
import frc.robot.subsystems.turret.subsystems.TurretPitch;

public class Turret extends SubsystemBase {
    public boolean inHangMode = false;

    public LimeLight limelight;

    public TurretPitch pitch;
    public TurretFlywheel flywheel;

    public LaunchTrajectory launchTrajectory = new LaunchTrajectory(0, 0);

    public Turret(){
        this.limelight = new LimeLight();
        this.pitch = new TurretPitch();
        this.flywheel = new TurretFlywheel();
    }

    public boolean inShootingRange(){
        return (
            this.limelight.hasTarget() &&
            LaunchTrajectory.estimateDistance(this.limelight.pitchOffset()) < Constants.Turret.Physics.kMaxShootDistance
        );
    }

    public boolean isReadyToShoot(){
        // TODO: make this take pitch and flywheel setpoints as args
        return (
            this.limelight.hasTarget() &&
            Math.abs(this.limelight.yawOffset()) < Constants.Turret.Yaw.kErrorThreshold &&

            this.pitch.isAligned(0) &&
            this.flywheel.isUpToSpeed(0)
        );
    };
}
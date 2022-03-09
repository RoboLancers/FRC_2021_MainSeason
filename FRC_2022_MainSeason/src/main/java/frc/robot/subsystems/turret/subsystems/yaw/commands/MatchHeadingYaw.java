package frc.robot.subsystems.turret.subsystems.yaw.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.turret.Turret;

public class MatchHeadingYaw extends CommandBase {
    private Turret turret;
    private Drivetrain driveTrain;

    private boolean seekDirection = true;

    public MatchHeadingYaw(Turret turret, Drivetrain driveTrain){
        this.turret.yaw = turret.yaw;
        this.driveTrain = driveTrain;

        this.addRequirements(this.turret.yaw);
    }

    @Override
    public void execute(){
        if(this.turret.inHangMode){
            return;
        }
        if(this.turret.yaw.limelight.hasTarget()){
            double targetYaw = this.turret.yaw.getPosition() + this.turret.yaw.limelight.yawOffset();
            if(targetYaw < Constants.Turret.TunedCoefficients.YawPID.kMinSafeAngle){
                this.turret.yaw.setPositionSetpoint(targetYaw - Constants.Turret.TunedCoefficients.YawPID.kMinSafeAngle + Constants.Turret.TunedCoefficients.YawPID.kMaxSafeAngle);
            } else if(targetYaw > Constants.Turret.TunedCoefficients.YawPID.kMaxSafeAngle) {
                this.turret.yaw.setPositionSetpoint(targetYaw + Constants.Turret.TunedCoefficients.YawPID.kMinSafeAngle - Constants.Turret.TunedCoefficients.YawPID.kMaxSafeAngle);
            } else {
                this.turret.yaw.setPositionSetpoint(targetYaw);
            }
        } else {
            if(this.turret.yaw.hasRelativeHub){
                double deltaX = this.turret.yaw.relativeHub.getX() - this.driveTrain.getPose().getX();
                double deltaY = this.turret.yaw.relativeHub.getY() - this.driveTrain.getPose().getY();
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                if(distance > Constants.Turret.PhysicsInfo.minLimelightViewableDistance && distance < Constants.Turret.PhysicsInfo.maxLimelightViewableDistance){
                    double turretYaw = this.turret.yaw.getPosition();
                    if(turretYaw < Constants.Turret.TunedCoefficients.YawPID.kMinSafeAngle || turretYaw > Constants.Turret.TunedCoefficients.YawPID.kMaxSafeAngle){
                        this.seekDirection = turretYaw < Constants.Turret.TunedCoefficients.YawPID.kMinSafeAngle;
                    }
                    this.turret.yaw.setPositionSetpoint(
                        turretYaw + (
                            this.seekDirection ?
                                Constants.Turret.TunedCoefficients.YawPID.kSeekAdjustment :
                                -Constants.Turret.TunedCoefficients.YawPID.kSeekAdjustment
                        )
                    );
                    return;
                }
            }
            this.turret.yaw.setPositionSetpoint(Math.atan2(
                this.turret.yaw.relativeHub.getY() - this.driveTrain.getPose().getY(),
                this.turret.yaw.relativeHub.getX() - this.driveTrain.getPose().getX()
            ));
        }
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}

package frc.robot.subsystems.drivetrain.commands;

import java.util.function.DoubleSupplier;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import frc.robot.Constants;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.turret.Turret;
import frc.robot.util.XboxController;

public class TurnToAngle extends PIDCommand {
        private Drivetrain drivetrain; 

    
        public TurnToAngle(Drivetrain drivetrain, DoubleSupplier setpoint){
            super(
                new PIDController(
                    Constants.Turret.Yaw.kP,
                    Constants.Turret.Yaw.kI,
                    Constants.Turret.Yaw.kD
                ),
                drivetrain::getHeading,
                () -> {
                    double setpointValue = setpoint.getAsDouble();
                    SmartDashboard.putNumber("Angular Setpoint", setpointValue);
                    SmartDashboard.putNumber("Angular Error", setpointValue - drivetrain.getHeading());
                    return setpointValue;
                },
                (outputPower) -> {
                    SmartDashboard.putNumber("Angular Output", outputPower);
                    drivetrain.arcadeDrive(0, outputPower, false);
                },
                drivetrain
            );
            
            this.getController().setTolerance(Constants.Turret.Yaw.kErrorThreshold);
            this.getController().enableContinuousInput(-180.0, 180.0);

            SmartDashboard.putBoolean("Angular Running", true);
    
            this.drivetrain = drivetrain;

            addRequirements(drivetrain);
        }
    

        @Override
        public void end(boolean interrupted){
            SmartDashboard.putBoolean("Angular Running", false);
            drivetrain.leftMotors.set(0);
            drivetrain.rightMotors.set(0);
        }
    
        @Override
        public boolean isFinished(){
            return this.getController().atSetpoint();
        }
}
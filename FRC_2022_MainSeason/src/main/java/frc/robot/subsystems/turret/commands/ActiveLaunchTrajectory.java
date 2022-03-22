package frc.robot.subsystems.turret.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.turret.LaunchTrajectory;
import frc.robot.subsystems.turret.Turret;

public class ActiveLaunchTrajectory extends CommandBase {
    public ActiveLaunchTrajectory(Turret turret){
        turret.launchTrajectory = new LaunchTrajectory(0, 0);

        this.addRequirements(turret);
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}
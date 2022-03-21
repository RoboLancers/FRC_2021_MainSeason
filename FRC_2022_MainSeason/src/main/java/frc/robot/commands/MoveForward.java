package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.turret.Turret;
import frc.robot.subsystems.turret.commands.UpperHubShoot;
import frc.robot.subsystems.turret.commands.ZeroPitch;

public class MoveForward extends SequentialCommandGroup {
    public MoveForward(Drivetrain drivetrain, Turret turret, Indexer indexer){
        addCommands(
            new ZeroPitch(turret),
            new ParallelRaceGroup(
                new RunCommand(() -> {
                    drivetrain.arcadeDrive(0.4, 0);;
                }),
                new WaitCommand(1.5)
            ),

            new ParallelRaceGroup(
                new RunCommand(() -> {
                    drivetrain.arcadeDrive(0, 0);;
                }),
                new WaitCommand(1)
            )
        );
        addRequirements(drivetrain, turret, indexer);
    }
}

package frc.robot;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import frc.robot.subsystems.climber.Climber;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;  
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController.Button;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.drivetrain.Pneumatics;
// import frc.robot.commands.TaxiAuto;
// import frc.robot.commands.GeneralizedReleaseRoutine;
import frc.robot.commands.UpdateLights;
import frc.robot.subsystems.climber.commands.ManualClimber;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.drivetrain.GearShifter;
import frc.robot.subsystems.drivetrain.commands.ToggleGearShifter;
import frc.robot.subsystems.drivetrain.commands.TurnToAngle;
import frc.robot.subsystems.drivetrain.commands.UseCompressor;
//import frc.robot.subsystems.misc.AddressableLEDs;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.turret.Turret;
//import frc.robot.subsystems.turret.commands.ActiveLaunchTrajectory;
import frc.robot.subsystems.drivetrain.enums.GearShifterState;
import frc.robot.subsystems.misc.AddressableLEDs;
import frc.robot.subsystems.misc.Camera;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.turret.LaunchTrajectory;
import frc.robot.subsystems.turret.commands.ActiveLaunchTrajectory;
import frc.robot.subsystems.turret.commands.LowHubShoot;
import frc.robot.subsystems.turret.commands.UpperHubShoot;
import frc.robot.subsystems.turret.commands.ZeroPitch;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.util.XboxController;
import frc.robot.util.XboxController.Axis;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import frc.robot.commands.Testing;

public class RobotContainer {
  private RobotContainer m_robotContainer;

  /*   Controllers   */
  private final XboxController driverController = new XboxController(0);
  private final XboxController manipulatorController = new XboxController(1);

  /*   Subsystems   */
  private final Drivetrain drivetrain = new Drivetrain(driverController);
  private final Pneumatics pneumatics = new Pneumatics();
  private final GearShifter gearShifter = new GearShifter(pneumatics);
  private final Indexer indexer = new Indexer();
  private final Camera camera = new Camera();
  public final Turret turret = new Turret();
  private final Climber climber = new Climber();
  private final Intake intake = new Intake();
  // private AddressableLEDs m_AddressableLEDs = new AddressableLEDs();

  /*   Autonomous Trajectory   */
  private Trajectory trajectory = new Trajectory();
  private String trajectoryJSON = "pathplanner/generatedJSON/Test Path.wpilib.json";
  private AHRS gyro = new AHRS(SPI.Port.kMXP);
  private PIDController rightPID = new PIDController(Constants.Trajectory.kP, 0, 0);
  private PIDController leftPID = new PIDController(Constants.Trajectory.kP, 0, 0);
  private Field2d m_field = new Field2d();

  public RobotContainer() {
    this.configureButtonBindings();

    // Shot trajectory tuning

    SmartDashboard.putNumber("Angular kP", SmartDashboard.getNumber("Angular kP", Constants.Turret.Yaw.kP));
    SmartDashboard.putNumber("Angular kI", SmartDashboard.getNumber("Angular kI", Constants.Turret.Yaw.kI));
    SmartDashboard.putNumber("Angular kD", SmartDashboard.getNumber("Angular kD", Constants.Turret.Yaw.kD));

    SmartDashboard.putNumber("High Shot Speed", SmartDashboard.getNumber("High Shot Speed", 3700));
    SmartDashboard.putNumber("High Shot Angle", SmartDashboard.getNumber("High Shot Angle", 3.5));
  }

  private void configureButtonBindings(){
    
    driverController.whenPressed(XboxController.Button.Y, new Testing(intake, indexer, turret, climber));
  }

  public Command getAutonomousCommand() {
    // The voltage constraint makes sure the robot doesn't exceed a certain voltage during runtime.
    var autoVoltageConstraint = new DifferentialDriveVoltageConstraint(
        new SimpleMotorFeedforward(
          Constants.Trajectory.ksVolts, 
          Constants.Trajectory.ksVoltSecondsPerMeter,
          Constants.Trajectory.kaVoltSecondsSquaredPerMeter),
        Constants.Trajectory.kDriveKinematics,10);

    // Gives the trajectory the constants determined in characterization.
    TrajectoryConfig config = new TrajectoryConfig(
      Constants.Trajectory.kMaxSpeedMetersPerSecond,
      Constants.Trajectory.kMaxAccelerationMetersPerSecondSquared
    ).setKinematics(Constants.Trajectory.kDriveKinematics).addConstraint(autoVoltageConstraint);    

    // Generates the actual path with given points.
    /*
      trajectory = TrajectoryGenerator.generateTrajectory(
        new Pose2d(0, 0, new Rotation2d(0)),
          // Pass through these two interior waypoints, making an 's' curve path
          List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
          // End 3 meters straight ahead of where we started, facing forward
          new Pose2d(3, 0, new Rotation2d(0)),
          // Pass config
          config);
    */
            
    try {
      Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON); 
      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
    } catch (IOException ex) {
      DriverStation.reportError("Unable to open trajectory: " + trajectoryJSON, ex.getStackTrace());
    }

    // Ramsete is a trajectory tracker and auto corrector. We feed it parameters into a ramsete command
    // so that it constantly updates and corrects the trajectory auto.
    RamseteCommand ramseteCommand = new RamseteCommand(
      trajectory, 
      drivetrain::getPose, // Gets the translational and rotational position of the robot.
      new RamseteController(Constants.Trajectory.kRamseteB, Constants.Trajectory.kRamseteZeta),//Uses constants of 2.0 and 0.7
      new SimpleMotorFeedforward( // Feedforward controller to control the motors before they move
        Constants.Trajectory.ksVolts,  
        Constants.Trajectory.ksVoltSecondsPerMeter,
        Constants.Trajectory.kaVoltSecondsSquaredPerMeter),
        Constants.Trajectory.kDriveKinematics, 
        drivetrain::getWheelSpeeds,
        leftPID,
        rightPID,
        drivetrain::tankDriveVolts,
        drivetrain);
    drivetrain.resetOdometry(trajectory.getInitialPose());
    return ramseteCommand.andThen(() -> drivetrain.tankDriveVolts(0,0));
  }

  public void doSendables(){
    SmartDashboard.putNumber("Gyro Angle", drivetrain.getHeading());

    SmartDashboard.putNumber("Actual Speed", turret.flywheel.getVelocity());
    SmartDashboard.putNumber("Actual Pitch", turret.pitch.getPosition());
  }
}
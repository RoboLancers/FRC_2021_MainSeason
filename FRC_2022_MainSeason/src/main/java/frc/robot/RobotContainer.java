package frc.robot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController.Button;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.drivetrain.Pneumatics;
// import frc.robot.commands.GeneralizedReleaseRoutine;
import frc.robot.subsystems.climber.commands.LowRung;
import frc.robot.subsystems.climber.commands.MidRung;
import frc.robot.commands.UpdateLights;
import frc.robot.subsystems.climber.Climber;
import frc.robot.commands.UpdateLights;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.drivetrain.GearShifter;
import frc.robot.subsystems.drivetrain.commands.ToggleGearShifter;
import frc.robot.subsystems.drivetrain.commands.UseCompressor;
import frc.robot.subsystems.misc.AddressableLEDs;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.turret.Turret;
import frc.robot.subsystems.turret.commands.ActiveLaunchTrajectory;
import frc.robot.subsystems.turret.subsystems.yaw.commands.MatchHeadingYaw;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj.SPI;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.robot.util.XboxController;
import frc.robot.util.XboxController.Axis;

public class RobotContainer {
  private final Drivetrain drivetrain = new Drivetrain();
  private final Indexer indexer = new Indexer();
  // private final Turret turret = new Turret();
  // private final Climber climber = new Climber();

  private Trajectory trajectory = new Trajectory();
  private String trajectoryJSON = "pathplanner/generatedJSON/Test Path.wpilib.json";
  
  private AHRS gyro = new AHRS(SPI.Port.kMXP);
 
  
  
  
  // private String trajectoryJSON = "paths/MyPath.wpilib.json";
  private RobotContainer m_robotContainer;
  private XboxController xboxController = new XboxController(0);
  private PIDController rightPID= new PIDController(Constants.Trajectory.kP, 0, 0);
  private PIDController leftPID= new PIDController(Constants.Trajectory.kP, 0, 0);
  private Field2d m_field = new Field2d();
  private Pneumatics pneumatics = new Pneumatics();
  private XboxController driverController = new XboxController(0);
  private XboxController manipulatorController = new XboxController(1);
  private GearShifter gearShifter = new GearShifter(pneumatics);
  //private AddressableLEDs m_AddressableLEDs = new AddressableLEDs();

  public RobotContainer() {
    // this.pneumatics.setDefaultCommand(new UseCompressor(pneumatics));

    this.indexer.setDefaultCommand(new RunCommand(
      () -> {
        SmartDashboard.putNumber("proximity", indexer.bottomColorSensor.getProximity());
        SmartDashboard.putNumber("red", indexer.bottomColorSensor.getRed());
        SmartDashboard.putNumber("blue", indexer.bottomColorSensor.getBlue());
        SmartDashboard.putNumber("green", indexer.bottomColorSensor.getGreen());
        SmartDashboard.putNumber("ball number", indexer.ballQueue.size());
        this.indexer.indexerMotor.set(Constants.Indexer.kIndexerOff);
      }, this.indexer
    ));
    
    this.configureButtonBindings();

    // A split-stick arcade command, with forward/backward controlled by the left hand, and turning controlled by the right.
    this.drivetrain.setDefaultCommand(
      new RunCommand(
        () -> {
          this.drivetrain.arcadeDrive(driverController.getAxisValue(XboxController.Axis.LEFT_Y), driverController.getAxisValue(XboxController.Axis.RIGHT_X));
        },
        drivetrain
      )
    );

    //m_AddressableLEDs.setDefaultCommand(new UpdateLights(turret, climber, indexer));
    
    //turret.setDefaultCommand(new ActiveLaunchTrajectory(turret));
    //turret.yaw.setDefaultCommand(new MatchHeadingYaw(turret.yaw));
    // m_AddressableLEDs.setDefaultCommand(new UpdateLights(turret, climber, indexer));
    
    // turret.setDefaultCommand(new ActiveLaunchTrajectory(turret));
    // turret.yaw.setDefaultCommand(new MatchHeadingYaw(turret.yaw));
  }

  private void configureButtonBindings() {
    driverController.whenPressed(XboxController.Button.A, new ToggleGearShifter(gearShifter));
    // driverController.whenPressed(XboxController.RIGHT_BUMPER, new Intake);
    // driverController.whenPressed(XboxController.Button.B, new Intake Deploy);
    // driverController.whenPressed(XboxController.UP, new HighGear);
    // driverController.whenPressed(XboxController.down, new LowGear);
    // driverController.whenPressed(XboxController.RIGHT_BUMPER, new Intake);

    //manipulatorController.whenPressed(XboxController.Trigger.RIGHT_TRIGGER, new GeneralizedReleaseRoutine(indexer, turret));
    // manipulatorController.whenPressed(XboxController.Trigger.RIGHT_TRIGGER, new GeneralizedReleaseRoutine(indexer, turret));
    // manipulatorController.whenPressed(XboxController.LEFT_BUMPER, new PassThrough Out);
    // manipulatorController.whenPressed(XboxController.RIGHT_BUMPER, new Passthrough In);
    // manipulatorController.whenPressed(XboxController.LEFT_JOYSTICK_BUTTON, new ManualControlClimber);
    // manipulatorController.whenPressed(XboxController.Up, new REzero);
    // manipulatorController.whenPressed(XboxController.DOWN, new ShootFromLaunchpad);
    // manipulatorController.whenPressed(XboxController.Button.A, new ClimberDown);
    //manipulatorController.whenPressed(XboxController.Button.B, new LowRung(climber,Constants.Climber.kLowClimb));
    //manipulatorController.whenPressed(XboxController.Button.Y, new MidRung(climber,Constants.Climber.kMidClimb));
    // manipulatorController.whenPressed(XboxController.Button.B, new LowRung(climber,Constants.Climber.kLowClimb));
    // manipulatorController.whenPressed(XboxController.Button.Y, new MidRung(climber,Constants.Climber.kMidClimb));
  }

  public Command getAutonomousCommand() {
    //The voltage constraint makes sure the robot doesn't exceed a certain voltage during runtime.
    var autoVoltageConstraint = 
      new DifferentialDriveVoltageConstraint(
        new SimpleMotorFeedforward(
          Constants.Trajectory.ksVolts, 
          Constants.Trajectory.ksVoltSecondsPerMeter,
          Constants.Trajectory.kaVoltSecondsSquaredPerMeter),
        Constants.Trajectory.kDriveKinematics, 10);

    //Gives the trajectory the constants determined in characterization.
    TrajectoryConfig config = 
      new TrajectoryConfig(
        Constants.Trajectory.kMaxSpeedMetersPerSecond,
        Constants.Trajectory.kMaxAccelerationMetersPerSecondSquared)
        .setKinematics(Constants.Trajectory.kDriveKinematics )
        .addConstraint(autoVoltageConstraint);    

    //Generates the actual path with given points.
    /*trajectory = TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, new Rotation2d(0)),
            // Pass through these two interior waypoints, making an 's' curve path
            List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
            // End 3 meters straight ahead of where we started, facing forward
            new Pose2d(3, 0, new Rotation2d(0)),
            // Pass config
            config);*/
    
    
    try {
      Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON); 

      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
    } catch (IOException ex) {
      DriverStation.reportError("Unable to open trajectory: " + trajectoryJSON, ex.getStackTrace());
    }
   
    
    Trigger manyBalls = new Trigger() {
    @Override
    public boolean get() {
        return (indexer.ballQueue.size() > 2);
        }
    };
    manyBalls.whenActive((new RunCommand(this::GeneralizedReleaseRoutine))
    .withInterrupt(this.indexer::hasFewBalls)
    ));


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
        drivetrain
    );

    drivetrain.resetOdometry(trajectory.getInitialPose());
    return ramseteCommand.andThen(() -> drivetrain.tankDriveVolts(0,0));
  }

  public void doSendables(){
    SmartDashboard.putNumber("Encoder", drivetrain.getAverageEncoderDistance());
    SmartDashboard.putString("Gearshifter",gearShifter.getState().toString());
    SmartDashboard.putNumber("Yaw", gyro.getYaw());
    SmartDashboard.putNumber("Altitude", gyro.getAltitude());
    SmartDashboard.putNumber("Joystick Value", driverController.getAxisValue(Axis.LEFT_Y));
    SmartDashboard.putNumber("Left voltage", drivetrain.getLeftVoltage());
    SmartDashboard.putNumber("Right voltage", drivetrain.getRightVoltage());
    //Are these encoder positions correct?
    SmartDashboard.putNumber("Left Position", Constants.Trajectory.kDistPerRot * drivetrain.getLeftEncoder().getPosition() / 42);
    SmartDashboard.putNumber("Right Position", Constants.Trajectory.kDistPerRot * drivetrain.getRightEncoder().getPosition() / 42);
    SmartDashboard.putNumber("System pressure", pneumatics.getPressure());
    SmartDashboard.putBoolean("System pressure switch tripped", pneumatics.pressureSwitchTripped());
    SmartDashboard.putNumber("Left Encoder Ticks", drivetrain.getLeftEncoder().getPosition() * 4096);
    SmartDashboard.putNumber("Right Encoder Ticks", drivetrain.getRightEncoder().getPosition() * 4096);
  }
}

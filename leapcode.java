import java.io.File;
import java.io.IOException;
import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Controller.PolicyFlag;

public class Mouse2 {
	public static void main(String[] args) throws AWTException {
		Controller controller = new Controller();
		controller.setPolicyFlags(PolicyFlag.POLICY_BACKGROUND_FRAMES);

		SampleListener listener = new SampleListener();
		controller.addListener(listener);
		// controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		// controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);

		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		controller.removeListener(listener);
	}
}

class SampleListener extends Listener {

	boolean readyForControl = false;
	int screenWidth;
	int screenHeight;
	boolean iBoxGet = false;
	InteractionBox iBox = null;
	Robot robot;
	boolean isMoving = false;
	boolean unGrip = false;
	boolean wasFacingDown = true;
	boolean wasInTabState = false;
	boolean wasTabbing = false;
	boolean justCircleGestured = false;
	boolean isResizing = false;

	public void onConnect(Controller controller) {
		System.out.println("Connected");
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHeight = gd.getDisplayMode().getHeight();
		System.out.println("Screen Resolution: X: " + screenWidth + ", H: "
				+ screenHeight);
		readyForControl = true;
		try {
			robot = new Robot();
			robot.setAutoDelay(5);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onFrame(Controller controller) {
		Frame frame = controller.frame(); // The latest frame
		// Frame previous = controller.frame(1); //The previous frame
		// System.out.println("Frame available");
		if (!iBoxGet) {
			iBox = frame.interactionBox();
			iBoxGet = true;
			System.out.println("Interaction box set!");
		}
		// Pointable furthestFront = frame.pointables().frontmost();
		Hand rightHand = frame.hands().rightmost();
		Vector palmV = rightHand.palmVelocity();
		// System.out.println("Velocity: X: " + palmV.getX() + ", Y: " +
		// palmV.getY()
		// + ", Z: " + palmV.getZ());
		Vector palmN = rightHand.palmNormal();
		// System.out.println("Normal: X: " + palmN.getX() + ", Y: "
		// + palmN.getY() + ", Z: " + palmN.getZ());

		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		int currentMouseX = mouseLoc.x;
		int currentMouseY = mouseLoc.y;

		if (readyForControl && rightHand.confidence() > .15) {
			if (!isMoving && !wasInTabState && frame.hands().count() > 1) {
				Hand leftHand = frame.hands().leftmost();
				if (leftHand.pinchStrength() > .8
						&& rightHand.pinchStrength() > .8) {
					if (!isResizing) {
						System.out.println("Resizing...");
						robot.keyPress(KeyEvent.VK_ALT);
						robot.keyPress(KeyEvent.VK_SPACE);
						robot.keyRelease(KeyEvent.VK_SPACE);
						robot.keyRelease(KeyEvent.VK_ALT);
						robot.keyPress(KeyEvent.VK_S);
						robot.keyRelease(KeyEvent.VK_S);
						robot.keyPress(KeyEvent.VK_DOWN);
						robot.keyPress(KeyEvent.VK_RIGHT);
						robot.keyRelease(KeyEvent.VK_DOWN);
						robot.keyRelease(KeyEvent.VK_RIGHT);
						isResizing = true;
					}
				}else{
					if(isResizing){
						System.out.println("Resizing complete!");
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
						isResizing = false;
					}
				}
			}
			
			// System.out.println("Confidence: " + rightHand.confidence());
			if (rightHand.grabStrength() > .99 && !wasInTabState && !isResizing) {
				if (!isMoving && palmN.getY() < .8) {
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_SPACE);
					robot.keyRelease(KeyEvent.VK_SPACE);
					robot.keyRelease(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_R);
					robot.keyRelease(KeyEvent.VK_R);

					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_SPACE);
					robot.keyRelease(KeyEvent.VK_SPACE);
					robot.keyRelease(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_M);
					robot.keyRelease(KeyEvent.VK_M);
					robot.keyPress(KeyEvent.VK_DOWN);
					robot.keyRelease(KeyEvent.VK_DOWN);
					isMoving = true;
				}

				// System.out.println(rightHand.grabStrength());
			} else {
				// System.out.println("Not grabbing");
				if (isMoving) {

					robot.mousePress(InputEvent.BUTTON1_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_MASK);
					isMoving = false;

					if (palmN.getX() != 0 && palmN.getY() != 0
							&& palmN.getZ() != 0) {
						if (palmN.getY() < -.1 && palmN.getZ() > -.8) {
							if (currentMouseY <= 8) {
								robot.keyPress(KeyEvent.VK_WINDOWS);
								robot.keyPress(KeyEvent.VK_UP);
								robot.keyRelease(KeyEvent.VK_WINDOWS);
								robot.keyRelease(KeyEvent.VK_UP);
							} else {
								if (screenWidth - currentMouseX <= 12) {
									robot.keyPress(KeyEvent.VK_WINDOWS);
									robot.keyPress(KeyEvent.VK_RIGHT);
									robot.keyRelease(KeyEvent.VK_WINDOWS);
									robot.keyRelease(KeyEvent.VK_RIGHT);
								} else if (currentMouseX <= 12) {
									robot.keyPress(KeyEvent.VK_WINDOWS);
									robot.keyPress(KeyEvent.VK_LEFT);
									robot.keyRelease(KeyEvent.VK_WINDOWS);
									robot.keyRelease(KeyEvent.VK_LEFT);
								}
							}
						} else {
							System.out.println("Normal: X: " + palmN.getX()
									+ ", Y: " + palmN.getY() + ", Z: "
									+ palmN.getZ());
							robot.keyPress(KeyEvent.VK_ALT);
							robot.keyPress(KeyEvent.VK_SPACE);
							robot.keyRelease(KeyEvent.VK_SPACE);
							robot.keyRelease(KeyEvent.VK_ALT);
							robot.keyPress(KeyEvent.VK_N);
							robot.keyRelease(KeyEvent.VK_N);
						}
					}
				}
			}

			if (!isMoving && !isResizing) {
				if (palmN.getY() < -.8 && palmN.getZ() > -.5) {
					wasFacingDown = true;
					wasTabbing = false;
					if (wasInTabState) {
						robot.keyPress(KeyEvent.VK_ENTER);
						robot.keyRelease(KeyEvent.VK_ENTER);
						wasInTabState = false;
					}
				} else if (palmN.getY() >= .8 && wasFacingDown
						&& !wasInTabState) {
					System.out.println("Alt tabbing");
					wasFacingDown = false;
					wasInTabState = true;

					wasTabbing = false;
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_CONTROL);
					robot.keyPress(KeyEvent.VK_TAB);
					robot.delay(100);
					robot.keyRelease(KeyEvent.VK_TAB);
					robot.keyRelease(KeyEvent.VK_CONTROL);
					robot.keyRelease(KeyEvent.VK_ALT);
					try {
						 
						        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("F:/Geeve/Projects/ATOM/PC/Sound Files/Internet.wav").getAbsoluteFile());
						        Clip clip = AudioSystem.getClip();
						        clip.open(audioInputStream);
						        clip.start();
						   
						Runtime.getRuntime().exec(
								
								"cmd /c start " + "start chrome");
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch(Exception ex) {
				        System.out.println("Error with playing sound.");
				        ex.printStackTrace();
				        
				        
				        
				    }
				}
					robot.delay(300);
				} else if (wasInTabState && !wasFacingDown && !wasTabbing
						&& palmN.getY() < .45) {

					wasTabbing = true;
				} else if (wasInTabState && !wasFacingDown && wasTabbing
						&& palmN.getY() > .75) {
					robot.keyPress(KeyEvent.VK_TAB);
					robot.keyRelease(KeyEvent.VK_TAB);
					wasTabbing = false;
				}
			}

			/*
			 * if (!isMoving && !wasInTabState) { /* if(palmN.getZ() <= -.7 &&
			 * rightHand.grabStrength() < .1){
			 * System.out.println("Palm vertical velocity: " +
			 * rightHand.palmVelocity().getY()); //float resultVerticalV =
			 * Math.round(Math.abs(rightHand.palmVelocity().getY()) - 1);
			 * //if(resultVerticalV > 0){ robot.mouseWheel((int)
			 * Math.round(((rightHand.palmVelocity().getY()) / 500))); //}
			 * }else{
			 */

			if (!isMoving && !wasInTabState && frame.gestures().count() > 0
					&& frame.hands().count() == 1 && !isResizing) {
				CircleGesture circleGesture = new CircleGesture(frame
						.gestures().get(0));
				// System.out.println("Pinch strength: " +
				// rightHand.pinchStrength());
				if (circleGesture.durationSeconds() > .5 && !justCircleGestured
						&& rightHand.pinchStrength() > .8) {
					System.out.println("Closed a window!");
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_F4);
					robot.keyRelease(KeyEvent.VK_F4);
					robot.keyRelease(KeyEvent.VK_ALT);
					justCircleGestured = true;
				}
			} else {
				justCircleGestured = false;
			}


			float xSpeed = (palmV.getX() / 6);
			float ySpeed = (palmV.getY() / 6);
			// System.out.println("xSpeed: " + xSpeed + ", ySpeed: " + ySpeed);
			robot.mouseMove((int) (currentMouseX + xSpeed),
					(int) (currentMouseY - ySpeed));

			// }
		}
	}




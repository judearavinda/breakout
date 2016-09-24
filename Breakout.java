import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Breakout extends JPanel {

	// speed of the ball
	private static int ballSpeed = 7;

	// window dimensions
	private static int windowWidth = 800;
	private static int windowHeight = 600;

	private int room = 0;

	// frames per second
	private static int UPDATE_RATE = 60;

	// game objects
	private Ball ball;
	private int score;
	private Map<Integer, Color> colorMap;
	private Paddle paddle;
	private List<Brick> bricks;
	private boolean inGame = true;
	private boolean startGame = false;
	private int lives = 3;

	private boolean destroyedAll = false;

	public Breakout() {

		Thread gameThread = new Thread() {
			public void run() {
				gameInit();
				while (true) {
					update();
					repaint();
					setDoubleBuffered(true);
					try {
						Thread.sleep(1000 / UPDATE_RATE);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		setFocusable(true);
		this.addMouseMotionListener(new ML());
		this.addMouseListener(new MCL());
		gameThread.start();

	}

	private void gameInit() {
		paddle = new Paddle(350, 500, 100, 10);
		ball = new Ball((int) ((paddle.getRect().getMinX() + paddle.getRect().getMaxX()) / 2),
				(int) paddle.getRect().getMinY() - paddle.getHeight(), 10, 10, Color.BLACK);
		bricks = new ArrayList<Brick>();
		int x_from_last = 5;
		int y_from_last = 100;
		int x_block_increment = 5;
		int y_block_increment = 30;

		Color[] color = { Color.WHITE, Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE };
		colorMap = new HashMap<Integer, Color>();
		int hits = 5;
		for (int i = 0; i < color.length; i++) {
			colorMap.put(hits, color[i]);
			hits -= 1;
		}
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 14; j++) {
				Brick brick = new Brick(x_from_last, y_from_last, 50, 10, i + 1);
				x_from_last += brick.width + x_block_increment;
				bricks.add(brick);
			}
			// reset for next level
			y_from_last += y_block_increment;
			x_from_last = x_block_increment;
		}

	}

	private void update() {
		if (startGame) {
			ball.move();
			checkCollision();
		}
	}

	private void checkCollision() {

		if ((ball.getRect()).intersects(paddle.getRect())) {
			checkCollision(ball, paddle);
		}

		for (int i = 0; i < bricks.size(); i++) {

			if ((ball.getRect()).intersects(bricks.get(i).getRect()) && !bricks.get(i).isDestroyed()) {
				// get the position
				checkCollision(ball, bricks.get(i));
				bricks.get(i).hit();
				score += 10;
			}
		}
		destroyedAll = true;
		for (int i = 0; i < bricks.size(); i++) {

			if (!bricks.get(i).isDestroyed()) {
				destroyedAll = false;
			}
		}
	}

	private void checkCollision(Ball ball, Base rect2) {
		if (ball.getRect().getMinY() <= rect2.getRect().getMinY() - (rect2.getRect().getHeight() / 2)) {
			// Hit was from above the brick
			ball.setBallSpeedY(-ball.getBallSpeedY());
			ball.setY((int) rect2.getRect().getMinY() - ball.getHeight());
		} else if (ball.getRect().getMaxY() >= rect2.getRect().getMaxY() + (rect2.getRect().getHeight() / 2)) {
			// Hit was from below the brick
			ball.setBallSpeedY(-ball.getBallSpeedY());
			ball.setY((int) rect2.getRect().getMaxY());
		} else if (ball.getRect().getMinX() < rect2.getRect().getMinX()) {
			// Hit was from left on the brick
			ball.setBallSpeedX(-ball.getBallSpeedX());
			ball.setX((int) rect2.getRect().getMinX() - ball.getWidth());
		} else if (ball.getRect().getMinX() > rect2.getRect().getMinX()) {
			// Hit was from right on the brick
			ball.setBallSpeedX(-ball.getBallSpeedX());
			ball.setX((int) rect2.getRect().getMaxX());
		}
	}

	private void stopVictoryGame(Graphics g2d) {
		Font font = new Font("Verdana", Font.BOLD, 18);
		FontMetrics metr = this.getFontMetrics(font);

		g2d.setColor(Color.BLACK);
		g2d.setFont(font);
		String message = "You have finished the game. You have a score of " + score;
		g2d.drawString(message, (this.getWidth() - metr.stringWidth(message)) / 2, this.getHeight() / 2);

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// ResizeObjects();

		Graphics2D g2d = (Graphics2D) g;

		if (this.getWidth() > 0 && this.getHeight() > 0) {
			g2d.scale((float) this.getWidth() / windowWidth, (float) this.getHeight() / windowHeight);
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		if (room == 0) {
			// draw splash screen
			createSplashScreen(g2d);
		} else if (inGame) {
			if (destroyedAll) {
				stopVictoryGame(g2d);
			} else {

				drawObjects(g2d);
			}
		} else {

			gameFinished(g2d);
		}

		Toolkit.getDefaultToolkit().sync();
	}

	private void createSplashScreen(Graphics2D g2d) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(System.getProperty("user.dir") + "//images//splashscreen.png"));
		} catch (IOException e) {
		}
		g2d.drawImage(img, null, 0, 0);
	}

	private void drawObjects(Graphics2D g2d) {
		//

		if (lives > 0) {
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2d.setColor(Color.WHITE);
			g2d.fillOval(ball.getX(), ball.getY(), ball.width, ball.height);
			g2d.fillRoundRect(paddle.getX(), paddle.y, paddle.width, paddle.height, 10, 10);
			if (!startGame) {
				ball.setX((int) (paddle.getRect().getMinX() + paddle.getRect().getMaxX()) / 2);
				ball.setY((int) paddle.getRect().getMinY() - paddle.getHeight());
			}

			for (int i = 0; i < bricks.size(); i++) {
				if (!(bricks.get(i)).isDestroyed()) {
					g2d.setColor(bricks.get(i).getColor());
					g2d.fillRect(bricks.get(i).getX(), bricks.get(i).getY(), bricks.get(i).getWidth(),
							bricks.get(i).getHeight());
				}
			}
			drawLives(g2d);
			drawScore(g2d);
			drawFPS(g2d);
		} else {
			gameFinished(g2d);
		}
	}

	private void drawFPS(Graphics2D g2d) {
		Font font = new Font("Verdana", Font.BOLD, 18);
		FontMetrics metr = this.getFontMetrics(font);

		g2d.setColor(Color.RED);
		g2d.setFont(font);
		g2d.drawString("FPS: " + UPDATE_RATE, 0, 60);

	}

	private void drawScore(Graphics2D g2d) {

		Font font = new Font("Verdana", Font.BOLD, 18);
		FontMetrics metr = this.getFontMetrics(font);

		g2d.setColor(Color.GREEN);
		g2d.setFont(font);
		g2d.drawString("Score: " + score, 0, 40);
	}

	private void drawLives(Graphics2D g2d) {

		Font font = new Font("Verdana", Font.BOLD, 18);
		FontMetrics metr = this.getFontMetrics(font);

		g2d.setColor(Color.WHITE);
		g2d.setFont(font);
		g2d.drawString("Lives: " + lives, 0, 20);
	}

	private void gameFinished(Graphics2D g2d) {

		Font font = new Font("Verdana", Font.BOLD, 18);
		FontMetrics metr = this.getFontMetrics(font);

		g2d.setColor(Color.BLACK);
		g2d.setFont(font);
		String message = "You lost all your lives. You have a score of " + score;
		g2d.drawString(message, (this.getWidth() - metr.stringWidth(message)) / 2, this.getHeight() / 2);
	}

	class ML extends MouseMotionAdapter {

		public void mouseMoved(MouseEvent e) {
			int xMousePosition = e.getX();
			if (xMousePosition < paddle.x)
				paddle.goLeft();
			if (xMousePosition > paddle.x)
				paddle.goRight();
		}
	}

	class MCL extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				startGame = true;
				if (room == 0) {
					room = 1;
				}

			}
		}
	}

	public class Base {
		protected int x;
		protected int y;
		protected int width;
		protected int height;
		protected Color colour;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		Rectangle getRect() {
			return new Rectangle(x, y, this.getWidth(), this.getHeight());
		}
	}

	public class Ball extends Base {

		public float ballSpeedX;
		public float ballSpeedY;

		public Ball(int x, int y, int width, int height, Color colour) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.colour = colour;
			this.ballSpeedX = (float) (Breakout.ballSpeed * Math.cos(0.785398));
			this.ballSpeedY = (float) (Breakout.ballSpeed * Math.sin(0.785398));
		}

		public void move() {

			x += ballSpeedX;
			y += ballSpeedY;
			// check for outside borders and bounce off the wall
			if (ball.getRect().getMinX() < 0) {
				ball.ballSpeedX = -ball.ballSpeedX;
			} else if (ball.getRect().getMaxX() >= windowWidth - ball.width) {
				ball.ballSpeedX = -ball.ballSpeedX;
			} else if (ball.getRect().getMaxY() > windowHeight - ball.height) {
				ball.ballSpeedY = -ball.ballSpeedY / 2;
				resetBallOnPaddle();
				lives -= 1;
			} else if (ball.getRect().getMinY() < 0) {
				ball.ballSpeedY = -ball.ballSpeedY;
			}
		}

		private void resetBallOnPaddle() {
			paddle = new Paddle(350, 500, 100, 10);
			ball = new Ball((int) ((paddle.getRect().getMinX() + paddle.getRect().getMaxX()) / 2),
					(int) paddle.getRect().getMinY() - paddle.getHeight(), 10, 10, Color.BLACK);
			startGame = false;
		}

		public float getBallSpeedX() {
			return ballSpeedX;
		}

		public void setBallSpeedX(float ballSpeedX) {
			this.ballSpeedX = ballSpeedX;
		}

		public float getBallSpeedY() {
			return ballSpeedY;
		}

		public void setBallSpeedY(float ballSpeedY) {
			this.ballSpeedY = ballSpeedY;
		}
	}

	public class Brick extends Base {
		protected int hits;
		protected Color colour;

		public Brick(int x, int y, int width, int height, int hits) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.colour = colorMap.get(hits);
			this.hits = hits;
		}

		public Color getColor() {
			return colour;
		}

		public void setColor(Color color) {
			colour = color;
		}

		public boolean isDestroyed() {
			return hits == 0;
		}

		public void hit() {
			hits -= 1;
			this.colour = colorMap.get(hits);
		}
	}

	public class Paddle extends Base {
		private int dx = 5;

		public Paddle(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void goLeft() {
			if (this.x - dx >= 0) {
				paddle.setX(paddle.getX() - dx);
			} else {
				paddle.setX(0);
			}
		}

		public void goRight() {
			if (this.x + dx + this.width + 1 <= windowWidth) {
				paddle.setX(paddle.getX() + dx + 1);
			} else {
				paddle.setX(windowWidth - this.width);
			}
		}
	}

	public static void main(String[] args) {
		// Set up main window (using Swing's Jframe)
		JFrame frame = new JFrame("Breakout");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// get the arguments
		int firstArg;
		int secondArg;
		if (args.length > 0) {
			try {
				firstArg = Integer.parseInt(args[0]);
				UPDATE_RATE = firstArg;
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[0] + " must be an integer.");
				System.exit(1);
			}
		}
		if (args.length > 1) {
			try {
				secondArg = Integer.parseInt(args[1]);
				ballSpeed = secondArg;
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[1] + " must be an integer.");
				System.exit(1);
			}
		}
		frame.setContentPane(new Breakout());
		frame.setSize(new Dimension(800, 600));
		frame.setVisible(true);
	}
}
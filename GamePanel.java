import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 75;

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    ArrayList<Point> obstacles = new ArrayList<>();

    JButton startButton;
    JButton restartButton;
    JButton backToMenuButton;
    boolean inMenu = true;

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        // Start Button
        startButton = new JButton("Start Game");
        startButton.setFocusable(false);
        startButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - 25, 200, 50);
        startButton.addActionListener(e -> {
            inMenu = false;
            startButton.setVisible(false);
            startGame();
        });
        this.add(startButton);

        // Restart Button
        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.setBounds(SCREEN_WIDTH / 2 - 75, SCREEN_HEIGHT / 2 + 50, 150, 40);
        restartButton.addActionListener(e -> resetGame());
        this.add(restartButton);

        // Back to Menu Button
        backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFocusable(false);
        backToMenuButton.setVisible(false);
        backToMenuButton.setBounds(SCREEN_WIDTH / 2 - 75, SCREEN_HEIGHT / 2 + 100, 150, 40);
        backToMenuButton.addActionListener(e -> {
            inMenu = true;
            restartButton.setVisible(false);
            backToMenuButton.setVisible(false);
            repaint();
            startButton.setVisible(true);
        });
        this.add(backToMenuButton);

        this.setLayout(null);
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        obstacles.clear();
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        restartButton.setVisible(false);
        backToMenuButton.setVisible(false);
        startGame();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inMenu) {
            drawMenu(g);
        } else {
            draw(g);
        }
    }

    public void drawMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 60));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Snake Game", (SCREEN_WIDTH - metrics.stringWidth("Snake Game")) / 2, SCREEN_HEIGHT / 2 - 100);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw obstacles
            g.setColor(Color.gray);
            for (Point p : obstacles) {
                g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Draw score
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 30));
            g.drawString("Score: " + applesEaten, 10, 30);
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        boolean valid;
        do {
            valid = true;
            appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE)) * UNIT_SIZE;
            for (Point p : obstacles) {
                if (p.x == appleX && p.y == appleY) {
                    valid = false;
                    break;
                }
            }
        } while (!valid);
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            addObstacle(); // Add new obstacle after eating
        }
    }

    public void addObstacle() {
        Point p;
        do {
            int ox = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int oy = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            p = new Point(ox, oy);
        } while (isOnSnake(p) || isOnApple(p));
        obstacles.add(p);
    }

    public boolean isOnSnake(Point p) {
        for (int i = 0; i < bodyParts; i++) {
            if (x[i] == p.x && y[i] == p.y) return true;
        }
        return false;
    }

    public boolean isOnApple(Point p) {
        return p.x == appleX && p.y == appleY;
    }

    public void checkCollisions() {
        // Snake head hits body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }

        // Wall collisions
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        // Obstacle collisions
        for (Point p : obstacles) {
            if (x[0] == p.x && y[0] == p.y) {
                running = false;
                break;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, SCREEN_HEIGHT / 2 - 60);

        // Game Over
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        // Show buttons
        restartButton.setVisible(true);
        backToMenuButton.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                }
            }
        }
    }
}

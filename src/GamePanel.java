import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 75;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
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

    BufferedImage spriteSheet;
    BufferedImage snakeUp, snakeLeft, snakeDown, snakeRight, bodyImg, appleImg, obstacleImg,bgImage, menuBgImage;

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        try {
            InputStream is = getClass().getResourceAsStream("/resources/images/spritesheet.png");
            bgImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/bgimage.jpg"));
            menuBgImage = ImageIO.read(getClass().getResourceAsStream("/resources/images/menu_bg.png"));
            
            spriteSheet = ImageIO.read(is);

            snakeUp = spriteSheet.getSubimage(8, 0, 8, 8);      // Frame 2
            snakeLeft = spriteSheet.getSubimage(16, 0, 8, 8);   // Frame 3
            snakeDown = spriteSheet.getSubimage(24, 0, 8, 8);   // Frame 4
            snakeRight = spriteSheet.getSubimage(32, 0, 8, 8);  // Frame 5
            bodyImg = spriteSheet.getSubimage(40, 0, 8, 8);     // Frame 6 
            appleImg = spriteSheet.getSubimage(48, 0, 8, 8);    // Frame 7 
            obstacleImg =spriteSheet.getSubimage(32, 8, 8,8);   // Bricks bruh
            } catch (IOException e) {
            e.printStackTrace();
            }


        startButton = new JButton("Start Game");
        startButton.setFocusable(false);
        startButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - 25, 200, 50);
        startButton.addActionListener(e -> {
            inMenu = false;
            startButton.setVisible(false);
            startGame();
        });
        this.add(startButton);

        restartButton = new JButton("Restart");
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.setBounds(SCREEN_WIDTH / 2 - 75, SCREEN_HEIGHT / 2 + 50, 150, 40);
        restartButton.addActionListener(e -> resetGame());
        this.add(restartButton);

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
    // Draw background image first
    g.drawImage(menuBgImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);

    // Then draw menu text on top
    g.setColor(Color.white);
    g.setFont(new Font("Ink Free", Font.BOLD, 60));
    FontMetrics metrics = getFontMetrics(g.getFont());
    g.drawString("Snake Game", (SCREEN_WIDTH - metrics.stringWidth("Snake Game")) / 2, SCREEN_HEIGHT / 2 - 100);
}


    public void draw(Graphics g) {
    if (running) {
        // Draw background first
        g.drawImage(bgImage, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);

        // Draw apple
        g.drawImage(appleImg, appleX, appleY, UNIT_SIZE, UNIT_SIZE, null);

        // Draw obstacles
        for (Point p : obstacles) {
            g.drawImage(obstacleImg, p.x, p.y, UNIT_SIZE, UNIT_SIZE, null);
        }

        // Draw snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) { // Draw head of snake
                switch (direction) {
                    case 'U' -> g.drawImage(snakeUp, x[i], y[i], UNIT_SIZE, UNIT_SIZE, null);
                    case 'D' -> g.drawImage(snakeDown, x[i], y[i], UNIT_SIZE, UNIT_SIZE, null);
                    case 'L' -> g.drawImage(snakeLeft, x[i], y[i], UNIT_SIZE, UNIT_SIZE, null);
                    case 'R' -> g.drawImage(snakeRight, x[i], y[i], UNIT_SIZE, UNIT_SIZE, null);
                }
            } else { // Draw body of snake
                g.drawImage(bodyImg, x[i], y[i], UNIT_SIZE, UNIT_SIZE, null);
            }
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
            appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
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
            addObstacle();
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
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                break;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

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
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, SCREEN_HEIGHT / 2 - 60);

        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

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

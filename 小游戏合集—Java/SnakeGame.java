import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * 贪吃蛇游戏主窗口类，继承自JFrame，实现游戏的核心逻辑和界面显示。
 * 包含游戏初始化、蛇的移动控制、碰撞检测、分数计算、难度调整等功能。
 */
public class SnakeGame extends JFrame {
    private MainFrame mainFrame; // 主菜单窗口引用，用于返回主页
    private final int BOARD_SIZE = 25; // 游戏棋盘尺寸（25x25格）
    private final int CELL_SIZE = 20; // 每格像素大小（20x20像素）
    private ArrayList<Point> snake; // 蛇的身体坐标集合（头部在索引0）
    private Point food; // 普通食物的位置
    private Point blackFood; // 黑色食物的位置
    private boolean isBlackFoodActive = false; // 黑色食物是否显示
    private Timer blackFoodTimer; // 控制黑色食物周期的计时器（显示/隐藏切换）
    private final int BLACK_FOOD_DURATION = 30000; // 黑色食物显示时长（30秒）
    private final int BLACK_FOOD_HIDE_DURATION = 10000; // 黑色食物隐藏时长（10秒）
    private Direction direction; // 当前移动方向
    private Direction nextDirection; // 临时存储下一个方向（避免连续按键丢失）
    private Timer timer; // 游戏主循环计时器（控制移动速度）
    private boolean isRunning; // 游戏是否运行中
    private boolean isPaused; // 游戏是否暂停
    private int countdown; // 初始倒计时变量（3秒）
    private int score = 0; // 当前分数
    private int difficultyDelay = 200; // 默认中级难度延迟（移动间隔200ms）

    /**
     * 构造方法，初始化游戏窗口和难度参数
     * @param mainFrame 主菜单窗口引用，用于返回主页
     * @param difficulty 游戏难度（"初级"/"中级"/"高级"）
     */
    public SnakeGame(MainFrame mainFrame, String difficulty) {
        this.mainFrame = mainFrame;
        setTitle("贪吃蛇");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 调整窗口尺寸为棋盘大小+适当边框空间
        // 调整窗口尺寸为棋盘大小+固定边距（上下各30，左右各20）
        setSize(BOARD_SIZE * CELL_SIZE + 40, BOARD_SIZE * CELL_SIZE + 100);
        setLocationRelativeTo(mainFrame);
        setLocationRelativeTo(mainFrame);
        // 使用传入的难度参数设置延迟
        switch (difficulty) {
            case "初级": difficultyDelay = 300; break;
            case "高级": difficultyDelay = 100; break;
            default: difficultyDelay = 200; // 中级
        }
        initGame();
        initUI();
    }

    /**
     * 初始化游戏核心状态：蛇的初始位置、食物生成、倒计时和游戏计时器
     */
    private void initGame() {
        // 移除重复的难度选择对话框，使用构造方法传入的难度参数

        snake = new ArrayList<>();
        score = 0; // 初始化分数
        // 固定初始位置为棋盘中心（原点）
        int startX = BOARD_SIZE / 2;
        int startY = BOARD_SIZE / 2;
        for (int i = 0; i < 5; i++) {
            snake.add(new Point(startX - i, startY)); // 沿x轴正向生成5个连续点
        }
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT; // 初始化临时方向
        generateFood();
        isRunning = true;
        isPaused = true; // 初始状态为暂停（倒计时期间不可操作）
        // 停止旧计时器避免重复运行
        if(timer != null) timer.stop();
        timer = new Timer(difficultyDelay, new GameLoop());
        // 初始化倒计时为3秒
        countdown = 3;
        isPaused = true; // 倒计时期间保持暂停状态
        // 倒计时计时器（每秒递减）
        Timer countdownTimer = new Timer(1000, e -> {
            if (countdown > 0) {
                countdown--;
                repaint();
            } else {
                ((Timer)e.getSource()).stop();
                isPaused = false; // 倒计时结束后恢复可操作状态
                move(); // 立即触发一次移动确保流畅启动
            }
        });
        countdownTimer.start();
        timer.start();

        // 初始化黑色食物计时器
        blackFoodTimer = new Timer(BLACK_FOOD_DURATION + BLACK_FOOD_HIDE_DURATION, new ActionListener() {
            private boolean showing = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (score >= 30) {
                    if (showing) {
                        // 隐藏黑色食物
                        isBlackFoodActive = false;
                        ((Timer) e.getSource()).setDelay(BLACK_FOOD_HIDE_DURATION);
                    } else {
                        // 生成并显示黑色食物
                        generateBlackFood();
                        isBlackFoodActive = true;
                        ((Timer) e.getSource()).setDelay(BLACK_FOOD_DURATION);
                    }
                    showing = !showing;
                }
            }
        });
        blackFoodTimer.setInitialDelay(0); // 初始延迟为0
        blackFoodTimer.start();
    }

    /**
     * 初始化游戏界面组件：分数显示面板、游戏主面板布局、键盘监听
     */
    private void initUI() {
        // 分数显示面板
        JPanel scorePanel = new JPanel();
        JLabel scoreLabel = new JLabel("分数：0");
        scoreLabel.setFont(new Font("宋体", Font.BOLD, 20));
        scorePanel.add(scoreLabel);

        // 游戏主面板
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制棋盘
                g.setColor(Color.BLACK);
                for (int i = 0; i <= BOARD_SIZE; i++) {
                    g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
                    g.drawLine(0, i * CELL_SIZE, BOARD_SIZE * CELL_SIZE, i * CELL_SIZE);
                }
                // 绘制蛇
                g.setColor(Color.GREEN);
                for (Point p : snake) {
                    g.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                }
                // 绘制食物
                if (isBlackFoodActive && blackFood != null) {
                    g.setColor(Color.BLACK);
                    g.fillOval(blackFood.x * CELL_SIZE, blackFood.y * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                } else {
                    g.setColor(Color.RED);
                    g.fillOval(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                }

                // 绘制倒计时
                if (countdown > 0) {
                    g.setFont(new Font("宋体", Font.BOLD, 80));
                    g.setColor(Color.BLUE);
                    String text = String.valueOf(countdown);
                    // 计算文本居中位置
                    FontMetrics fm = g.getFontMetrics();
                    int x = (BOARD_SIZE * CELL_SIZE - fm.stringWidth(text)) / 2;
                    int y = (BOARD_SIZE * CELL_SIZE + fm.getAscent()) / 2;
                    g.drawString(text, x, y);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));

        // 调整窗口布局
        setLayout(new BorderLayout());
        add(scorePanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        // 同步分数显示
        Timer scoreUpdateTimer = new Timer(100, e -> {
            scoreLabel.setText("分数：" + score);
        });
        scoreUpdateTimer.start();

        // 键盘监听
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP: if (countdown <= 0 && direction != Direction.DOWN) nextDirection = Direction.UP; break;
                    case KeyEvent.VK_DOWN: if (countdown <= 0 && direction != Direction.UP) nextDirection = Direction.DOWN; break;
                    case KeyEvent.VK_LEFT: if (countdown <= 0 && direction != Direction.RIGHT) nextDirection = Direction.LEFT; break;
                    case KeyEvent.VK_RIGHT: if (countdown <= 0 && direction != Direction.LEFT) nextDirection = Direction.RIGHT; break;
                    case KeyEvent.VK_ESCAPE: 
    isPaused = !isPaused; 
    if (isPaused) {
        Object[] options = {"继续", "重新开始并选择难度", "返回主页"};
        int choice = JOptionPane.showOptionDialog(SnakeGame.this, "游戏已暂停", "暂停",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            isPaused = false;
        } else if (choice == 1) {
            String[] difficulties = {"初级", "中级", "高级"};
            String selected = (String) JOptionPane.showInputDialog(SnakeGame.this, "选择难度:", "贪吃蛇难度",
                    JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);
            if (selected != null) {
                // 更新难度延迟并重新初始化游戏
                switch (selected) {
                    case "初级": difficultyDelay = 300; break;
                    case "高级": difficultyDelay = 100; break;
                    default: difficultyDelay = 200;
                }
                initGame(); // initGame内部已处理timer启动
                isPaused = false;
            }
        } else {
            dispose();
            mainFrame.setVisible(true);
        }
    }
    break; // 切换暂停状态
                }
            }
        });
        setFocusable(true);
    }

    private void generateFood() {
        Random random = new Random();
        Point newFood;
        do {
            newFood = new Point(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE));
        } while (snake.contains(newFood));
        food = newFood;
    }

    private void generateBlackFood() {
        Random random = new Random();
        Point newBlackFood;
        do {
            newBlackFood = new Point(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE));
        } while (snake.contains(newBlackFood) || newBlackFood.equals(food));
        blackFood = newBlackFood;
    }

    /**
     * 游戏核心移动逻辑：更新蛇的位置、检测碰撞、处理食物交互
     * 1. 更新移动方向（避免反向移动）
     * 2. 计算新头部坐标
     * 3. 检测边界碰撞和自碰撞
     * 4. 根据是否吃到食物决定是否增长蛇身
     * 5. 处理黑色食物特殊逻辑（缩短蛇身并扣分）
     */
    private void move() {
        // 更新当前方向为临时方向（避免连续按键丢失）
        if (nextDirection != direction) {
            direction = nextDirection;
        }
        Point head = snake.get(0);
        Point newHead = new Point();
        switch (direction) {
            case UP: newHead.setLocation(head.x, head.y - 1); break;
            case DOWN: newHead.setLocation(head.x, head.y + 1); break;
            case LEFT: newHead.setLocation(head.x - 1, head.y); break;
            case RIGHT: newHead.setLocation(head.x + 1, head.y); break;
        }

        // 碰撞检测
        if (newHead.x < 0 || newHead.x >= BOARD_SIZE || newHead.y < 0 || newHead.y >= BOARD_SIZE
                || snake.contains(newHead)) {
            gameOver();
            return;
        }

        snake.add(0, newHead);
        if (newHead.equals(food)) {
            score++; // 分数增加
            // 检查是否需要加速
            if (score % 10 == 0) {
                difficultyDelay = Math.max(difficultyDelay - 20, 50); // 最小延迟50ms
                timer.setDelay(difficultyDelay);
            }
            generateFood();
        } else if (newHead.equals(blackFood) && isBlackFoodActive) {
            // 处理黑色食物碰撞
            int newLength = snake.size() / 2;
            if (newLength < 4) {
                gameOver();
                return;
            }
            // 截断蛇的长度
            while (snake.size() > newLength) {
                snake.remove(snake.size() - 1);
            }
            score = (int) (score * 0.4); // 扣除60%分数
            isBlackFoodActive = false; // 隐藏黑色食物
        } else {
            snake.remove(snake.size() - 1);
        }
        repaint();
    }

    /**
     * 游戏结束处理逻辑：停止计时器、显示得分、提供重新开始/返回主页选项
     */
    private void gameOver() {
        isRunning = false;
        timer.stop();
        JOptionPane.showMessageDialog(this, "游戏结束！得分：" + score, "提示", JOptionPane.INFORMATION_MESSAGE);
        Object[] options = {"重新开始", "返回主页"};
        int choice = JOptionPane.showOptionDialog(this, "是否重新开始？", "提示",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            String[] difficulties = {"初级", "中级", "高级"};
            String selected = (String) JOptionPane.showInputDialog(this, "选择难度:", "贪吃蛇难度",
                    JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);
            if (selected != null) {
                switch (selected) {
                    case "初级": difficultyDelay = 300; break;
                    case "高级": difficultyDelay = 100; break;
                    default: difficultyDelay = 200;
                }
                initGame();
                timer.start();
            }
        } else {
            dispose();
            mainFrame.setVisible(true);
        }
    }

    /**
     * 移动方向枚举类，定义上下左右四个方向
     */
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /**
     * 游戏主循环监听器，控制游戏定时移动（通过Timer触发）
     */
    private class GameLoop implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isRunning && !isPaused) {
                move();
            }
        }
    }
}
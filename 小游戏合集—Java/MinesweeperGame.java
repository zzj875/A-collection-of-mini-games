import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * 扫雷游戏主窗口类
 * 继承自JFrame，负责游戏界面初始化、难度设置、游戏逻辑处理（布雷、翻格子、插旗等）及状态管理
 */
public class MinesweeperGame extends JFrame {
        /** 主窗口引用（用于返回主页） */
    private MainFrame mainFrame;
    /** 当前游戏难度（初级/中级/高级） */
    private String currentDifficulty;
    /** 雷区行数 */
    private int rows;
    /** 雷区列数 */
    private int cols;
    /** 总雷数 */
    private int mineCount;
    /** 雷区格子按钮数组（rows行cols列） */
    private JButton[][] cells;
    /** 雷位置标记数组（true表示该位置有雷） */
    private boolean[][] isMine;
    /** 剩余未标记雷数 */
    private int remainingMines;
    /** 顶部状态栏标签（显示剩余雷数和游戏时间） */
    private JLabel statusLabel;
    /** 游戏计时器（每秒更新时间） */
    private Timer timer;
    /** 已 elapsed 游戏时间（秒） */
    private int timeElapsed = 0;

        /**
     * 构造方法：初始化游戏窗口
     * @param mainFrame 主窗口引用
     * @param difficulty 游戏难度（初级/中级/高级）
     */
    public MinesweeperGame(MainFrame mainFrame, String difficulty) {
        this.mainFrame = mainFrame;
        setTitle("扫雷 - " + difficulty);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initDifficulty(difficulty);
        initUI();
        setSize(1920, 1080);
        setLocationRelativeTo(mainFrame);
    }

        /**
     * 初始化难度参数（行数、列数、雷数）
     * @param difficulty 游戏难度字符串
     */
    private void initDifficulty(String difficulty) {
        switch (difficulty) {
            case "初级": rows = 9; cols = 9; mineCount = 10; break;
            case "中级": rows = 16; cols = 16; mineCount = 40; break;
            case "高级": rows = 30; cols = 16; mineCount = 99; break;
            default: throw new IllegalArgumentException("无效难度");
        }
        remainingMines = mineCount;
    }

        /**
     * 初始化游戏界面（状态栏、雷区网格、计时器）
     */
    private void initUI() {
        // 顶部状态栏
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("剩余雷数: " + remainingMines + " 时间: 0s");
        JButton restartBtn = new JButton("重新开始");
        restartBtn.addActionListener(e -> restartGame());
        JButton backBtn = new JButton("返回主页");
        backBtn.addActionListener(e -> {
            dispose();
            mainFrame.setVisible(true);
        });
        // 添加难度选择下拉框
        JComboBox<String> difficultyCombo = new JComboBox<>(new String[]{"初级", "中级", "高级"});
        difficultyCombo.setSelectedItem(currentDifficulty);
        difficultyCombo.addActionListener(e -> {
            String newDifficulty = (String) difficultyCombo.getSelectedItem();
            currentDifficulty = newDifficulty;
            setTitle("扫雷 - " + newDifficulty);
            initDifficulty(newDifficulty);
            restartGame();
            // 调整窗口大小为固定1920*1080
            setSize(1920, 1080);
        });
        statusPanel.add(difficultyCombo);
        statusPanel.add(statusLabel);
        statusPanel.add(restartBtn);
        statusPanel.add(backBtn);

        // 雷区网格
        JPanel minePanel = new JPanel(new GridLayout(rows, cols));
        cells = new JButton[rows][cols];
        isMine = new boolean[rows][cols];
        generateMines();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new JButton();
                cells[i][j].setPreferredSize(new Dimension(30, 30));
                cells[i][j].addMouseListener(new CellMouseListener(i, j));
                minePanel.add(cells[i][j]);
            }
        }

        // 计时器
        timer = new Timer(1000, e -> {
            timeElapsed++;
            statusLabel.setText("剩余雷数: " + remainingMines + " 时间: " + timeElapsed + "s");
        });
        timer.start();

        add(statusPanel, BorderLayout.NORTH);
        add(minePanel, BorderLayout.CENTER);
    }

        /**
     * 生成雷的位置（简化逻辑，可能重复随机但最终保证总雷数）
     */
    private void generateMines() {
        // 简化布雷逻辑（待优化：避免重复布雷）
        Random random = new Random();
        int count = 0;
        while (count < mineCount) {
            int x = random.nextInt(rows);
            int y = random.nextInt(cols);
            if (!isMine[x][y]) {
                isMine[x][y] = true;
                count++;
            }
        }
    }

        /**
     * 计算指定位置周围的雷数
     * @param x 行坐标（0-based）
     * @param y 列坐标（0-based）
     * @return 周围8邻域的雷数
     */
    private int countAdjacentMines(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                    if (isMine[nx][ny]) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

        /**
     * 展开无雷格子（递归展开相邻无雷区域）
     * @param x 起始行坐标
     * @param y 起始列坐标
     */
    private void revealCells(int x, int y) {
        // 检查坐标是否有效
        if (x < 0 || x >= rows || y < 0 || y >= cols) return;
        // 检查是否已显示或为雷
        if (!cells[x][y].isEnabled() || isMine[x][y]) return;
        int mines = countAdjacentMines(x, y);
        cells[x][y].setEnabled(false); // 标记为已显示
        if (mines > 0) {
            cells[x][y].setText(String.valueOf(mines));
            switch (mines) {
                case 1: cells[x][y].setBackground(Color.BLUE); break;
                case 2: cells[x][y].setBackground(Color.GREEN); break;
                case 3: cells[x][y].setBackground(Color.ORANGE); break;
                case 4: cells[x][y].setBackground(Color.MAGENTA); break;
                default: cells[x][y].setBackground(Color.CYAN); break;
            }
        } else {
            cells[x][y].setText("");
            cells[x][y].setBackground(Color.LIGHT_GRAY);
            // 递归展开周围8个方向
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    revealCells(x + i, y + j);
                }
            }
        }
    }

        /**
     * 重新开始游戏（重置时间、雷区、格子状态）
     */
    private void restartGame() {
        timeElapsed = 0;
        remainingMines = mineCount;
        timer.restart();
        // 重新生成雷区
        isMine = new boolean[rows][cols];
        generateMines();

        // 重新初始化雷区网格（适配新的rows/cols）
        JPanel minePanel = new JPanel(new GridLayout(rows, cols));
        cells = new JButton[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new JButton();
                cells[i][j].setPreferredSize(new Dimension(30, 30));
                cells[i][j].addMouseListener(new CellMouseListener(i, j));
                minePanel.add(cells[i][j]);
            }
        }

        // 替换旧的雷区面板
        Container contentPane = getContentPane();
        contentPane.remove(1); // 移除原来的CENTER位置面板（索引1）
        contentPane.add(minePanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }

        /**
     * 格子鼠标事件监听器（处理左键翻格子、右键插旗）
     */
    private class CellMouseListener extends MouseAdapter {
        private int x, y;

        CellMouseListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                // 检查是否已标记（右键插旗），若是则忽略左键翻格子操作
                if ("⚑".equals(cells[x][y].getText())) {
                    return;
                }
                // 左键翻格子（待实现：显示周围雷数/触发爆炸）
                if (isMine[x][y]) {
                    // 显示所有雷的位置
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            if (isMine[i][j]) {
                                cells[i][j].setBackground(Color.RED);
                            }
                        }
                    }
                    Object[] options = {"重新开始", "返回主页"};
                    int choice = JOptionPane.showOptionDialog(MinesweeperGame.this, "游戏失败！", "提示",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);
                    if (choice == 0) {
                        restartGame();
                    } else if (choice == 1) {
                        dispose();
                        mainFrame.setVisible(true);
                    }
                } else {
                    int mines = countAdjacentMines(x, y);
                    if (mines > 0) {
                        cells[x][y].setText(String.valueOf(mines));
                        // 根据数字设置不同颜色
                        switch (mines) {
                            case 1: cells[x][y].setBackground(Color.BLUE); break;
                            case 2: cells[x][y].setBackground(Color.GREEN); break;
                            case 3: cells[x][y].setBackground(Color.ORANGE); break;
                            case 4: cells[x][y].setBackground(Color.MAGENTA); break;
                            default: cells[x][y].setBackground(Color.CYAN); break;
                        }
                    } else {
                        revealCells(x, y);
                    }
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                // 右键插旗并更新剩余雷数
                String text = cells[x][y].getText();
                if (text.isEmpty()) {
                    cells[x][y].setText("⚑");
                    remainingMines--;
                } else {
                    cells[x][y].setText("");
                    remainingMines++;
                }
                statusLabel.setText("剩余雷数: " + remainingMines + " 时间: " + timeElapsed + "s");
                // 检查是否所有雷都被正确标记
                if (remainingMines == 0) {
                    boolean allMinesMarked = true;
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            if (isMine[i][j] && !"⚑".equals(cells[i][j].getText())) {
                                allMinesMarked = false;
                                break;
                            }
                        }
                        if (!allMinesMarked) break;
                    }
                    if (allMinesMarked) {
                        Object[] options = {"重新开始", "返回主页"};
                        int choice = JOptionPane.showOptionDialog(MinesweeperGame.this, "游戏胜利！", "提示",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0]);
                        if (choice == 0) {
                            restartGame();
                        } else if (choice == 1) {
                            dispose();
                            mainFrame.setVisible(true);
                        }
                    }
                }
            }
            }
        }
    }
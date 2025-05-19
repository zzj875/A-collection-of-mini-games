import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2048游戏主窗口类，继承自Swing的JFrame
 * 包含游戏核心逻辑（移动合并、分数计算）和界面组件（网格、分数面板）
 */
public class Game2048 extends JFrame {
        /** 父窗口引用（用于返回小游戏合集主界面） */
    private MainFrame mainFrame;
    /** 4x4的游戏核心网格（二维数组结构），存储每个单元格的数值（0表示空单元格） */
    private int[][] grid;
    /** 当前游戏分数（规则：等于当前网格中的最大数值） */
    private int score;
    /** 历史最高分数（记录游戏过程中达到的最大数值峰值） */
    private int highScore = 0;
    /** 界面上显示当前分数和历史最高分的文本标签组件 */
    private JLabel scoreLabel;
    /** 存放游戏网格单元格的容器面板（使用4x4网格布局） */
    private JPanel gridPanel;

    //页面定义
        /**
     * 游戏窗口构造方法
     * @param mainFrame 小游戏合集主窗口引用（用于返回主界面）
     */
    public Game2048(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("合成2048"); // 设置窗口标题
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭时仅销毁当前窗口
        setMinimumSize(new Dimension(400, 400)); // 设置最小窗口尺寸
        setLocationRelativeTo(mainFrame); // 窗口相对于主窗口居中显示
        initGame(); // 初始化游戏核心数据（网格、分数）
        initUI(); // 初始化游戏界面组件（分数面板、网格）
        pack(); // 根据组件大小自动调整窗口尺寸
    }

    // 初始化游戏
        /**
     * 初始化游戏核心状态
     * 1. 重置4x4网格为全0
     * 2. 重置当前分数为0
     * 3. 生成2个初始数字（2或4）
     */
    private void initGame() {
        grid = new int[4][4]; // 初始化4x4空网格
        score = 0; // 重置当前分数
        addNewNumber(); // 生成第一个随机数字
        addNewNumber(); // 生成第二个随机数字
    }

        /**
     * 初始化用户界面：创建分数面板、游戏网格和键盘监听
     */
    private void initUI() {
        // 分数面板
        JPanel scorePanel = new JPanel();
        scoreLabel = new JLabel("当前分数: 0  历史最高: " + highScore);
        JButton backBtn = new JButton("返回主页");
        backBtn.addActionListener(e -> {
            dispose();
            mainFrame.setVisible(true);
        });
        scorePanel.add(scoreLabel);
        scorePanel.add(backBtn);
        JButton restartBtn = new JButton("重新开始");
        restartBtn.addActionListener(e -> {
            initGame();
            score = getMaxValue(); // 重新计算当前最大数值作为新分数
            updateGridUI();
            requestFocusInWindow();
        });
        scorePanel.add(restartBtn);

        // 游戏网格
        gridPanel = new JPanel(new GridLayout(4, 4, 5, 5));
        gridPanel.setBackground(new Color(187, 173, 160));
        updateGridUI();

        // 键盘监听
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:// 上移
                    case KeyEvent.VK_W:
                        moved = moveUp();
                        break;
                    case KeyEvent.VK_DOWN://下移
                    case KeyEvent.VK_S:
                        moved = moveDown();
                        break;
                    case KeyEvent.VK_LEFT://左移
                    case KeyEvent.VK_A:
                        moved = moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT://右移 
                    case KeyEvent.VK_D:
                        moved = moveRight();
                        break;
                }
                if (moved) {
                    // 获取当前网格最大值作为当前分数
                    int currentMax = getMaxValue();
                    score = currentMax;
                    // 更新历史最高分（取当前最高分和历史最高分的较大值）
                    highScore = Math.max(highScore, currentMax);
                    // 生成新数字并刷新界面
                    addNewNumber();
                    updateGridUI();
                    // 检查游戏是否结束
                    checkGameOver();
                }
            }
        });

        setFocusable(true);
        add(scorePanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
    }

        /**
     * 在空白单元格随机生成2（90%概率）或4（10%概率）
     */
    private void addNewNumber() {
        List<Point> emptyCells = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == 0) emptyCells.add(new Point(i, j));
            }
        }
        if (!emptyCells.isEmpty()) {
            Point p = emptyCells.get(new Random().nextInt(emptyCells.size()));
            grid[p.x][p.y] = new Random().nextDouble() < 0.9 ? 2 : 4;
        }
    }

        /**
     * 更新网格界面显示：根据当前grid数组刷新每个单元格的数字和颜色
     */
    private void updateGridUI() {
        gridPanel.removeAll();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JLabel cell = new JLabel(String.valueOf(grid[i][j]));
                cell.setOpaque(true);
                cell.setBackground(getCellColor(grid[i][j]));
                cell.setHorizontalAlignment(JLabel.CENTER);
                cell.setFont(new Font("微软雅黑", Font.BOLD, 20));
                gridPanel.add(cell);
            }
        }
        scoreLabel.setText("当前分数: " + score + "  历史最高: " + highScore);
        gridPanel.revalidate();
    }

        /**
     * 根据单元格数字获取对应的背景颜色
     * @param value 单元格数字（0,2,4,8,...）
     * @return 匹配的颜色对象
     */
    private Color getCellColor(int value) {
        switch (value) {
            case 0: return new Color(205, 193, 180);
            case 2: return new Color(238, 228, 218);
            case 4: return new Color(237, 224, 200);
            case 8: return new Color(242, 177, 121);
            default: return new Color(245, 149, 99); // 简化颜色逻辑
        }
    }

        /**
     * 获取当前网格中的最大数字（作为当前分数）
     * @return 网格中的最大值
     */
    private int getMaxValue() {
        int max = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] > max) {
                    max = grid[i][j];
                }
            }
        }
        return max;
    }

    // 移动合并逻辑（待完善：具体移动算法）
        /**
     * 处理向上移动合并逻辑（核心算法）
     * 步骤：
     * 1. 提取当前列非空数字并前置
     * 2. 合并相邻相同数字（仅合并一次）
     * 3. 重新排列合并后的数字并更新网格
     * @return 布尔值（true表示发生有效移动，需要生成新数字）
     */
    private boolean moveUp() {
        boolean moved = false;
        for (int col = 0; col < 4; col++) {
            // 步骤1：提取非空数字到临时数组
            int[] column = new int[4];
            int index = 0;
            for (int row = 0; row < 4; row++) {
                if (grid[row][col] != 0) {
                    column[index++] = grid[row][col];
                }
            }
            // 填充剩余位置为0
            for (int row = index; row < 4; row++) column[row] = 0;

            // 步骤2：合并相邻相同数字
            for (int row = 0; row < 3; row++) {
                if (column[row] == column[row+1] && column[row] != 0) {
                    column[row] *= 2; // 合并为双倍值
                    column[row+1] = 0; // 清空原位置
                    moved = true; // 标记发生移动
                }
            }

            // 步骤3：重新排列并更新网格
            int[] newColumn = new int[4];
            index = 0;
            for (int row = 0; row < 4; row++) {
                if (column[row] != 0) newColumn[index++] = column[row];
            }
            // 同步到原网格
            for (int row = 0; row < 4; row++) {
                if (grid[row][col] != newColumn[row]) {
                    grid[row][col] = newColumn[row];
                    moved = true;
                }
            }
        }
        return moved;
    }

        /**
     * 处理向下移动合并逻辑（核心算法）
     * 步骤：
     * 1. 提取当前列非空数字并后置
     * 2. 合并相邻相同数字（仅合并一次）
     * 3. 重新排列合并后的数字并更新网格
     * @return 布尔值（true表示发生有效移动，需要生成新数字）
     */
    private boolean moveDown() {
        boolean moved = false;
        for (int col = 0; col < 4; col++) {
            int[] column = new int[4];
            int index = 3;
            for (int row = 3; row >= 0; row--) {
                if (grid[row][col] != 0) {
                    column[index--] = grid[row][col];
                }
            }
            for (int row = index; row >= 0; row--) column[row] = 0;
            for (int row = 3; row > 0; row--) {
                if (column[row] == column[row-1] && column[row] != 0) {
                    column[row] *= 2;
                    column[row-1] = 0;
                    moved = true;
                }
            }
            int[] newColumn = new int[4];
            index = 3;
            for (int row = 3; row >= 0; row--) {
                if (column[row] != 0) newColumn[index--] = column[row];
            }
            for (int row = 0; row < 4; row++) {
                if (grid[row][col] != newColumn[row]) {
                    grid[row][col] = newColumn[row];
                    moved = true;
                }
            }
        }
        return moved;
    }

        /**
     * 处理向左移动合并逻辑（核心算法）
     * 步骤：
     * 1. 提取当前行非空数字并前置
     * 2. 合并相邻相同数字（仅合并一次）
     * 3. 重新排列合并后的数字并更新网格
     * @return 布尔值（true表示发生有效移动，需要生成新数字）
     */
    private boolean moveLeft() {
        boolean moved = false;
        for (int row = 0; row < 4; row++) {
            int[] newRow = new int[4];
            int index = 0;
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] != 0) {
                    if (index > 0 && newRow[index-1] == grid[row][col]) {
                        newRow[index-1] *= 2;
                        moved = true;
                    } else {
                        newRow[index++] = grid[row][col];
                    }
                }
            }
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] != newRow[col]) {
                    grid[row][col] = newRow[col];
                    moved = true;
                }
            }
        }
        return moved;
    }

        /**
     * 处理向右移动合并逻辑（核心算法）
     * 步骤：
     * 1. 提取当前行非空数字并后置
     * 2. 合并相邻相同数字（仅合并一次）
     * 3. 重新排列合并后的数字并更新网格
     * @return 布尔值（true表示发生有效移动，需要生成新数字）
     */
    private boolean moveRight() {
        boolean moved = false;
        for (int row = 0; row < 4; row++) {
            int[] newRow = new int[4];
            int index = 3;
            for (int col = 3; col >= 0; col--) {
                if (grid[row][col] != 0) {
                    if (index < 3 && newRow[index+1] == grid[row][col]) {
                        newRow[index+1] *= 2;
                        moved = true;
                    } else {
                        newRow[index--] = grid[row][col];
                    }
                }
            }
            for (int col = 0; col < 4; col++) {
                if (grid[row][col] != newRow[col]) {
                    grid[row][col] = newRow[col];
                    moved = true;
                }
            }
        }
        return moved;
    }

        /**
     * 检测游戏是否结束（终局判断逻辑）
     * 结束条件：
     * 1. 网格无空白单元格
     * 2. 所有相邻单元格（上下左右）无相同数字
     */
    private void checkGameOver() {
        boolean canMove = false;

        // 条件1：检查是否存在空白单元格
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == 0) {
                    canMove = true;
                    break;
                }
            }
            if (canMove) break;
        }

        // 条件2：检查是否存在可合并的相邻数字
        if (!canMove) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int current = grid[i][j];
                    if (current == 0) continue;
                    // 检查右侧相邻单元格
                    if (j < 3 && grid[i][j+1] == current) canMove = true;
                    // 检查下侧相邻单元格
                    if (i < 3 && grid[i+1][j] == current) canMove = true;
                    if (canMove) break;
                }
                if (canMove) break;
            }
        }

        // 触发游戏结束逻辑
        if (!canMove) {
            Object[] options = {"重玩", "回主页"};
            int choice = JOptionPane.showOptionDialog(
                    this, // 父窗口
                    "游戏结束！", // 提示信息
                    "提示", // 对话框标题
                    JOptionPane.DEFAULT_OPTION, // 选项类型
                    JOptionPane.INFORMATION_MESSAGE, // 消息类型
                    null, // 图标
                    options, // 按钮选项
                    options[0] // 默认选择
            );
            if (choice == 0) {
                initGame(); // 重新初始化游戏
                updateGridUI(); // 刷新界面
            } else {
                dispose(); // 关闭当前游戏窗口
            }
        }
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * 数独游戏主窗口类
 * 负责数独游戏的生成、界面初始化及交互逻辑
 * 支持简单/困难模式切换，包含新游戏、答案检查等功能
 */
public class SudokuGame extends JFrame {
    /** 主窗口引用，用于返回主页时显示 */
    private MainFrame mainFrame;
    /** 游戏难度等级（1-3为简单，4-5为困难） */
    private int difficulty;
    /** 数独单元格组件数组（9x9网格） */
    private JTextField[][] cells;
    /** 数独完整解数组（存储正确答案） */
    private int[][] solution;

    /**
     * 构造方法：初始化数独游戏窗口
     * @param mainFrame 主窗口引用
     * @param difficulty 游戏难度等级
     */
    public SudokuGame(MainFrame mainFrame, int difficulty) {
        this.mainFrame = mainFrame;
        this.difficulty = difficulty;
        setTitle("数独 - " + (difficulty <= 3 ? "简单" : "困难"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        setLocationRelativeTo(mainFrame);
        initGame();
        initUI();
    }

    /**
     * 初始化游戏核心逻辑：生成数独解
     * 使用回溯算法生成有效数独终盘
     */
    private void initGame() {
        // 使用回溯法生成有效数独
        solution = new int[9][9];
        Random random = new Random();
        generateSudoku(0, 0, random);
    }

    /**
     * 回溯算法生成数独解
     * @param row 当前处理行（0-8）
     * @param col 当前处理列（0-8）
     * @param random 随机数生成器
     * @return 生成成功返回true，否则false
     */
    private boolean generateSudoku(int row, int col, Random random) {
        if (row == 9) {
            return true;
        }
        if (col == 9) {
            return generateSudoku(row + 1, 0, random);
        }
        // 生成1-9的随机排列
        int[] nums = {1,2,3,4,5,6,7,8,9};
        for (int i = 0; i < 9; i++) {
            int temp = nums[i];
            int r = random.nextInt(9 - i) + i;
            nums[i] = nums[r];
            nums[r] = temp;
        }
        for (int num : nums) {
            if (isValid(row, col, num)) {
                solution[row][col] = num;
                if (generateSudoku(row, col + 1, random)) {
                    return true;
                }
                solution[row][col] = 0;
            }
        }
        return false;
    }

    /**
     * 检查当前位置是否可以放置指定数字
     * @param row 行号
     * @param col 列号
     * @param num 待放置数字（1-9）
     * @return 合法返回true，否则false
     */
    private boolean isValid(int row, int col, int num) {
        return isValidRow(row, num) && isValidCol(col, num) && isValidSubgrid(row, col, num);
    }

    /**
     * 检查当前行是否存在重复数字
     * @param row 行号
     * @param num 待检查数字
     * @return 无重复返回true，否则false
     */
    private boolean isValidRow(int row, int num) {
        for (int c = 0; c < 9; c++) {
            if (solution[row][c] == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查当前列是否存在重复数字
     * @param col 列号
     * @param num 待检查数字
     * @return 无重复返回true，否则false
     */
    private boolean isValidCol(int col, int num) {
        for (int r = 0; r < 9; r++) {
            if (solution[r][col] == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查当前3x3子格是否存在重复数字
     * @param row 行号
     * @param col 列号
     * @param num 待检查数字
     * @return 无重复返回true，否则false
     */
    private boolean isValidSubgrid(int row, int col, int num) {
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (solution[r][c] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 初始化游戏界面
     * 包含功能按钮面板和数独网格布局
     * 处理新游戏按钮点击事件和单元格输入限制
     */
    private void initUI() {
        // 功能按钮面板
        JPanel buttonPanel = new JPanel();
        JButton newGameBtn = new JButton("新游戏");
        newGameBtn.addActionListener(e -> {
    initGame();
    // 重置所有单元格
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            cells[i][j].setText("");
            cells[i][j].setEditable(true);
            cells[i][j].setBackground(Color.WHITE);
        }
    }
    // 重新预填数字
    int preFill = difficulty <= 3 ? 45 : 25;
    Random random = new Random();
    for (int k = 0; k < preFill; k++) {
        int i = random.nextInt(9);
        int j = random.nextInt(9);
        cells[i][j].setText(String.valueOf(solution[i][j]));
        cells[i][j].setEditable(false);
        cells[i][j].setBackground(new Color(230, 230, 230));
    }
});
        JButton checkBtn = new JButton("检查答案");
        checkBtn.addActionListener(this::checkAnswer);
        JButton backBtn = new JButton("返回主页");
        backBtn.addActionListener(e -> {
            dispose();
            mainFrame.setVisible(true);
        });
        buttonPanel.add(newGameBtn);
        buttonPanel.add(checkBtn);
        buttonPanel.add(backBtn);

        // 数独网格
        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 2, 2));
        gridPanel.setBackground(Color.BLACK);
        cells = new JTextField[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(new Font("微软雅黑", Font.BOLD, 16));
                cells[i][j].setBorder(getSubgridBorder(i, j));
                int finalI = i, finalJ = j;
                cells[i][j].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!(c >= '1' && c <= '9')) {
                            e.consume();
                        } else {
                            cells[finalI][finalJ].setText(String.valueOf(c));
                            e.consume(); // 消耗有效事件以防止重复输入
                        }
                    }
                });
                gridPanel.add(cells[i][j]);
            }
        }

        // 预填数字（简单模式>40个，困难≤30个）
        int preFill = difficulty <= 3 ? 45 : 25;
        Random random = new Random();
        for (int k = 0; k < preFill; k++) {
            int i = random.nextInt(9);
            int j = random.nextInt(9);
            cells[i][j].setText(String.valueOf(solution[i][j]));
            cells[i][j].setEditable(false);
            cells[i][j].setBackground(new Color(230, 230, 230));
        }

        add(buttonPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
    }

    /**
     * 获取3x3子格边框样式
     * 子格底部/右侧使用加粗边框（2像素），内部使用细边框（1像素）
     * @param i 行号
     * @param j 列号
     * @return 边框对象
     */
    private Border getSubgridBorder(int i, int j) {
        // 3x3子格加粗边框（2像素），普通格子细边框（1像素）
        // 正确判断3x3子格外边框（子格底部/右侧加粗）
        // 判断是否为3x3子格的底部或右侧边界（索引从0开始）
        boolean isSubgridBottom = (i % 3 == 2); // 子格底部（第2行）
        boolean isSubgridRight = (j % 3 == 2);  // 子格右侧（第2列）
        if (isSubgridBottom || isSubgridRight) {
            return new LineBorder(Color.BLACK, 2);
        }
        // 子格内部右/下边界细边框（1像素）
        else {
            return new LineBorder(Color.LIGHT_GRAY, 1);
        }
    }

    /**
     * 检查用户答案正确性
     * 错误数字标记为红色（当前仅对比正确解，待完善行/列/宫冲突检查）
     * @param e 事件对象
     */
    private void checkAnswer(ActionEvent e) {
        // 错误提示（待完善：检查行/列/宫冲突）
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setForeground(Color.BLACK);
                String text = cells[i][j].getText();
                if (!text.isEmpty() && !text.equals(String.valueOf(solution[i][j]))) {
                    cells[i][j].setForeground(Color.RED);
                }
            }
        }
    }
}
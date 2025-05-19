import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 小游戏合集主窗口类
 * 负责创建程序主界面，提供各游戏入口按钮及统一的窗口管理功能
 */
public class MainFrame extends JFrame {
        /**
     * 主窗口构造方法
     * 初始化窗口基本属性并调用界面初始化方法
     */
    public MainFrame() {
        setTitle("小游戏合集");          // 设置窗口标题
        setSize(1280, 960);              // 设置初始窗口大小
        setMinimumSize(new Dimension(800, 600));  // 设置最小窗口尺寸
        setLocationRelativeTo(null);     // 窗口居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 设置关闭行为

        initUI();  // 调用界面初始化方法
    }

        /**
     * 界面初始化方法
     * 负责创建按钮面板、配置按钮样式、绑定事件监听器及设置窗口布局
     */
    private void initUI() {
        // 创建按钮容器面板（垂直布局）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        // 设置复合边框：40像素内边距 + 2像素浅灰圆角边框
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(40, 40, 40, 40),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true)));
        buttonPanel.setBackground(new Color(248, 248, 255)); // 设置面板背景色（浅蓝）

        JButton snakeBtn = new JButton("贪吃蛇");
        JButton minesweeperBtn = new JButton("扫雷");
        JButton game2048Btn = new JButton("2048");
        JButton sudokuBtn = new JButton("数独");
        JButton exitBtn = new JButton("退出程序");

                // 统一按钮尺寸和字体配置
        Dimension btnSize = new Dimension(220, 60);  // 按钮固定尺寸（宽220，高60）
        Font btnFont = new Font("微软雅黑", Font.BOLD, 16);  // 按钮字体（微软雅黑，加粗，16号）

        // 遍历所有游戏按钮，统一设置样式
        for(JButton btn : new JButton[]{snakeBtn, minesweeperBtn, game2048Btn, sudokuBtn, exitBtn}){
            // 设置按钮尺寸（优先/最小/最大尺寸保持一致，确保统一）
            btn.setPreferredSize(btnSize);
            btn.setMinimumSize(btnSize);
            btn.setMaximumSize(btnSize);
            btn.setFont(btnFont);  // 设置按钮字体
            btn.setForeground(new Color(0, 0, 139));  // 深蓝色文字
            btn.setBackground(new Color(240, 248, 255));  // 更浅蓝背景色
            // 设置复合边框：2像素天空蓝圆角边框 + 5像素内边距
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(135, 206, 250), 2, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            btn.setFocusPainted(false);  // 移除焦点框（提升美观度）
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);  // 按钮在面板中居中对齐

            // 添加鼠标悬停效果（背景色变化）
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(220, 230, 255));  // 悬停时背景色变亮
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(240, 248, 255));  // 鼠标离开后恢复原背景色
                }
            });
        }

                // 贪吃蛇按钮点击事件：显示难度选择对话框并跳转游戏窗口
        snakeBtn.addActionListener(e -> {
            String[] difficulties = {"初级", "中级", "高级"};  // 可选难度列表
            // 显示输入对话框（问题类型，默认选择第一个难度）
            String selected = (String) JOptionPane.showInputDialog(this, "选择难度:", "贪吃蛇难度",
                    JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);
            if (selected != null) {  // 用户确认选择时
                setVisible(false);  // 隐藏当前主窗口
                new SnakeGame(this, selected).setVisible(true);  // 创建并显示贪吃蛇游戏窗口
            }
        });
        minesweeperBtn.addActionListener(this::onMinesweeperClick);
        game2048Btn.addActionListener(this::on2048Click);
        sudokuBtn.addActionListener(this::onSudokuClick);
        exitBtn.addActionListener(e -> System.exit(0));

        // 添加垂直间隔
        // 调整垂直间距为25像素
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(snakeBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(minesweeperBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(game2048Btn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(sudokuBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        buttonPanel.add(exitBtn);
        buttonPanel.add(Box.createVerticalGlue());
        // 使用自定义渐变面板作为内容面板
        class GradientPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = getHeight();
                GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 248, 255),
                        width, height, new Color(224, 255, 255));
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        }
        getContentPane().add(new GradientPanel(), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.CENTER);
    }

        /**
     * 扫雷游戏入口点击事件处理
     * @param e 动作事件对象
     */
    private void onMinesweeperClick(ActionEvent e) {
        // 显示扫雷难度选择对话框
        String[] difficulties = {"初级", "中级", "高级"};  // 可选难度列表
        String selected = (String) JOptionPane.showInputDialog(this, "选择难度:", "扫雷难度",
                JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);  // 获取用户选择
        if (selected != null) {  // 用户确认选择时
            setVisible(false);  // 隐藏当前主窗口
            new MinesweeperGame(this, selected).setVisible(true);  // 创建并显示扫雷游戏窗口
        }
    }

        /**
     * 2048游戏入口点击事件处理
     * @param e 动作事件对象
     */
    private void on2048Click(ActionEvent e) {
        setVisible(false);  // 隐藏当前主窗口
        new Game2048(this).setVisible(true);  // 创建并显示2048游戏窗口
    }

        /**
     * 数独游戏入口点击事件处理
     * @param e 动作事件对象
     */
    private void onSudokuClick(ActionEvent e) {
        // 显示数独难度选择对话框（1-6级整数选择）
        Integer[] difficulties = {1, 2, 3, 4, 5, 6};  // 可选难度列表（1-6级）
        Integer selected = (Integer) JOptionPane.showInputDialog(this, "选择难度(1-6):", "数独难度",
                JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);  // 获取用户选择
        if (selected != null) {  // 用户确认选择时
            setVisible(false);  // 隐藏当前主窗口
            new SudokuGame(this, selected).setVisible(true);  // 创建并显示数独游戏窗口
        }
    }

        /**
     * 程序入口方法
     * 使用SwingUtilities保证GUI在事件分派线程中初始化
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 在事件分派线程中安全地创建并显示主窗口
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
@echo off
cd /d "c:\Users\14406\Desktop\game"
javac MainFrame.java Game2048.java MinesweeperGame.java SudokuGame.java SnakeGame.java
if errorlevel 1 (
    echo 编译失败，请检查Java文件是否有错误。
    pause
    exit
)
start java MainFrame
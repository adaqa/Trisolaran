# Trisolaran (Three-Body Simulation Mod)

A Minecraft mod that brings the chaotic world of "The Three-Body Problem" into the game. It features a custom physics engine simulating three suns and a planet, creating unpredictable day/night cycles and stunning visual effects.

## Features

- **Three-Body Physics Simulation**: Experience the chaotic dance of three suns governed by Newtonian gravity.
- **Dynamic Sky Rendering**: Realistic sky color, fog, and sun positions based on the physics simulation.
- **Chaotic Era**: No fixed day/night cycle. Prepare for long nights or scorching triple-sun days.
- **Celestial Locator (天体定位器)**: A powerful tool to monitor and control the star system.
    - View real-time positions and velocities of the suns.
    - Adjust planet mass and orientation.
    - Randomize the system state for new scenarios.
    - Pause/Resume the simulation.

## Usage

1.  **Obtain the Celestial Locator**: Find it in the Creative Inventory under the "Trisolaran" tab.
2.  **Open the Control Panel**: Right-click with the item in hand.
3.  **Control the System**:
    - Use the GUI to modify the mass of the planet.
    - Adjust the planet's orientation vector to change how the suns appear in the sky.
    - Click "Random" to reset the system to a new random state.

## Installation

1.  Install **Minecraft 1.21** or later.
2.  Install **Fabric Loader** and **Fabric API**.
3.  Download the latest release of `Trisolaran-mod.jar`.
4.  Place the `.jar` file into your `mods` folder.
5.  Launch the game.

## Configuration

The mod currently uses a simplified simulation model optimized for single-player. The physics engine runs on the server side (integrated server in single-player) and syncs visual data to the client.

## Building from Source

```bash
./gradlew build
```

The compiled mod jar will be in `build/libs/`.

---

# Trisolaran (三体模拟模组)

这是一个基于《三体》设定的 Minecraft 模组，在游戏中引入了一个拥有三颗恒星的混沌星系。模组通过自定义物理引擎模拟三体运动，呈现出不可预测的昼夜交替和震撼的天空视觉效果。

## 主要功能

- **三体物理模拟**：基于牛顿引力定律的实时三体运动模拟。
- **动态天空渲染**：根据物理模拟结果实时渲染天空颜色、雾气和太阳位置。
- **乱纪元**：告别固定的昼夜循环，迎接漫长的黑夜或三日凌空的酷热。
- **天体定位器**：用于观测和控制星系的工具。
    - 查看恒星的实时位置和速度。
    - 调整行星质量和自转轴朝向。
    - 随机重置系统状态。
    - 暂停/恢复模拟。

## 使用方法

1.  **获取天体定位器**：在创造模式物品栏的“Trisolaran”标签页中找到。
2.  **打开控制面板**：手持物品右键点击。
3.  **控制系统**：
    - 在 GUI 中修改行星质量。
    - 调整行星朝向向量，改变太阳在空中的运行轨迹。
    - 点击“随机”按钮重置系统状态。

## 安装

1.  安装 **Minecraft 1.21** 或更高版本。
2.  安装 **Fabric Loader** 和 **Fabric API**。
3.  下载最新版本的模组文件放入 `mods` 文件夹。

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/` 目录下。

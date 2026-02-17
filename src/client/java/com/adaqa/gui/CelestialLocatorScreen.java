package com.adaqa.gui;

import com.adaqa.simulation.ThreeBodySystem;
import com.adaqa.simulation.TrisolaranSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CelestialLocatorScreen extends Screen {

    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    
    // 输入框列表
    private List<TextFieldWidget> positionInputs = new ArrayList<>();
    private List<TextFieldWidget> velocityInputs = new ArrayList<>();
    private List<TextFieldWidget> massInputs = new ArrayList<>();
    
    // 行星参数输入
    private TextFieldWidget planetPosInput;
    private TextFieldWidget planetVelInput;

    public CelestialLocatorScreen() {
        super(Text.translatable("item.trisolaran.celestial_locator"));
    }

    @Override
    protected void init() {
        super.init();
        
        int rightPanelX = this.width - 120;
        int inputWidth = 110;
        
        // 1. 重置按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            TrisolaranSystem.getSystem().reset();
            updateInputsFromSystem();
            // 重置视图
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
        }).dimensions(10, this.height - 30, 50, 20).build());
        
        // 1.5 随机按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Random"), button -> {
            TrisolaranSystem.getSystem().randomize();
            updateInputsFromSystem();
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
        }).dimensions(65, this.height - 30, 60, 20).build());
        
        // 2. 应用按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply Changes"), button -> {
            applyChanges();
        }).dimensions(rightPanelX, this.height - 30, inputWidth, 20).build());

        // 3. 初始化输入框 (针对3个太阳 + 1个行星)
        positionInputs.clear();
        velocityInputs.clear();
        massInputs.clear();

        int startY = 30;
        int gapY = 55;
        
        for (int i = 0; i < 3; i++) {
            // 位置输入 (x, y)
            TextFieldWidget posInput = new TextFieldWidget(this.textRenderer, rightPanelX, startY + i * gapY, inputWidth, 14, Text.literal("Pos " + i));
            this.addDrawableChild(posInput);
            positionInputs.add(posInput);

            // 速度输入
            TextFieldWidget velInput = new TextFieldWidget(this.textRenderer, rightPanelX, startY + i * gapY + 16, inputWidth, 14, Text.literal("Vel " + i));
            this.addDrawableChild(velInput);
            velocityInputs.add(velInput);
            
            // 质量输入
            TextFieldWidget massInput = new TextFieldWidget(this.textRenderer, rightPanelX, startY + i * gapY + 32, inputWidth, 14, Text.literal("Mass " + i));
            this.addDrawableChild(massInput);
            massInputs.add(massInput);
        }
        
        // 行星输入 (Body index 3)
        int planetY = startY + 3 * gapY + 10;
        planetPosInput = new TextFieldWidget(this.textRenderer, rightPanelX, planetY, inputWidth, 14, Text.literal("Planet Pos"));
        this.addDrawableChild(planetPosInput);
        
        planetVelInput = new TextFieldWidget(this.textRenderer, rightPanelX, planetY + 16, inputWidth, 14, Text.literal("Planet Vel"));
        this.addDrawableChild(planetVelInput);
        
        // 行星质量输入
        TextFieldWidget planetMassInput = new TextFieldWidget(this.textRenderer, rightPanelX, planetY + 32, inputWidth, 14, Text.literal("Planet Mass"));
        this.addDrawableChild(planetMassInput);
        massInputs.add(planetMassInput); // 复用massInputs列表来管理更新逻辑，注意索引
        
        updateInputsFromSystem();
    }
    
    private void updateInputsFromSystem() {
        List<ThreeBodySystem.Body> bodies = TrisolaranSystem.getSystem().getBodies();
        
        // 更新太阳 (indices 0, 1, 2)
        for (int i = 0; i < 3; i++) {
            if (i >= bodies.size()) break;
            ThreeBodySystem.Body body = bodies.get(i);
            
            if (!positionInputs.get(i).isFocused())
                positionInputs.get(i).setText(String.format("%.1f, %.1f", body.position.x, body.position.y));
            
            if (!velocityInputs.get(i).isFocused())
                velocityInputs.get(i).setText(String.format("%.2f, %.2f", body.velocity.x, body.velocity.y));
                
            if (!massInputs.get(i).isFocused())
                massInputs.get(i).setText(String.format("%.0f", body.mass));
        }
        
        // 更新行星 (假设索引为3)
        if (bodies.size() > 3) {
            ThreeBodySystem.Body planet = bodies.get(3);
            if (!planetPosInput.isFocused())
                planetPosInput.setText(String.format("%.1f, %.1f", planet.position.x, planet.position.y));
            if (!planetVelInput.isFocused())
                planetVelInput.setText(String.format("%.2f, %.2f", planet.velocity.x, planet.velocity.y));
            
            // 行星质量 (massInputs的第4个元素，index 3)
            if (massInputs.size() > 3 && !massInputs.get(3).isFocused()) {
                massInputs.get(3).setText(String.format("%.1f", planet.mass));
            }
        }
    }
    
    private void applyChanges() {
        try {
            ThreeBodySystem system = TrisolaranSystem.getSystem();
            List<ThreeBodySystem.Body> bodies = system.getBodies();
            
            // 应用太阳参数
            for (int i = 0; i < 3; i++) {
                if (i >= bodies.size()) break;
                ThreeBodySystem.Body body = bodies.get(i);
                
                // Pos
                String[] posParts = positionInputs.get(i).getText().split(",");
                if (posParts.length >= 2) {
                    double x = Double.parseDouble(posParts[0].trim());
                    double y = Double.parseDouble(posParts[1].trim());
                    body.position = new Vec3d(x, y, body.position.z);
                }
                
                // Vel
                String[] velParts = velocityInputs.get(i).getText().split(",");
                if (velParts.length >= 2) {
                    double vx = Double.parseDouble(velParts[0].trim());
                    double vy = Double.parseDouble(velParts[1].trim());
                    body.velocity = new Vec3d(vx, vy, body.velocity.z);
                }
                
                // Mass
                body.mass = Double.parseDouble(massInputs.get(i).getText().trim());
            }
            
            // 应用行星参数
            if (bodies.size() > 3) {
                ThreeBodySystem.Body planet = bodies.get(3);
                
                String[] pPosParts = planetPosInput.getText().split(",");
                if (pPosParts.length >= 2) {
                    planet.position = new Vec3d(
                        Double.parseDouble(pPosParts[0].trim()),
                        Double.parseDouble(pPosParts[1].trim()),
                        planet.position.z
                    );
                }
                
                String[] pVelParts = planetVelInput.getText().split(",");
                if (pVelParts.length >= 2) {
                    planet.velocity = new Vec3d(
                        Double.parseDouble(pVelParts[0].trim()),
                        Double.parseDouble(pVelParts[1].trim()),
                        planet.velocity.z
                    );
                }
                
                // 应用行星质量
                if (massInputs.size() > 3) {
                    planet.mass = Double.parseDouble(massInputs.get(3).getText().trim());
                }
            }
            
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 覆盖此方法并留空，彻底禁用默认的背景渲染逻辑（包括模糊和渐变）
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. 手动绘制纯黑背景，不使用任何模糊效果
        context.fill(0, 0, this.width, this.height, 0xFF000000);
        
        // 2. 绘制三体系统
        renderSystem(context);
        
        // 3. 绘制UI组件 (输入框、按钮等)
        super.render(context, mouseX, mouseY, delta);
        
        // 4. 绘制标签 (右侧面板)
        renderLabels(context);
        
        // 5. 绘制标题和提示
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFF55FF);
        context.drawText(this.textRenderer, "Esc to close", 10, 10, 0xFFFFFF, false);
        
        // 6. 显示状态信息
        String statusText = String.format("Scale: %.2f Offset: (%.0f, %.0f)", scale, offsetX, offsetY);
        context.drawText(this.textRenderer, statusText, 10, 25, 0xAAAAAA, false);
        context.drawText(this.textRenderer, "Drag to move, Scroll to zoom", 10, this.height - 15, 0x888888, false);
    }

    private void renderSystem(DrawContext context) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ThreeBodySystem system = TrisolaranSystem.getSystem();
        List<ThreeBodySystem.Body> bodies = system.getBodies();
        
        // 计算屏幕中心点 (考虑偏移)
        int screenCenterX = centerX + (int)Math.round(offsetX);
        int screenCenterY = centerY + (int)Math.round(offsetY);

        // 绘制中心十字线
        context.fill(screenCenterX - 10, screenCenterY, screenCenterX + 11, screenCenterY + 1, 0xFF888888);
        context.fill(screenCenterX, screenCenterY - 10, screenCenterX + 1, screenCenterY + 11, 0xFF888888);

        // 颜色定义
        int[] sunColors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF55FFFF}; 
        
        for (int i = 0; i < bodies.size(); i++) {
            ThreeBodySystem.Body body = bodies.get(i);
            
            // 映射：Screen = Center + World * Scale
            // 关键：每一步都使用 double 计算，只在最后转换为 int，确保精度
            // 并使用 Math.round 进行四舍五入，避免 int 强转导致的向下取整偏差
            int screenX = screenCenterX + (int)Math.round(body.position.x * scale);
            int screenY = screenCenterY + (int)Math.round(body.position.y * scale);

            // 视锥剔除
            if (screenX < -20 || screenX > this.width + 20 || screenY < -20 || screenY > this.height + 20) {
                continue;
            }

            // 绘制天体 (4x4 像素方块)
            int size = 2; 
            int color = (i < 3) ? sunColors[i] : sunColors[3];
            context.fill(screenX - size, screenY - size, screenX + size + 1, screenY + size + 1, color);
            
            // 如果是行星，绘制自转方向 (Orientation)
            if (i == 3) {
                // orientation 代表"天顶"或"北方"，这里我们在2D平面上投影它的 X/Z 分量
                // 注意：simulation 中的 orientation 是 Vec3d(x, y, z)
                // 在 GUI 上，我们是从上往下看 (Top-Down View)，所以对应 x, z 坐标
                // 但我们的渲染映射是: screenX = worldX, screenY = worldY (注意这里其实是 Y 轴作为纵轴)
                // 等等，ThreeBodySystem 里是 (x, y, z)，通常 y 是垂直高度，x, z 是水平面
                // 我们的 GUI 渲染逻辑一直用的是 body.position.y 作为屏幕 Y 轴
                // 这意味着我们在看 XY 平面 (侧视图？) 或者模拟是在 2D 平面 (z=0) 进行的？
                // 检查 ThreeBodySystem 初始化：恒星分布在 (x, y, 0) 和 (x, y, 20)
                // 所以主要运动平面是 XY 平面。
                
                // 那么 Orientation (自转轴) 如果默认是 (0, 1, 0) (Y轴)，在 XY 平面上就是一个点。
                // 如果我们想看"朝向"，应该是看 orientation 在 XY 平面上的投影，或者
                // 我们之前定义的"观察方向" (World Forward)
                
                // 假设 orientation 向量就是我们在 GUI 上要画的箭头方向
                Vec3d dir = body.orientation;
                if (dir.lengthSquared() > 0.0001) {
                    // 投影到 XY 平面并归一化
                    double len = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
                    if (len > 0.0001) {
                        int endX = screenX + (int)(dir.x / len * 10);
                        int endY = screenY + (int)(dir.y / len * 10);
                        
                        // 绘制白色线条表示朝向
                        // 简易画线：用多个点模拟
                        int steps = 5;
                        for(int s=0; s<=steps; s++) {
                            double t = (double)s / steps;
                            int px = screenX + (int)((endX - screenX) * t);
                            int py = screenY + (int)((endY - screenY) * t);
                            context.fill(px, py, px+1, py+1, 0xFFFFFFFF);
                        }
                        // 箭头头部
                        context.fill(endX - 1, endY - 1, endX + 1, endY + 1, 0xFFFF0000); // 红色头部
                    }
                }
            }
            
            // 绘制标签 (不带阴影)
            String label = (i == 3) ? "Planet" : "S" + (i + 1);
            context.drawText(this.textRenderer, label, screenX + 6, screenY - 4, color, false);
        }
    }
    
    private void renderLabels(DrawContext context) {
        int rightPanelX = this.width - 120;
        int startY = 30;
        int gapY = 55;
        
        for (int i = 0; i < 3; i++) {
            int labelY = startY + i * gapY;
            context.drawText(this.textRenderer, "Sun " + (i+1), rightPanelX - 35, labelY + 4, 0xFFFFFF, false);
        }
        
        // 行星标签
        int planetY = startY + 3 * gapY + 10;
        context.drawText(this.textRenderer, "Planet", rightPanelX - 35, planetY + 4, 0xAAAAFF, false);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            scale *= 1.1;
        } else if (verticalAmount < 0) {
            scale *= 0.9;
        }
        // 限制最小最大缩放
        if (scale < 0.1) scale = 0.1;
        if (scale > 10.0) scale = 10.0;
        
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) { // 左键拖动
             // 检查是否在输入框区域，避免冲突
             if (mouseX < this.width - 120) {
                 offsetX += deltaX;
                 offsetY += deltaY;
                 return true;
             }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void tick() {
        super.tick();
        // 实时更新输入框中的数据，如果用户没有正在编辑
        if (!isAnyInputFocused()) {
            updateInputsFromSystem();
        }
    }
    
    private boolean isAnyInputFocused() {
        for (TextFieldWidget w : positionInputs) if (w.isFocused()) return true;
        for (TextFieldWidget w : velocityInputs) if (w.isFocused()) return true;
        for (TextFieldWidget w : massInputs) if (w.isFocused()) return true;
        if (planetPosInput != null && planetPosInput.isFocused()) return true;
        if (planetVelInput != null && planetVelInput.isFocused()) return true;
        return false;
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
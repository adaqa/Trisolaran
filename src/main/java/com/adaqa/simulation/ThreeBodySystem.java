package com.adaqa.simulation;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class ThreeBodySystem {
    public static class Body {
        public Vec3d position;
        public Vec3d velocity;
        public double mass;
        public Vec3d orientation; // 行星朝向 (自转轴/观察方向)
        public Vec3d angularVelocity; // 自转角速度

        public Body(Vec3d position, Vec3d velocity, double mass) {
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
            this.orientation = new Vec3d(0, 1, 0); // 默认朝上 (Y轴)
            this.angularVelocity = new Vec3d(0, 0, 0);
        }
    }

    private final List<Body> bodies = new ArrayList<>();
    private final double G = 10.0; // 引力常数

    public ThreeBodySystem() {
        reset();
    }

    public void reset() {
        bodies.clear();
        // 初始化三个太阳 (等边三角形配置)
        // 恒星1
        bodies.add(new Body(new Vec3d(100, 0, 0), new Vec3d(0, 5, 0), 1000.0));
        // 恒星2
        bodies.add(new Body(new Vec3d(-50, 86.6, 0), new Vec3d(-4.33, -2.5, 0), 1000.0));
        // 恒星3
        bodies.add(new Body(new Vec3d(-50, -86.6, 20), new Vec3d(4.33, -2.5, 0), 1000.0));
        
        // 初始化行星 (质量很小，几乎不影响恒星，但受恒星影响)
        // 初始位置在某个安全区域
        bodies.add(new Body(new Vec3d(0, 0, 0), new Vec3d(0, 0, 0), 0.1));
    }

    public void randomize() {
        bodies.clear();
        
        // 随机参数范围
        double posRange = 150.0;
        double velRange = 5.0; // 适当增加初始速度，避免直接塌缩
        double massMin = 800.0;
        double massMax = 1200.0;
        
        // 生成三个恒星
        for (int i = 0; i < 3; i++) {
            double x = (Math.random() - 0.5) * 2 * posRange;
            double y = (Math.random() - 0.5) * 2 * posRange;
            double z = (Math.random() - 0.5) * 2 * (posRange / 5.0); // Z轴稍微扁平一些，模拟盘状结构
            
            double vx = (Math.random() - 0.5) * 2 * velRange;
            double vy = (Math.random() - 0.5) * 2 * velRange;
            double vz = (Math.random() - 0.5) * 2 * (velRange / 5.0);
            
            double mass = massMin + Math.random() * (massMax - massMin);
            
            bodies.add(new Body(new Vec3d(x, y, z), new Vec3d(vx, vy, vz), mass));
        }
        
        // 修正动量守恒，确保系统质心不动
        Vec3d totalMomentum = Vec3d.ZERO;
        double totalMass = 0;
        for (Body b : bodies) {
            totalMomentum = totalMomentum.add(b.velocity.multiply(b.mass));
            totalMass += b.mass;
        }
        Vec3d centerVelocity = totalMomentum.multiply(1.0 / totalMass);
        for (Body b : bodies) {
            b.velocity = b.velocity.subtract(centerVelocity);
        }
        
        // 修正位置质心到原点
        Vec3d centerOfMass = Vec3d.ZERO;
        for (Body b : bodies) {
            centerOfMass = centerOfMass.add(b.position.multiply(b.mass));
        }
        centerOfMass = centerOfMass.multiply(1.0 / totalMass);
        for (Body b : bodies) {
            b.position = b.position.subtract(centerOfMass);
        }

        // 添加行星
        // 为了避免行星一出生就撞上恒星或被甩飞，尝试把它放在一个相对"空旷"的地方
        // 比如：放在离所有恒星至少有一定距离的位置
        Vec3d planetPos = Vec3d.ZERO;
        boolean safe = false;
        int attempts = 0;
        while (!safe && attempts < 100) {
            double px = (Math.random() - 0.5) * 2 * (posRange * 0.8);
            double py = (Math.random() - 0.5) * 2 * (posRange * 0.8);
            double pz = (Math.random() - 0.5) * 2 * (posRange * 0.1);
            planetPos = new Vec3d(px, py, pz);
            
            safe = true;
            for (Body star : bodies) {
                if (planetPos.distanceTo(star.position) < 50.0) {
                    safe = false;
                    break;
                }
            }
            attempts++;
        }
        
        // 行星速度：赋予一个切向速度，使其绕着当前的质心旋转
        // 粗略估算：v = sqrt(GM/r)
        // M 取最近恒星的质量，r 取距离
        // 这只是一个启发式初始化
        Body nearestStar = bodies.get(0);
        double minDist = Double.MAX_VALUE;
        for (Body star : bodies) {
            double d = planetPos.distanceTo(star.position);
            if (d < minDist) {
                minDist = d;
                nearestStar = star;
            }
        }
        
        // 计算切向方向
        Vec3d rVec = planetPos.subtract(nearestStar.position);
        Vec3d tangent = new Vec3d(-rVec.y, rVec.x, 0).normalize(); // 简单的XY平面切向
        double orbitalSpeed = Math.sqrt(G * nearestStar.mass / minDist);
        
        // 加上恒星本身的速度
        Vec3d planetVel = nearestStar.velocity.add(tangent.multiply(orbitalSpeed));

        // 随机行星质量 (0.1 ~ 50.0)
        double planetMass = 0.1 + Math.random() * 49.9;
        Body planet = new Body(planetPos, planetVel, planetMass);
        
        // 随机自转轴 (简单起见，主要绕Y轴，带一点倾角)
        // 这里的 orientation 实际上代表"行星地表的正北方向"或者"天顶方向"在惯性系中的指向
        // 为了简化计算，我们引入一个新的向量 forwardDirection 代表"观察者的正前方"
        // 暂时我们只给它一个随机的初始自转角速度
        planet.angularVelocity = new Vec3d(0, (Math.random() - 0.5) * 0.5, 0); // 主要是Y轴自转
        
        // 初始朝向 (X轴正向)
        planet.orientation = new Vec3d(1, 0, 0);

        bodies.add(planet);
    }

    public void update(double dt) {
        int n = bodies.size();
        Vec3d[] forces = new Vec3d[n];

        // 1. 计算引力
        for (int i = 0; i < n; i++) {
            forces[i] = Vec3d.ZERO;
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                Body b1 = bodies.get(i);
                Body b2 = bodies.get(j);
                
                // 行星对恒星的引力可以忽略，优化性能 (可选，但为了通用性暂不优化)
                // if (b1.mass < 1.0 && b2.mass > 100.0) { ... }
                
                Vec3d r = b2.position.subtract(b1.position);
                double dist = r.length();
                if (dist < 10.0) dist = 10.0; // 软化核心，防止无限引力
                
                double f = (G * b1.mass * b2.mass) / (dist * dist);
                forces[i] = forces[i].add(r.normalize().multiply(f));
            }
        }

        // 2. 更新位置和速度
        for (int i = 0; i < n; i++) {
            Body b = bodies.get(i);
            Vec3d acceleration = forces[i].multiply(1.0 / b.mass);
            b.velocity = b.velocity.add(acceleration.multiply(dt));
            b.position = b.position.add(b.velocity.multiply(dt));
            
            // 2.5 更新自转
            // orientation 在这里代表"观察者面向的方向" (World Forward)
            // 简单的绕 Y 轴旋转模拟自转
            if (b.mass < 100.0) { 
                double angle = b.angularVelocity.y * dt;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                
                // 旋转 orientation 向量 (绕 Y 轴)
                double newX = b.orientation.x * cos - b.orientation.z * sin;
                double newZ = b.orientation.x * sin + b.orientation.z * cos;
                b.orientation = new Vec3d(newX, b.orientation.y, newZ).normalize();
            }

            // 边界约束 (仅针对飞得太远的物体)
            if (b.position.length() > 2000) {
                 b.velocity = b.velocity.multiply(-0.1).add(b.position.multiply(-0.001));
            }
        }
    }

    public Body getBody(int index) {
        if (index >= 0 && index < bodies.size()) {
            return bodies.get(index);
        }
        return null; // Handle appropriately
    }
    
    public List<Body> getBodies() {
        return bodies;
    }
}

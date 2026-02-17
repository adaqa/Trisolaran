package com.adaqa.simulation;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class ThreeBodySystem {
    public static class Body {
        public Vec3d position;
        public Vec3d velocity;
        public double mass;

        public Body(Vec3d position, Vec3d velocity, double mass) {
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
        }
    }

    private final List<Body> bodies = new ArrayList<>();
    private final double G = 10.0; // 引力常数

    public ThreeBodySystem() {
        reset();
    }

    public void reset() {
        bodies.clear();
        // 初始化三个太阳
        bodies.add(new Body(new Vec3d(100, 0, 0), new Vec3d(0, 0.5, 0.2), 1000.0));
        bodies.add(new Body(new Vec3d(-50, 86.6, 0), new Vec3d(-0.5, -0.2, 0), 1000.0));
        bodies.add(new Body(new Vec3d(-50, -86.6, 20), new Vec3d(0.2, -0.2, -0.1), 1000.0));
        
        // 初始化行星 (质量很小，几乎不影响恒星，但受恒星影响)
        // 初始位置在某个安全区域
        bodies.add(new Body(new Vec3d(0, 0, 0), new Vec3d(0, 0, 0), 0.1));
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

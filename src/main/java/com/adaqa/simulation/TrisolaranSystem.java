package com.adaqa.simulation;

import net.minecraft.util.math.Vec3d;
import java.lang.Math;
import java.util.List;

public class TrisolaranSystem {
    private static final ThreeBodySystem system = new ThreeBodySystem();

    // 物理模拟步进
    public static void tick() {
        system.update(0.05); 
    }

    public static Vec3d getSkyColor() {
        return new Vec3d(0.65F, 0.60F, 1.0F);
    }

    public static double getSunSize(int idx){
        Vec3d pos = getSunPos(idx);
        double dist = pos.length();
        if (dist < 1.0) dist = 1.0;
        return 3000.0 / dist;
    }

    public static double[] getSunAngle(int idx){
        double[] debug_angle = {0, 0};
        // 获取相对于行星的太阳位置
        Vec3d sun_pos = getSunPos(idx);
        
        double horizontalLength = sun_pos.horizontalLength();
        if (horizontalLength == 0) horizontalLength = 0.0001;

        debug_angle[0] = Math.atan(sun_pos.y / horizontalLength) / (2 * Math.PI);
        debug_angle[1] = Math.atan2(-sun_pos.z, sun_pos.x) / (2 * Math.PI);
        
        return debug_angle;
    }

    public static Vec3d getSunPos(int idx) {
        try {
            // 获取太阳位置
            ThreeBodySystem.Body sun = system.getBody(idx);
            
            // 获取行星位置 (假设行星是第4个物体，index 3)
            List<ThreeBodySystem.Body> bodies = system.getBodies();
            if (bodies.size() > 3) {
                ThreeBodySystem.Body planet = bodies.get(3);
                // 返回太阳相对于行星的位置
                return sun.position.subtract(planet.position);
            }
            
            return sun.position;
        } catch (Exception e) {
            return new Vec3d(0, 100, 0);
        }
    }
    
    public static ThreeBodySystem getSystem() {
        return system;
    }
}

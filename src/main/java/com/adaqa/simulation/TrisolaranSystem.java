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

    // 计算单个太阳的光照强度
    // 考虑距离平方反比定律和入射角度
    public static double getSunIntensity(int idx) {
        try {
            Vec3d sunPos = getSunPos(idx);
            double distSq = sunPos.lengthSquared();
            
            // 避免距离过近导致的除零或数值爆炸
            if (distSq < 100.0) distSq = 100.0;
            
            // 基础强度 (假设标准距离约为 100.0)
            // 之前的常数 1,000,000 太大了，导致距离 100 时强度高达 100.0
            // 现在调整为 15,000，使得距离 100 时强度约为 1.5 (明亮)，距离 300 时约为 0.16 (昏暗)
            double baseIntensity = 15000.0 / distSq;
            
            // 考虑仰角
            // 优化：直接使用向量计算 sin(elevation)，避免重复调用 getSunPos 和三角函数
            // sin(elevation) = y / distance
            double sinAngle = sunPos.y / Math.sqrt(distSq);
            
            // 如果在地平线以下，强度为 0 (或者非常微弱的散射光?)
            if (sinAngle < 0) {
                return 0.0;
            }
            
            return baseIntensity * sinAngle;
        } catch (Exception e) {
            return 0.0;
        }
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
        
        // 获取行星的朝向 (Orientation)
        // 我们需要把 sun_pos 从"绝对坐标系"转换到"行星表面坐标系"
        // 假设 orientation 向量代表天顶 (Up)，velocity 方向代表北方 (North)
        // 这是一个简化的 LookAt 变换
        
        List<ThreeBodySystem.Body> bodies = system.getBodies();
        if (bodies.size() > 3) {
            ThreeBodySystem.Body planet = bodies.get(3);
            Vec3d up = planet.orientation; // 天顶
            
            // 为了构建正交基，我们需要另一个向量。
            // 假设 (0,1,0) 是绝对坐标系的 Y 轴。
            // 如果 orientation 接近 (0,1,0)，我们用 (1,0,0) 做参考。
            // 这里我们用一种更稳健的方法：
            // 我们不依赖速度方向做北方，因为速度可能为0或者垂直于地表。
            // 我们构建一个相对稳定的"地表坐标系"。
            // Z轴 (North) = Up x (1,0,0) (如果Up平行X轴则改用(0,0,1))
            
            Vec3d ref = new Vec3d(1, 0, 0);
            if (Math.abs(up.dotProduct(ref)) > 0.9) {
                ref = new Vec3d(0, 0, 1);
            }
            
            Vec3d north = up.crossProduct(ref).normalize(); // Z axis
            Vec3d east = north.crossProduct(up).normalize(); // X axis
            
            // 将 sun_pos 投影到这个新基底上
            // sun_local.x = sun_pos . east
            // sun_local.y = sun_pos . up (Elevation component)
            // sun_local.z = sun_pos . north
            
            double lx = sun_pos.dotProduct(east);
            double ly = sun_pos.dotProduct(up);
            double lz = sun_pos.dotProduct(north);
            
            // 重新计算仰角和方位角
            double horizontalDist = Math.sqrt(lx*lx + lz*lz);
            if (horizontalDist < 0.0001) horizontalDist = 0.0001;
            
            debug_angle[0] = Math.atan(ly / horizontalDist) / (2 * Math.PI); // Elevation
            debug_angle[1] = Math.atan2(-lz, lx) / (2 * Math.PI); // Azimuth
            
            return debug_angle;
        }

        // Fallback for non-planet scenarios or early init
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

    public static Vec3d getCalculatedSkyColor() {
        double totalIntensity = 0.0;
        for (int i = 0; i < 3; i++) {
            totalIntensity += getSunIntensity(i);
        }
        

        
        // 映射总强度到颜色亮度
        // 假设标准强度为 1.0 -> 正常的蓝色天空 (0.65, 0.6, 1.0)
        // 强度 > 1.0 -> 趋向白色
        // 强度 < 1.0 -> 趋向黑色
        
        // 基础天空色
        double r = 0.65;
        double g = 0.60;
        double b = 1.0;
        
        // 调整因子
        double brightness = Math.min(totalIntensity, 2.0); // 限制最大亮度
        
        // 简单的线性乘法 (这可能需要非线性映射来模拟人眼适应)
        return new Vec3d(r * brightness, g * brightness, b * brightness);
    }
    
    // 计算晚霞颜色
    // 返回 float[4] {r, g, b, a} 或者 null
    /*
    public static float[] getFogColorOverride(float tickDelta) {
        float[] totalFogColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        boolean hasSunset = false;
        
        for (int i = 0; i < 3; i++) {
            // 获取仰角
            double elevation = getSunAngle(i)[0]; // turns
            double angle = elevation * 2 * Math.PI; // radians
            
            // 检查是否在日出/日落区间 (例如仰角在 0 到 15 度之间)
            // 0度 = 0.0 rad, 15度 ~= 0.26 rad
            double horizonAngle = 0.0;
            double twilightAngle = Math.toRadians(15.0); // 约 0.26
            
            if (angle >= horizonAngle && angle <= twilightAngle) {
                // 计算晚霞强度 (越接近地平线越强，但太低也没光)
                // 使用简单的线性插值
                double factor = 1.0 - (angle / twilightAngle);
                
                // 结合太阳本身的强度 (主要是距离)
                // 这里简化处理，假设近的太阳晚霞更亮
                double intensity = getSunIntensity(i) * 2.0; // 晚霞增强系数
                
                // 累加颜色 (简单的橙红色)
                // R: 1.0, G: 0.4, B: 0.1
                totalFogColor[0] += 1.0f * (float)(factor * intensity);
                totalFogColor[1] += 0.4f * (float)(factor * intensity);
                totalFogColor[2] += 0.1f * (float)(factor * intensity);
                totalFogColor[3] += (float)(factor * intensity); // Alpha
                
                hasSunset = true;
            }
        }
        
        if (!hasSunset || totalFogColor[3] <= 0.001f) {
            return null;
        }
        
        // 归一化 Alpha
        if (totalFogColor[3] > 1.0f) {
            totalFogColor[0] /= totalFogColor[3];
            totalFogColor[1] /= totalFogColor[3];
            totalFogColor[2] /= totalFogColor[3];
            totalFogColor[3] = 1.0f;
        }
        
        return totalFogColor;
    }
    */

    public static ThreeBodySystem getSystem() {
        return system;
    }
    
    public static void randomize() {
        system.randomize();
    }

    public static double getCalculatedSkyAngle() {
        // 找到光照强度最大的太阳
        double maxIntensity = -1.0;
        int maxIndex = -1;
        
        for (int i = 0; i < 3; i++) {
            double intensity = getSunIntensity(i);
            // 这里我们可能需要考虑即使在地平线以下的太阳，谁离天顶最近？
            // 但目前的 getSunIntensity 在地平线以下返回 0
            // 所以我们还需要一个辅助判断：如果所有强度都是0 (全黑夜)，
            // 我们选一个"离天顶最近"或者"离地平线最近"的太阳作为参考
            
            if (intensity > maxIntensity) {
                maxIntensity = intensity;
                maxIndex = i;
            }
        }
        
        // 如果所有太阳都在地平线以下 (maxIntensity == 0)
        // 我们回退到使用仰角最高的那个 (这部分逻辑和之前一样)
        if (maxIndex == -1 || maxIntensity <= 0.0001) {
            double maxElevation = -999.0;
            for (int i = 0; i < 3; i++) {
                double elevation = getSunAngle(i)[0];
                if (elevation > maxElevation) {
                    maxElevation = elevation;
                    maxIndex = i;
                }
            }
        }
        
        // 使用选定太阳的仰角计算时间
        double selectedElevation = getSunAngle(maxIndex)[0];
        
        // 映射: Zenith (0.25) -> 0.0, Horizon (0.0) -> 0.25
        return 0.25 - selectedElevation;
    }
}

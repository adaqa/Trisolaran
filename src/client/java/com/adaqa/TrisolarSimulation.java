package com.adaqa;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import java.lang.Math;

public class TrisolarSimulation {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final MinecraftServer server = client.getServer();
    private static int debug_cnt = 0;
    //private static Vec3d sun_pos[] = new Vec3d[3];

    private static final double[] orbitRadius = {70, 100, 130};
    private static final double[] orbitAngle = {0, Math.PI * 2/3, Math.PI * 1/2};
    private static final double[] orbitSpeed = {0.022, 0.03, 0.075};

    public static Vec3d getSkyColor() {
        Vec3d debug_color = new Vec3d(0.65F, 0.60F, 1.0F);
        return debug_color;
    }

    public static double getSunSize(int idx){
        return 30.0F * 100.0F / orbitRadius[idx];
        //return 30.0F * 100.0F / suns_pos[idx].length();
    }

    public static double[] getSunAngle(int idx){
        double[] debug_angle = {0, 0};
        Vec3d sun_pos = getSunPos(idx);
        debug_angle[0] = Math.atan(sun_pos.y / sun_pos.horizontalLength()) / (2 * Math.PI);
        debug_angle[1] = Math.atan2(-sun_pos.z, sun_pos.x) / (2 * Math.PI);
        if (debug_cnt++ % 1000 == 0 && idx == 2) {
            System.out.println("idx: " + idx + ", sun_pos: " + sun_pos);
            System.out.println("angle: " + debug_angle[0] + ", " + debug_angle[1]);
            System.out.println("Rendering sun at angle: " + (float)(debug_angle[0] - 0.25F) * 360.0F + ", " + (float)(debug_angle[1] * 360.0F));
        }
        return debug_angle;
    }

    public static Vec3d getSunPos(int idx) {
        int tickTimes = server.getTicks();
        double trueAnomaly = tickTimes * orbitSpeed[idx];
        double x = orbitRadius[idx] * Math.cos(orbitAngle[idx]) * Math.cos(trueAnomaly);
        double z = orbitRadius[idx] * Math.sin(orbitAngle[idx]) * Math.cos(trueAnomaly);
        double y = orbitRadius[idx] * Math.sin(trueAnomaly);
        return new Vec3d(x, y, z);
        //return suns_pos[idx];
    }
}

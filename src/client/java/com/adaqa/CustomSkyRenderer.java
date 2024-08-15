package com.adaqa;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import static com.adaqa.TrisolarSimulation.*;

public class CustomSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {

    private static final Identifier SUN_TEXTURE = Identifier.ofVanilla("textures/environment/sun.png");
    private static final Identifier MOON_PHASES_TEXTURE = Identifier.ofVanilla("textures/environment/moon_phases.png");

    @Override
    public void render(WorldRenderContext context) {
        ClientWorld world = context.world();
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(context.positionMatrix());

        if (isValidSubmersionType(context.camera().getSubmersionType())) {
            renderSky(context, matrixStack, world);
        }
    }

    private boolean isValidSubmersionType(CameraSubmersionType type) {
        return type != CameraSubmersionType.POWDER_SNOW && type != CameraSubmersionType.LAVA;
    }

    private void renderSky(WorldRenderContext context, MatrixStack matrixStack, ClientWorld world) {
        float tickDelta = context.tickCounter().getTickDelta(true);
        Matrix4f projectionMatrix = context.projectionMatrix();
        Vec3d skyColor = new Vec3d(0.65F, 0.6F, 1.0F);

        setupInitialRenderState(skyColor);
        RenderSystem.enableBlend();
        renderFogEffect(world, tickDelta, matrixStack);
        renderSunAndMoon(world, tickDelta, matrixStack);
        renderStars(world, tickDelta, matrixStack, projectionMatrix);
        resetRenderState();
    }

    private void setupInitialRenderState(Vec3d skyColor) {
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1.0F);
        BackgroundRenderer.applyFogColor();
    }

    private void renderFogEffect(ClientWorld world, float tickDelta, MatrixStack matrixStack) {
        float[] fogColorOverride = world.getDimensionEffects().getFogColorOverride(0.0F, tickDelta);
        if (fogColorOverride != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            setupFogColorRender(matrixStack, fogColorOverride);
        }
    }

    private void setupFogColorRender(MatrixStack matrixStack, float[] fogColorOverride) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.push();
        renderFogTriangles(matrixStack.peek().getPositionMatrix(), fogColorOverride);
        matrixStack.pop();
    }

    private void renderFogTriangles(Matrix4f positionMatrix, float[] fogColor) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(positionMatrix, 0.0F, 100.0F, 0.0F).color(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);

        for (int i = 0; i <= 16; i++) {
            float angle = (float) i * (float) (Math.PI * 2) / 16.0F;
            float sin = MathHelper.sin(angle);
            float cos = MathHelper.cos(angle);
            bufferBuilder.vertex(positionMatrix, sin * 120.0F, cos * 120.0F, -cos * 40.0F * fogColor[3]).color(fogColor[0], fogColor[1], fogColor[2], 0.0F);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private void renderSunAndMoon(ClientWorld world, float tickDelta, MatrixStack matrixStack) {
        float skyAngle = world.getSkyAngle(tickDelta);
        float rainGradient = 1.0F - world.getRainGradient(tickDelta);

        matrixStack.push();
        setupSunAndMoonRender(rainGradient);
        for (int i = 0; i < 3; i++) {
            renderSun(matrixStack, getSunAngle(i), getSunSize(i));
        }
    }

    private void setupSunAndMoonRender(float rainGradient) {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainGradient);
    }

    private void renderSun(MatrixStack matrixStack, double[] sunAngle, double sunSize) {
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(sunAngle[1] * 360.0F)));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)((sunAngle[0] - 0.25) * 360.0F)));
        //matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
        //matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
        float size = (float)sunSize;
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(positionMatrix, -size, 100.0F, -size).texture(0.0F, 0.0F);
        bufferBuilder.vertex(positionMatrix, size, 100.0F, -size).texture(1.0F, 0.0F);
        bufferBuilder.vertex(positionMatrix, size, 100.0F, size).texture(1.0F, 1.0F);
        bufferBuilder.vertex(positionMatrix, -size, 100.0F, size).texture(0.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrixStack.pop();
    }

    private void renderMoon(MatrixStack matrixStack, int moonPhase) {
        float size = 20.0F;
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        RenderSystem.setShaderTexture(0, MOON_PHASES_TEXTURE);
        float[] textureCoords = getMoonTextureCoords(moonPhase);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(positionMatrix, -size, -100.0F, size).texture(textureCoords[2], textureCoords[3]);
        bufferBuilder.vertex(positionMatrix, size, -100.0F, size).texture(textureCoords[0], textureCoords[3]);
        bufferBuilder.vertex(positionMatrix, size, -100.0F, -size).texture(textureCoords[0], textureCoords[1]);
        bufferBuilder.vertex(positionMatrix, -size, -100.0F, -size).texture(textureCoords[2], textureCoords[1]);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private float[] getMoonTextureCoords(int moonPhase) {
        int x = moonPhase % 4;
        int y = moonPhase / 4 % 2;

        return new float[]{
                (float) (x) / 4.0F,
                (float) (y) / 2.0F,
                (float) (x + 1) / 4.0F,
                (float) (y + 1) / 2.0F
        };
    }

    private void renderStars(ClientWorld world, float tickDelta, MatrixStack matrixStack, Matrix4f projectionMatrix) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        float starBrightness = world.getStarBrightness(tickDelta) * (1.0F - world.getRainGradient(tickDelta));
        if (starBrightness > 0.0F) {
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            BackgroundRenderer.clearFog();
            // this.starsBuffer.bind();
            // this.starsBuffer.draw(matrixStack.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionProgram());
            VertexBuffer.unbind();
        }
    }

    private void resetRenderState() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }
}

package com.direwolf20.laserio.client.renderer;

import com.direwolf20.laserio.common.LaserIO;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;

public class MyRenderType extends RenderType {
    private final static ResourceLocation laserBeam = ResourceLocation.parse(LaserIO.MODID + ":textures/misc/laser.png");
    private final static ResourceLocation laserBeam2 = ResourceLocation.parse(LaserIO.MODID + ":textures/misc/laser2.png");
    private final static ResourceLocation laserBeamGlow = ResourceLocation.parse(LaserIO.MODID + ":textures/misc/laser_glow.png");

    // Dummy
    public MyRenderType(String name, VertexFormat format, VertexFormat.Mode p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

    public static void updateRenders() {
        /*LASER_MAIN_BEAM = create("MiningLaserMainBeam",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
                RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam2, false, false))
                        .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setLayeringState(NO_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(true));
        LASER_MAIN_CORE = create("MiningLaserCoreBeam",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
                RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
                        .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true));
        CONNECTING_LASER = create("ConnectingLaser",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
                RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
                        .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true));*/
    }

    public static final RenderType LASER_MAIN_BEAM = create("MiningLaserMainBeam",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam2, false, false))
                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setLayeringState(NO_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));
    public static final RenderType LASER_MAIN_CORE = create("MiningLaserCoreBeam",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));
    public static final RenderType CONNECTING_LASER = create("ConnectingLaser",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));
    //Unused?
    public static final RenderType LASER_MAIN_ADDITIVE = create("MiningLaserAdditiveBeam",
            DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeamGlow, false, false))
                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType BlockOverlay = create("MiningLaserBlockOverlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    /*public static final RenderType RenderBlock = create("MiningLaserRenderBlock",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
//                    .setShaderState(SMOOTH_SHADE)
                    .setShaderState(ShaderStateShard.BLOCK_SHADER)
                    .setLightmapState(LIGHTMAP)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));*/
}

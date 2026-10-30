package nova.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import nova.NovaMod;

/**
 * 注册并持有天空快照表面使用的 core shader。
 */
public final class SkyboxShader {
    private static ShaderProgram program;

    private SkyboxShader() {
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context ->
                context.register(new Identifier(NovaMod.MOD_ID, "skybox_surface"), VertexFormats.POSITION, shader -> program = shader)
        );
    }

    public static ShaderProgram getProgram() {
        return program;
    }
}

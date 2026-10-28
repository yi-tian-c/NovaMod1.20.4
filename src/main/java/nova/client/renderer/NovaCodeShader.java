package nova.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import nova.NovaMod;

/**
 * 注册并持有 NovaBlock 使用的 Code Sky 投影 core shader。
 */
public final class NovaCodeShader {
    private static ShaderProgram program;

    private NovaCodeShader() {
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context ->
                context.register(
                        new Identifier(NovaMod.MOD_ID, "nova_code"),
                        VertexFormats.POSITION,
                        shader -> program = shader
                )
        );
    }

    public static ShaderProgram getProgram() {
        return program;
    }
}

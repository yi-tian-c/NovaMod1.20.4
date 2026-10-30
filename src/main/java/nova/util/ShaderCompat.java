package nova.util;

import net.fabricmc.loader.api.FabricLoader;

public final class ShaderCompat {
    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    private ShaderCompat() {}

    /** 是否安装了 Iris */
    public static boolean isIrisLoaded() {
        return IRIS_LOADED;
    }

    /**
     * 当前是否真正启用了光影包。
     * 简单版先用 isIrisLoaded()，后续可以换成 Iris 的 API 做更精确判断。
     */
    public static boolean isShaderEnabled() {
        return IRIS_LOADED;
    }
}
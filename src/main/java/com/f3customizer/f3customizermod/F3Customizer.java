package com.f3customizer.f3customizermod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

@Mod(modid = "f3customizer", 
     name = "F3 Customizer", 
     version = "1.1",  // 版本号更新
     guiFactory = "com.f3customizer.f3customizermod.F3Customizer$GuiFactory",
     clientSideOnly = true,
     acceptedMinecraftVersions = "[1.12.2]")
public class F3Customizer {
    
    // 调试行配置项
    public static boolean hideAll = false;
    public static boolean hideXYZ = true;
    public static boolean hideFPS = false;
    public static boolean hideBiome = false;
    public static boolean hideFacing = true;
    public static boolean hideChunk = false;
    public static boolean hideBlock = true;
    public static boolean hideLight = false;
    public static boolean hideEntCount = true;
    public static boolean hideRam = false;
    public static boolean hideMisc = true;
    
    // 新增：自定义隐藏模式
    public static String[] customPatterns = new String[0];
    
    private static Configuration config;
    private static File configFile;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        loadConfig();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 初始化代码
    }
    
    public static void loadConfig() {
        hideAll = config.getBoolean("hideAll", Configuration.CATEGORY_GENERAL, false, "Force hide ALL debug lines");
        hideXYZ = config.getBoolean("hideXYZ", Configuration.CATEGORY_GENERAL, true, "Hide XYZ position line");
        hideFPS = config.getBoolean("hideFPS", Configuration.CATEGORY_GENERAL, false, "Hide FPS and performance line");
        hideBiome = config.getBoolean("hideBiome", Configuration.CATEGORY_GENERAL, false, "Hide Biome info line");
        hideFacing = config.getBoolean("hideFacing", Configuration.CATEGORY_GENERAL, true, "Hide Facing direction line");
        hideChunk = config.getBoolean("hideChunk", Configuration.CATEGORY_GENERAL, false, "Hide Chunk info line");
        hideBlock = config.getBoolean("hideBlock", Configuration.CATEGORY_GENERAL, true, "Hide Block info line");
        hideLight = config.getBoolean("hideLight", Configuration.CATEGORY_GENERAL, false, "Hide Light level line");
        hideEntCount = config.getBoolean("hideEntCount", Configuration.CATEGORY_GENERAL, true, "Hide Entity count line");
        hideRam = config.getBoolean("hideRam", Configuration.CATEGORY_GENERAL, false, "Hide RAM usage line");
        hideMisc = config.getBoolean("hideMisc", Configuration.CATEGORY_GENERAL, true, "Hide other misc lines");
        
        // 加载自定义模式
        customPatterns = config.getStringList("customPatterns", Configuration.CATEGORY_GENERAL, 
            new String[]{"Example: ", "Test pattern"}, 
            "Add custom patterns to hide (one per line)");
        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals("f3customizer")) {
            loadConfig();
        }
    }
    
    // 所有事件处理器
    @SideOnly(Side.CLIENT)
    private static class EventHandler {
        private final List<String> hiddenPatterns = new ArrayList<>();
        
        @SubscribeEvent
        public void onRenderDebugText(RenderGameOverlayEvent.Text event) {
            if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
            
            // 如果强制隐藏所有，立即清除所有行
            if (hideAll) {
                event.getLeft().clear();
                event.getRight().clear();
                return;
            }
            
            // 更新隐藏模式列表
            updateHiddenPatterns();
            
            // 过滤调试文本
            filterDebugList(event.getLeft());
            filterDebugList(event.getRight());
        }
        
        private void updateHiddenPatterns() {
            hiddenPatterns.clear();
            
            // 添加预定义模式
            if (hideXYZ) {
                hiddenPatterns.add("XYZ: ");
                hiddenPatterns.add("Looking at: ");
            }
            if (hideFPS) {
                hiddenPatterns.add(" fps");
                hiddenPatterns.add(" T: ");
            }
            if (hideBiome) {
                hiddenPatterns.add("Biome: ");
            }
            if (hideFacing) {
                hiddenPatterns.add("Facing: ");
            }
            if (hideChunk) {
                hiddenPatterns.add("Chunk: ");
                hiddenPatterns.add("C: ");
            }
            if (hideBlock) {
                hiddenPatterns.add("Block: ");
            }
            if (hideLight) {
                hiddenPatterns.add("Light: ");
                hiddenPatterns.add("Client Light: ");
            }
            if (hideEntCount) {
                hiddenPatterns.add("Entities: ");
                hiddenPatterns.add("E: ");
            }
            if (hideRam) {
                hiddenPatterns.add("Allocated: ");
                hiddenPatterns.add("%");
                hiddenPatterns.add("MB");
            }
            if (hideMisc) {
                hiddenPatterns.add("Chunk cache");
                hiddenPatterns.add("Forced chunk");
                hiddenPatterns.add("MC");
                hiddenPatterns.add("P: ");
                hiddenPatterns.add("Server chunk");
                hiddenPatterns.add("Smooth lighting");
                hiddenPatterns.add("Sound manager");
                hiddenPatterns.add("Targeted Entity");
                hiddenPatterns.add("Targeted Block");
                hiddenPatterns.add("Java:");
                hiddenPatterns.add("Display:");
                hiddenPatterns.add("CPU:");
                hiddenPatterns.add("Display mode:");
                hiddenPatterns.add("Sound devices:");
                hiddenPatterns.add("OpenGL:");
                hiddenPatterns.add("GL Caps:");
                hiddenPatterns.add("Using VBOs");
                hiddenPatterns.add("Vec3");
            }
            
            // 添加自定义模式
            for (String pattern : customPatterns) {
                if (!pattern.trim().isEmpty()) {
                    hiddenPatterns.add(pattern.trim());
                }
            }
        }
        
        private void filterDebugList(List<String> lines) {
            if (lines == null || lines.isEmpty()) return;
            
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                for (String pattern : hiddenPatterns) {
                    if (line.contains(pattern)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }
    
    // GUI工厂实现
    public static class GuiFactory implements IModGuiFactory {
        @Override
        public void initialize(Minecraft minecraftInstance) {}
        
        @Override
        public boolean hasConfigGui() {
            return true;
        }
        
        @Override
        public GuiScreen createConfigGui(GuiScreen parentScreen) {
            return new ConfigGui(parentScreen);
        }
        
        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return Collections.emptySet();
        }
    }
    
    // 配置GUI
    public static class ConfigGui extends GuiConfig {
        public ConfigGui(GuiScreen parent) {
            super(parent, getConfigElements(), 
                 "f3customizer", 
                 false, false, 
                 "F3 Debug Screen Customization");
        }
        
        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> elements = new ArrayList<>();
            ConfigCategory category = config.getCategory(Configuration.CATEGORY_GENERAL);
            
            // 基本配置
            elements.add(new ConfigElement(category.get("hideAll")));
            elements.add(new ConfigElement(category.get("hideXYZ")));
            elements.add(new ConfigElement(category.get("hideFPS")));
            elements.add(new ConfigElement(category.get("hideBiome")));
            elements.add(new ConfigElement(category.get("hideFacing")));
            elements.add(new ConfigElement(category.get("hideChunk")));
            elements.add(new ConfigElement(category.get("hideBlock")));
            elements.add(new ConfigElement(category.get("hideLight")));
            elements.add(new ConfigElement(category.get("hideEntCount")));
            elements.add(new ConfigElement(category.get("hideRam")));
            elements.add(new ConfigElement(category.get("hideMisc")));
            
            // 自定义模式配置
            elements.add(new ConfigElement(category.get("customPatterns")));
            
            return elements;
        }
    }
}
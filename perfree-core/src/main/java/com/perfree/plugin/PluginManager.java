package com.perfree.plugin;

import com.perfree.plugin.register.PluginRegister;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PluginManager extends DefaultPluginManager implements PluginManagerService, ApplicationContextAware {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);
    @Autowired
    PluginRegister pluginRegister;

    ApplicationContext applicationContext;


    public PluginManager(Path... pluginsRoots) {
        super(pluginsRoots);
    }


    @Override
    public PluginInfo install(Path path) throws Exception {
        String pluginId = null;
        try {
            pluginId = loadPlugin(path);
            startPlugin(pluginId);
            PluginInfo plugin = new PluginInfo(getPlugin(pluginId), applicationContext);
            pluginRegister.registry(plugin);
            PluginHolder.put(pluginId, plugin);
            LOGGER.info("install plugin [{}] success", plugin.getPluginId());
            return plugin;
        } catch (Exception e) {
            e.printStackTrace();
            if(StringUtils.isNotBlank(pluginId)) {
                PluginHolder.remove(pluginId);
            }
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void unInstall(String pluginId) throws Exception {
        LOGGER.info("unInstallPlugin:" + pluginId);
        PluginInfo plugin = PluginHolder.getPlugin(pluginId);

        if(plugin != null) {
            try {
                pluginRegister.unRegistry(plugin);
                PluginHolder.remove(pluginId);
                if(!unloadPlugin(pluginId, false)) {
                    throw new Exception("plugin [" + pluginId + "] 卸载失败");
                }
                PluginsUtils.forceDelete(plugin.getPluginWrapper().getPluginPath().toFile());
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }
    }

    @Override
    public void initPlugins() throws Exception {
        loadPlugins();
        startPlugins();
        pluginRegister.initialize();
        for (PluginWrapper startedPlugin : getPlugins()) {
            PluginInfo plugin = new PluginInfo(startedPlugin, applicationContext);
            PluginHolder.put(startedPlugin.getPluginId(), plugin);
            try {
                pluginRegister.registry(plugin);
            } catch (Exception e) {
                PluginHolder.remove(startedPlugin.getPluginId());
                unloadPlugin(plugin.getPluginWrapper().getPluginId());
                throw new Exception(e.getMessage());
            }
        }
    }

    @Override
    public List<PluginWrapper> getInstallPlugins() {
        return getPlugins();
    }

    @Override
    public String loadPlugin(Path pluginPath) {
        if ((pluginPath == null) || Files.notExists(pluginPath)) {
            throw new IllegalArgumentException(String.format("plugin %s does not exist!", pluginPath));
        }
        LOGGER.info("Loading plugin from '{}'", pluginPath);
        PluginWrapper pluginWrapper = loadPluginFromPath(pluginPath);
        try {
            resolvePlugins();
        } catch (Exception e) {
            unloadPlugin(pluginWrapper.getPluginId());
        }

        return pluginWrapper.getDescriptor().getPluginId();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /** 
     * @description 根据插件id获取插件信息
     * @param pluginId 插件id
     * @return com.perfree.plugin.PluginInfo 
     * @author Perfree
     */ 
    public PluginInfo getPluginInfoById(String pluginId){
        return PluginHolder.getPlugin(pluginId);
    }
}
package imooc.naga.server.controller;

import cn.hutool.core.date.DateUtil;
import imooc.naga.core.exception.ErrorCodes;
import imooc.naga.entity.meta.ProjectInfo;
import imooc.naga.entity.plugin.PluginCategory;
import imooc.naga.entity.plugin.PluginPackage;
import imooc.naga.entity.plugin.PluginStatus;
import imooc.naga.server.BaseController;
import imooc.naga.server.jwt.ContextUtil;
import imooc.naga.server.jwt.LoginRequired;
import imooc.naga.server.log.OperationObj;
import imooc.naga.server.log.OperationRecord;
import imooc.naga.server.service.MetaService;
import imooc.naga.server.service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/naga/v1/task")
@CrossOrigin
public class PluginController extends BaseController {


    @Autowired
    PluginService pluginService;
    @Autowired
    MetaService metaService;

    @ResponseBody
    @PostMapping("plugin")
    @LoginRequired
    @OperationRecord("上传插件")
    @CrossOrigin(value = "*",allowedHeaders="*",allowCredentials="true",maxAge = 10000)
    public Object uploadPlugin(@RequestParam("pkgName") String pkgName,
                               @RequestParam("pkgVersion") String pkgVersion,
                               @RequestParam(value = "status", defaultValue = "Dev") PluginStatus status,
                               @RequestParam(value = "description", defaultValue = "") String description,
                               @RequestParam(value = "tags", defaultValue = "") String tags,
                               @RequestParam("category") PluginCategory category,
                               @RequestParam("projectName") String projectName,
                               @RequestParam("file") MultipartFile file) {

        ProjectInfo projectInfoByName = metaService.findProjectInfoByName(projectName);

        if (projectInfoByName == null) {
            return getError(ErrorCodes.ERROR_PARAM, "project not exists");
        }
        //调用save pluginfile方法
        try {
            PluginPackage pluginPackage = pluginService.savePluginFile(file.getInputStream(), pkgName, pkgVersion);
            pluginPackage.setPluginStatus(status);
            pluginPackage.setPluginDesc(description);
            pluginPackage.setTags(tags);
            pluginPackage.setPluginCategory(category);
            pluginPackage.setProjectId(projectInfoByName.getId());
            pluginPackage.setProjectName(projectName);
            //todo set admin and team by loginuser
            pluginPackage.setAdmin(ContextUtil.getCurrentUser().getName());
            pluginPackage.setTeam(ContextUtil.getCurrentUser().getTeam());
            pluginPackage.setTrash(false);
            pluginPackage.setCreateTime(DateUtil.toIntSecond(new Date()));
            pluginService.update(pluginPackage);
            return getResult(true);
        } catch (IOException e) {
            e.printStackTrace();
            return getError(ErrorCodes.ERROR_PARAM, e.getMessage());
        }

        //完善plugin 详情


    }


    @ResponseBody
    @GetMapping("plugins")
    @LoginRequired
    @OperationRecord("获取插件列表")
    public Object listPlugins(@RequestParam(name = "pageIndex", required = true, defaultValue = "1")
                                      int pageIndex,
                              @RequestParam(name = "pageSize", required = true, defaultValue = "20")
                                      int pageSize) {
        Page<PluginPackage> plugins = pluginService.getPlugins(pageIndex-1, pageSize, null, null);
        Map<String,Object> pages = new HashMap<>();
        pages.put("pages",plugins.getContent());
        pages.put("pageIndex",pageIndex);
        pages.put("pageSize",pageSize);
        pages.put("pageCount",plugins.getTotalPages());
        return getResult(pages);
    }

    @ResponseBody
    @GetMapping("plugin")
    @LoginRequired
    @OperationRecord("获取插件详情")
    public Object getPluginInfo(@OperationObj @RequestParam Long id) {
        PluginPackage pluginByNameVersion = pluginService.getPlugin(id);
        Map<String,Object> info = new HashMap<>();
        info.put("id",pluginByNameVersion.getId());
        info.put("name",pluginByNameVersion.getName());
        info.put("version",pluginByNameVersion.getVersion());
        List<Map<String,Object>> params = new ArrayList<>();
        pluginByNameVersion.getDefaultParams().forEach(packageParam -> {
            Map<String,Object> param = new HashMap<>();
            param.put("name",packageParam.getName());
            param.put("value",packageParam.getDefaultValue());
            param.put("disable",!packageParam.isUserSetAble());
            params.add(param);
        });
        info.put("params",params);
        return getResult(info);
    }

    @ResponseBody
    @DeleteMapping("plugin")
    @LoginRequired
    @OperationRecord("删除插件")
    public Object delPlugin(@OperationObj @RequestParam long id) throws IOException {
        pluginService.delPlugin(id);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("plugin/name")
    @LoginRequired
    public Object listPluginNames() {
        return getResult(pluginService.getPluginGroupBy());
    }
}

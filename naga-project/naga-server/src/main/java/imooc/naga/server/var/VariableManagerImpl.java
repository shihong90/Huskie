package imooc.naga.server.var;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.base.Strings;
import imooc.naga.core.exception.ErrorCodes;
import imooc.naga.core.exception.NagaException;
import imooc.naga.entity.meta.ProjectInfo;
import imooc.naga.entity.plugin.PackageOutParam;
import imooc.naga.entity.plugin.ParamType;
import imooc.naga.entity.plugin.PluginPackage;
import imooc.naga.entity.task.JobInfo;
import imooc.naga.entity.var.ProjectVar;
import imooc.naga.entity.var.VariableType;
import imooc.naga.entity.var.VariableValue;
import imooc.naga.repository.meta.ProjectInfoRepository;
import imooc.naga.repository.plugin.PluginPackageRepository;
import imooc.naga.repository.task.JobInfoRepository;
import imooc.naga.repository.var.ProjectVarRepository;
import imooc.naga.server.var.model.AccumulatorValue;
import imooc.naga.server.var.model.CheckPointValue;
import imooc.naga.server.var.model.IndicatorValue;
import imooc.naga.server.var.model.QueueValue;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class VariableManagerImpl implements VariableManager {
    @Resource
    ProjectVarRepository projectVarRepository;
    @Resource
    ProjectInfoRepository projectInfoRepository;
    @Resource
    JobInfoRepository jobInfoRepository;
    @Resource
    PluginPackageRepository pluginPackageRepository;

    @Override
    public void createVariable(ProjectVar projectVar) {
        String projectName = projectVar.getProjectName();
        ProjectInfo byName = projectInfoRepository.findByName(projectName);
        if (byName == null) {
            throw new NagaException("project not exists", ErrorCodes.ERROR_PARAM);
        }
        projectVar.setProjectId(byName.getId());
        if (!Strings.isNullOrEmpty(projectVar.getBindParam())) {
            projectVar.setBind(true);
            projectVar.setBindTime(DateUtil.toIntSecond(new Date()));
        } else {
            projectVar.setBind(false);
            projectVar.setBindTime(-1);
        }

        if (projectVar.getBind()) {
            JobInfo jobInfo = jobInfoRepository.findByTaskNameAndName(projectVar.getBindTaskName(), projectVar.getBindJobName());
            PluginPackage plugin =
                    pluginPackageRepository.findById(jobInfo.getPkgId()).get();

            List<PackageOutParam> outParams = plugin.getOutParams();
            if (outParams != null && outParams.size() > 0) {
                for (PackageOutParam outParam : outParams) {
                    if (outParam.getName().equals(projectVar.getBindParam())) {
                        projectVar.setParamType(outParam.getType());
                        break;
                    }
                }
            }

        }
        if (projectVar.getInitValue() != null) {
            VariableValue value = null;
            if (VariableType.Indicator.equals(projectVar.getVarType())) {
                IndicatorValue indicatorValue = new IndicatorValue();
                indicatorValue.setValue(projectVar.getInitValue());
                indicatorValue.setParamType(projectVar.getParamType());
                value = indicatorValue;
            } else if (VariableType.Accumulator.equals(projectVar.getVarType())) {
                AccumulatorValue accumulatorValue = new AccumulatorValue();
                accumulatorValue.setValue(projectVar.getInitValue());
                accumulatorValue.setParamType(projectVar.getParamType());
                value = accumulatorValue;
            } else {
                QueueValue queueValue = new QueueValue();
                queueValue.setValue(projectVar.getInitValue());
                queueValue.setParamType(projectVar.getParamType());
                value = queueValue;
            }
            projectVar.setCurrentValue(value);

        }

        projectVarRepository.save(projectVar);
    }

    @Override
    public void update(ProjectVar var) {
        projectVarRepository.save(var);
    }

    @Override
    public void deleteVariable(long id) {
        projectVarRepository.deleteById(id);
    }

    @Override
    public void bindVariable(String projectName, String varName, String taskName, String jobName, String jobParam) {
        ProjectVar variable = projectVarRepository.findByProjectNameAndName(projectName, varName);
        variable.setBindTaskName(taskName);
        variable.setBindJobName(jobName);
        variable.setBindParam(jobParam);
        variable.setBindTime(DateUtil.toIntSecond(new Date()));
        variable.setBind(true);
        projectVarRepository.save(variable);
    }

    @Override
    public void unBindVariable(String projectName, String varName) {
        ProjectVar variable = projectVarRepository.findByProjectNameAndName(projectName, varName);
        variable.setBindTaskName(null);
        variable.setBindJobName(null);
        variable.setBindParam(null);
        variable.setBindTime(-1);
        variable.setBind(false);
        projectVarRepository.save(variable);
    }

    @Override
    public boolean isBind(String projectName, String varName) {
        return projectVarRepository.findByProjectNameAndName(projectName, varName).getBind();
    }

    @Override
    public ProjectVar getVariable(String projectName, String name) {
        return projectVarRepository.findByProjectNameAndName(projectName, name);
    }

    @Override
    public Page<ProjectVar> getVariables(String team, int pageIndex, int pageSize, String sort, Sort.Direction direction) {
        ProjectVar projectVar = new ProjectVar();
        projectVar.setTeam(team);
        projectVar.setTrash(false);
        return projectVarRepository.findAll(Example.of(projectVar),
                PageRequest.of(pageIndex, pageSize,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction,
                                ObjectUtil.isNull(sort) ? "id" : sort)));

    }

    @Override
    public void applyUpdate(String taskName, String jobName, Map<String, Object> values) {
        List<ProjectVar> variables = projectVarRepository.findByBindTaskNameAndBindJobName(taskName, jobName);
        if (variables == null || variables.size() == 0) {
            return;
        }

        for (ProjectVar variable : variables) {
            String param = variable.getBindParam();
            if (!values.containsKey(param)) {
                continue;
            }
            Object value = values.get(param);
            VariableValue currentValue = variable.getCurrentValue();
            if (currentValue == null) {
                variable.setInitTime(DateUtil.toIntSecond(new Date()));
            }
            if (VariableType.Indicator.equals(variable.getVarType())) {
                //indicator value process
                IndicatorValue indicatorValue = new IndicatorValue();
                indicatorValue.setParamType(variable.getParamType());
                indicatorValue.setValue(value);
                variable.setPreValue(currentValue);
                variable.setCurrentValue(indicatorValue);
                variable.setUpdateTime(DateUtil.toIntSecond(new Date()));
            } else if (VariableType.Accumulator.equals(variable.getVarType())) {
                // accumulator value process
                AccumulatorValue accumulatorValue = new AccumulatorValue();
                accumulatorValue.setValue(variable.getCurrentValue().getValue());
                accumulatorValue.setParamType(variable.getParamType());
                accumulatorValue.applyUpdate(value);
                variable.setPreValue(currentValue);
                variable.setCurrentValue(accumulatorValue);
                variable.setUpdateTime(DateUtil.toIntSecond(new Date()));

            } else if (VariableType.Queue.equals(variable.getVarType())) {
                if (ParamType.isEmpty(variable.getParamType(), value)) {
                    return;
                }
                QueueValue queueValue = new QueueValue();
                queueValue.setValue(value);
                queueValue.setParamType(variable.getParamType());
                variable.setPreValue(currentValue);
                variable.setCurrentValue(queueValue);
                variable.setUpdateTime(DateUtil.toIntSecond(new Date()));
            } else {
                // todo add other var type
            }
            projectVarRepository.save(variable);
        }
    }
}

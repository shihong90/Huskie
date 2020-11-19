package imooc.naga.server.visual;

import imooc.naga.entity.visual.ChartType;
import lombok.Data;

@Data
public abstract class ChartSpecific<C extends ChartSetting> {
    protected String querySql;
    protected String creator;
    protected ChartType chartType;
    protected String title;
    protected C chartSetting;

}
